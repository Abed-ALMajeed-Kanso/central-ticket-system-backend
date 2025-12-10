package com.example.ticket_system.auth.user.service;

import com.example.ticket_system.auth.dto.NoIdUserDto;
import com.example.ticket_system.auth.dto.UserDto;
import org.springframework.data.domain.Page;

public interface UserService {

    UserDto addUser(UserDto userDto);

    UserDto getUser(Long id);

    public NoIdUserDto updateUser(NoIdUserDto userDto);

    public Page<UserDto> getAllUsers(int page, int size, String sortBy, String sortDir);

    void deleteUser(Long id);

    // void changePassword(String number, Long id);
}
