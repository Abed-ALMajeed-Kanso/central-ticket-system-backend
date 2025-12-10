package com.example.ticket_system.service.impl;
import com.example.ticket_system.auth.user.entity.Role;
import com.example.ticket_system.auth.user.entity.User;
import com.example.ticket_system.dto.TicketViewSocketDto;
import com.example.ticket_system.entity.*;
import com.example.ticket_system.integration.aws.S3Service;
import com.example.ticket_system.integration.slack.SlackService;
import lombok.AllArgsConstructor;
import com.example.ticket_system.dto.MessageSocketDto;
import com.example.ticket_system.dto.TicketMessageDto;
import com.example.ticket_system.exception.ResourceNotFoundException;
import com.example.ticket_system.repository.TicketMessageAttachmentRepository;
import com.example.ticket_system.repository.TicketMessageRepository;
import com.example.ticket_system.repository.TicketRepository;
import com.example.ticket_system.auth.user.repository.UserRepository;
import com.example.ticket_system.service.TicketMessageService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Service
@AllArgsConstructor
public class TicketMessageServiceImpl implements TicketMessageService {

    private final TicketRepository ticketRepository;
    private final TicketMessageRepository ticketMessageRepository;
    private final UserRepository userRepository;
    private final TicketMessageAttachmentRepository attachmentRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final S3Service s3Service;
    private final SlackService slackService;

    @Override
    @Transactional
    public MessageSocketDto addTicketMessage(TicketMessageDto ticketMessageDto, Long ticketId) throws InterruptedException{

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Logged-in user not found"));

        TicketMessage message = new TicketMessage();
        message.setMessage(ticketMessageDto.getMessage());
        message.setTicket(ticket);
        message.setSender(user);

        ticketMessageRepository.save(message);

        List<String> attachmentUrls = new ArrayList<>();

        if (ticketMessageDto.getFiles() != null && !ticketMessageDto.getFiles().isEmpty()) {
            for (MultipartFile file : ticketMessageDto.getFiles()) {

                String original = file.getOriginalFilename().replaceAll("\\s+", "_");
                if (original == null || original.isBlank())
                    throw new RuntimeException("Missing file name");

                String key = "attachments/" + UUID.randomUUID() + "-" + original;

                String url = s3Service.store(file, key);
                TicketMessageAttachment attachment = new TicketMessageAttachment();
                attachment.setAttachment(url);
                attachment.setTicketMessage(message);
                attachmentRepository.save(attachment);
                message.getAttachments().add(attachment);
                attachmentUrls.add(url);
            }
        }

        ticket.getMessages().add(message);
        String roleName = user.getRoles().stream()
                .findFirst()
                .map(Role::getName)
                .orElse("USER");

        if (roleName.equals("ROLE_USER"))
            ticket.setViewedByAdmin(false);
        else if (roleName.equals("ROLE_ADMIN"))
            ticket.setViewedByUser(false);

        ticketRepository.save(ticket);

        MessageSocketDto dto = new MessageSocketDto();
        dto.setId(message.getId());
        dto.setMessage(message.getMessage());
        dto.setSenderId(user.getId());
        dto.setAttachments(attachmentUrls);
        dto.setCreatedAt(message.getCreatedAt() != null ? message.getCreatedAt() : LocalDateTime.now());

        messagingTemplate.convertAndSend("/topic/tickets/view-update", new TicketViewSocketDto(
                ticket.getId(),
                ticket.getViewedByUser(),
                ticket.getViewedByAdmin()
        ));

        messagingTemplate.convertAndSend("/topic/tickets/" + ticketId, dto);


        if (roleName.equals("ROLE_ADMIN") && ticketMessageDto.getShare()) {
            StringBuilder messageBuilder = new StringBuilder();
            messageBuilder.append("Reply on Ticket \"")
                    .append(ticket.getHeader())
                    .append("\" by Admin:\n")
                    .append(ticketMessageDto.getMessage());
            if (!attachmentUrls.isEmpty()) {
                messageBuilder.append("\nAttachments:\n");
                for (String url : attachmentUrls) {
                    messageBuilder.append("- ").append(url).append("\n");
                }
            }

            slackService.sendMessage(messageBuilder.toString());
        }

        return dto;
    }



    @Override
    public List<MessageSocketDto> getTicketMessages(Long ticketId, String messageQuery, String attachmentQuery) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        return ticket.getMessages().stream()
                .filter(msg -> {
                    boolean matchesMessage = messageQuery == null
                            || msg.getMessage().toLowerCase().contains(messageQuery.toLowerCase());

                    boolean matchesAttachment = attachmentQuery == null
                            || msg.getAttachments().stream()
                            .anyMatch(att -> att.getAttachment().toLowerCase().contains(attachmentQuery.toLowerCase()));

                    return matchesMessage && matchesAttachment;
                })
                .map(msg -> {
                    MessageSocketDto dto = new MessageSocketDto();
                    dto.setId(msg.getId());
                    dto.setMessage(msg.getMessage());
                    dto.setSenderId(msg.getSender().getId());
                    dto.setAttachments(
                            msg.getAttachments().stream()
                                    .map(TicketMessageAttachment::getAttachment)
                                    .toList()
                    );
                    dto.setCreatedAt(msg.getCreatedAt()); // add createdAt to DTO
                    return dto;
                })
                .toList();
    }

    @Override
    @Transactional
    public void viewedBySender(Long ticketId) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Logged-in user not found"));

        boolean isAdmin = user.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_ADMIN"));

        if (isAdmin)
            ticket.setViewedByAdmin(true);
        else
            ticket.setViewedByUser(true);

        ticketRepository.save(ticket);

    }



}
