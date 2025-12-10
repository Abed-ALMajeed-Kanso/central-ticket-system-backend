package com.example.ticket_system.service;

import com.example.ticket_system.dto.FirstTicketSocketDto;
import com.example.ticket_system.dto.TicketDto;
import org.springframework.data.domain.Page;

public interface TicketService {
    FirstTicketSocketDto addTicket(FirstTicketSocketDto firstTickeSocketDto) throws InterruptedException;

    TicketDto getTicket(Long id);

    public Page<TicketDto> getAllTickets(int page, int size, String status, String header, String sortByDate, Boolean unread);

    TicketDto updateTicket(TicketDto ticketDtoDto, Long id);

    void deleteTicket(Long id);
}
