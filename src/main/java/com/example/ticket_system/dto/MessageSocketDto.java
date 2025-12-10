package com.example.ticket_system.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MessageSocketDto {
    private Long id;
    private String message;
    private Long senderId;
    private List<String> attachments;
    private LocalDateTime createdAt;
}
