package com.example.ticket_system.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TicketDto {
    private Long id;
    private String header;
    private String status;
    private Long userId;
    private Long adminId;
    private Boolean viewedByAdmin;
    private Boolean viewedByUser;
}
