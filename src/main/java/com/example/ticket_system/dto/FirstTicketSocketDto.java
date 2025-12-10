package com.example.ticket_system.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Data
public class FirstTicketSocketDto {

    private Long id;
    private String header;
    private String message;
    private String status = "pending";
    private Long userId;
    private Long adminId;
    private Boolean viewedByAdmin = false;
    private Boolean viewedByUser = true;
    private Boolean share = false;
    private List<MultipartFile> files;
    private List<String> attachments;
    private LocalDateTime timestamp;

    public FirstTicketSocketDto(String header,
                                String message,
                                List<MultipartFile> files,
                                Boolean share) {
        this.header = header;
        this.message = message;
        this.files = files;
        this.share = share;
    }

    public FirstTicketSocketDto(Long id,
                                String header,
                                String message,
                                String status,
                                Long userId,
                                Long adminId,
                                Boolean viewedByAdmin,
                                Boolean viewedByUser,
                                List<String> attachments,
                                LocalDateTime timestamp) {
        this.id = id;
        this.header = header;
        this.message = message;
        this.status = status;
        this.userId = userId;
        this.adminId = adminId;
        this.viewedByAdmin = viewedByAdmin;
        this.viewedByUser = viewedByUser;
        this.attachments = attachments;
        this.timestamp = timestamp;
    }
}
