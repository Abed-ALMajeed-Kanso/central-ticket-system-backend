package com.example.ticket_system.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public class ticket_systemAPIException extends RuntimeException{
    private HttpStatus status;
    private String message;
}
