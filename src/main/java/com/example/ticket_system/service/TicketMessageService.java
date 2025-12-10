package com.example.ticket_system.service;

import com.example.ticket_system.dto.MessageSocketDto;
import com.example.ticket_system.dto.TicketMessageDto;

import java.util.List;

public interface TicketMessageService {

    MessageSocketDto addTicketMessage(TicketMessageDto ticketMessageDto, Long ticketId) throws InterruptedException;

    List<MessageSocketDto> getTicketMessages(Long ticketId, String messageQuery, String attachmentQuery);

    void viewedBySender(Long ticketId);
}
