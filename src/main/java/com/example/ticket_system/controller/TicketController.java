package com.example.ticket_system.controller;

import com.example.ticket_system.dto.FirstTicketSocketDto;
import com.example.ticket_system.exception.ticket_systemAPIException;
import lombok.AllArgsConstructor;
import com.example.ticket_system.dto.TicketDto;
import com.example.ticket_system.service.TicketService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("api/tickets")
@AllArgsConstructor
public class TicketController {

    private TicketService ticketService;

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("{id}")
    public ResponseEntity<TicketDto> getTicket(@PathVariable("id") Long ticketId){
        TicketDto ticketDto = ticketService.getTicket(ticketId);
        return new ResponseEntity<>(ticketDto, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping
    public Page<TicketDto> getAllTickets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String header,
            @RequestParam(defaultValue = "desc") String sortByDate,
            @RequestParam(required = false) Boolean unread
    ) {
        return ticketService.getAllTickets(page, size, status, header, sortByDate, unread);
    }

    @PreAuthorize("hasRole('USER')")
//    @PostMapping
//    public ResponseEntity<FirstTicketSocketDto> addTicket(@RequestBody FirstTicketSocketDto firstTicketDto){
//
//        FirstTicketSocketDto savedTicket = ticketService.addTicket(firstTicketDto);
//        return new ResponseEntity<>(savedTicket, HttpStatus.CREATED);
//    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<FirstTicketSocketDto> addTicket(
            @RequestPart("header") String header,
            @RequestPart("message") String message,
            @RequestPart("share") String share,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {

        FirstTicketSocketDto dto = new FirstTicketSocketDto();
        dto.setHeader(header);
        dto.setMessage(message);
        if(share.equals("true"))
            dto.setShare(true);
        dto.setFiles(files);
        dto.setTimestamp(LocalDateTime.now());

        try {
            FirstTicketSocketDto saved = ticketService.addTicket(dto);
            return new ResponseEntity<>(saved, HttpStatus.CREATED);
        } catch (InterruptedException e) {
            throw new ticket_systemAPIException(HttpStatus.NOT_FOUND, "Slack message sending was interrupted");
        }
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("{id}")
    public ResponseEntity<TicketDto> updateTicket(@RequestBody TicketDto ticketDto, @PathVariable("id") Long ticketId){
        TicketDto updatedTicket = ticketService.updateTicket(ticketDto, ticketId);
        return ResponseEntity.ok(updatedTicket);
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("{id}")
    public ResponseEntity<String> deleteTicket(@PathVariable("id") Long ticketId){
        ticketService.deleteTicket(ticketId);
        return ResponseEntity.ok("Ticket deleted successfully!.");
    }
}
