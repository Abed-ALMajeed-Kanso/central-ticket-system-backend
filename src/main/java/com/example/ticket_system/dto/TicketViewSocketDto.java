package com.example.ticket_system.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

public record TicketViewSocketDto(
        Long ticketId,
        Boolean viewedByUser,
        Boolean viewedByAdmin
) {}
