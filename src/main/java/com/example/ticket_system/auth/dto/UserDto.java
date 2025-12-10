package com.example.ticket_system.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Long id;
    private String name;
    private String number;
    private String email;
    private String address;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
}
