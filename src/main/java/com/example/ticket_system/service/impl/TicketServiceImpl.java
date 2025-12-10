package com.example.ticket_system.service.impl;

import com.example.ticket_system.entity.TicketStatus;
import com.example.ticket_system.integration.aws.S3Service;
import com.example.ticket_system.integration.slack.SlackService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import com.example.ticket_system.dto.FirstTicketSocketDto;
import com.example.ticket_system.dto.TicketDto;
import com.example.ticket_system.entity.Ticket;
import com.example.ticket_system.entity.TicketMessage;
import com.example.ticket_system.entity.TicketMessageAttachment;
import com.example.ticket_system.auth.user.entity.User;
import com.example.ticket_system.exception.AccessDeniedException;
import com.example.ticket_system.exception.ResourceNotFoundException;
import com.example.ticket_system.repository.TicketRepository;
import com.example.ticket_system.auth.user.repository.UserRepository;
import com.example.ticket_system.service.TicketService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.criteria.Predicate;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Service
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final SlackService slackService;
    private final S3Service s3Service;

    @Override
    @Transactional
    public FirstTicketSocketDto addTicket(FirstTicketSocketDto firstTickeSocketDto)  throws InterruptedException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Logged-in user not found"));

        Ticket ticket = new Ticket();
        ticket.setHeader(firstTickeSocketDto.getHeader());
        ticket.setUser(user);
        User admin = userRepository.findByEmail("admin@example.com")
                .orElseThrow(() -> new ResourceNotFoundException("admin not found"));
        ticket.setAdmin(admin);

        TicketMessage message = new TicketMessage();
        message.setMessage(firstTickeSocketDto.getMessage());
        message.setSender(user);
        message.setTicket(ticket);

        List<String> uploadedUrls = new ArrayList<>();
        if (firstTickeSocketDto.getFiles() != null) {
            for (MultipartFile file : firstTickeSocketDto.getFiles()) {

                String original = file.getOriginalFilename().replaceAll("\\s+", "_");
                if (original == null || original.isBlank())
                    throw new RuntimeException("Missing file name");

                // Log the file info
                System.out.println("Received file: " + original);
                System.out.println("Content type: " + file.getContentType());
                System.out.println("Size (bytes): " + file.getSize());

                String key = "attachments/" + UUID.randomUUID() + "-" + original;

                String url = s3Service.store(file, key);

                TicketMessageAttachment att = new TicketMessageAttachment();
                att.setAttachment(url);
                att.setTicketMessage(message);
                uploadedUrls.add(url);

                message.getAttachments().add(att);
            }
        }

        ticket.getMessages().add(message);
        ticket.setUser(user);
        user.getTickets().add(ticket);

        ticket = ticketRepository.save(ticket);

        FirstTicketSocketDto socketDto = new FirstTicketSocketDto();
        socketDto.setId(ticket.getId());
        socketDto.setUserId(user.getId());
        socketDto.setAdminId(admin.getId());
        socketDto.setHeader(ticket.getHeader());
        socketDto.setMessage(message.getMessage());
        socketDto.setAttachments(uploadedUrls);
        socketDto.setTimestamp(message.getCreatedAt() != null ? message.getCreatedAt() : LocalDateTime.now());

        simpMessagingTemplate.convertAndSend("/topic/tickets/new", socketDto);

        if(firstTickeSocketDto.getShare()) {

            StringBuilder messageBuilder = new StringBuilder();
            messageBuilder.append("Ticket Opened by User: ").append(user.getName()).append("\n")
                    .append(firstTickeSocketDto.getHeader()).append("\n")
                    .append(firstTickeSocketDto.getMessage());


            if (!uploadedUrls.isEmpty()) {
                messageBuilder.append("\nAttachments:\n");
                for (String url : uploadedUrls) {
                    messageBuilder.append("- ").append(url).append("\n");
                }
            }

            slackService.sendMessage(messageBuilder.toString());
        }

        return socketDto;
    }



    @Override
    public TicketDto getTicket(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + id));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !ticket.getUser().getEmail().equals(currentUsername)) {
            throw new AccessDeniedException("You are not allowed to view this ticket");
        }

        return modelMapper.map(ticket, TicketDto.class);
    }

    @Override
    public Page<TicketDto> getAllTickets(int page, int size, String status, String header, String sortByDate, Boolean unread) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentEmail = authentication.getName();

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        Sort sort;
        if (isAdmin) {
            sort = Sort.by("viewedByAdmin").ascending();
        } else {
            sort = Sort.by("viewedByUser").ascending();
        }

        // SECONDARY: createdAt asc/desc
        Sort createdAtSort = Sort.by("createdAt");
        if ("desc".equalsIgnoreCase(sortByDate))
            createdAtSort = createdAtSort.descending();
        else
            createdAtSort = createdAtSort.ascending();


        sort = sort.and(createdAtSort);

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Ticket> tickets;

        if (isAdmin) {
            tickets = ticketRepository.findAll((root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();

                if (status != null && !status.isEmpty())
                    predicates.add(cb.equal(root.get("status"), status));

                if (header != null && !header.isEmpty())
                    predicates.add(cb.like(cb.lower(root.get("header")), "%" + header.toLowerCase() + "%"));

                if (unread != null && !unread) {
                    predicates.add(cb.isFalse(root.get("viewedByAdmin"))); // filter unread for admin
                }

                return cb.and(predicates.toArray(new Predicate[0]));
            }, pageable);
        } else {
            tickets = ticketRepository.findAll((root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(cb.equal(root.get("user").get("email"), currentEmail));

                if (status != null && !status.isEmpty())
                    predicates.add(cb.equal(root.get("status"), status));

                if (header != null && !header.isEmpty())
                    predicates.add(cb.like(cb.lower(root.get("header")), "%" + header.toLowerCase() + "%"));

                if (unread != null && unread) {
                    predicates.add(cb.isFalse(root.get("viewedByUser"))); // filter unread for user
                }

                return cb.and(predicates.toArray(new Predicate[0]));
            }, pageable);
        }

        return tickets.map(ticket -> modelMapper.map(ticket, TicketDto.class));
    }


    @Override
    @Transactional
    public TicketDto updateTicket(TicketDto ticketDto, Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id:" + id));

        Set<String> allowedStatuses = Set.of("pending", "active", "completed");
        if (!allowedStatuses.contains(ticketDto.getStatus().toLowerCase()))
            throw new IllegalArgumentException("Invalid status. Allowed values are: pending, active, completed");

        ticket.setStatus(TicketStatus.valueOf(ticketDto.getStatus()));

        return modelMapper.map(ticketRepository.save(ticket), TicketDto.class);
    }

    @Override
    @Transactional
    public void deleteTicket(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + id));

        ticketRepository.delete(ticket);
    }

}
