package com.example.ticket_system.controller;
import com.example.ticket_system.exception.ticket_systemAPIException;
import com.example.ticket_system.integration.slack.SlackService;
import lombok.AllArgsConstructor;
import com.example.ticket_system.dto.MessageSocketDto;
import com.example.ticket_system.dto.TicketMessageDto;
import com.example.ticket_system.service.TicketMessageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("api/tickets")
@AllArgsConstructor
public class MessageController {

    private TicketMessageService ticketMessageService;

    private final SlackService slackService;

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    //@PostMapping("/{ticketId}/messages")
    //public ResponseEntity<MessageSocketDto> addMessageToTicket(@PathVariable Long ticketId,
    //        @RequestBody TicketMessageDto ticketMessageDto) {
    //    MessageSocketDto responseDto = ticketMessageService.addTicketMessage(ticketMessageDto, ticketId);
    //    return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    //}

    @PostMapping(
            value = "/{ticketId}/messages",
            consumes = "multipart/form-data"
    )
    public ResponseEntity<MessageSocketDto> addMessageToTicket(
            @PathVariable Long ticketId,
            @RequestPart("message") String message,
            @RequestPart("share") String share,
            @RequestPart(value = "files", required = false) List<MultipartFile> files){

        TicketMessageDto dto = new TicketMessageDto();
        dto.setMessage(message);
        if(share.equals("true"))
            dto.setShare(true);
        dto.setFiles(files);

        try {
            MessageSocketDto responseDto = ticketMessageService.addTicketMessage(dto, ticketId);
            return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
        } catch (InterruptedException e) {
            throw new ticket_systemAPIException(HttpStatus.NOT_FOUND, "Slack message sending was interrupted");
        }
    }



    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/{ticketId}/messages")
    public ResponseEntity<List<MessageSocketDto>> getTicketMessages(
            @PathVariable Long ticketId,
            @RequestParam(required = false) String messageQuery,
            @RequestParam(required = false) String attachmentQuery
    ) {
        List<MessageSocketDto> messages =
                ticketMessageService.getTicketMessages(ticketId, messageQuery, attachmentQuery);

        return ResponseEntity.ok(messages);
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PostMapping("/{ticketId}/view")
    public ResponseEntity<Void> updateViewStatus(
            @PathVariable Long ticketId
    ) {
        ticketMessageService.viewedBySender(ticketId);
        return ResponseEntity.ok().build();
    }
}
