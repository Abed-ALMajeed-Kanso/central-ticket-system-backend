package com.example.ticket_system.auth.user.controller;

import com.example.ticket_system.auth.dto.NoIdUserDto;
import lombok.AllArgsConstructor;
import com.example.ticket_system.auth.dto.UserDto;
import com.example.ticket_system.auth.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RestController
@RequestMapping("api/users")
@AllArgsConstructor
public class UserController {

    private UserService userService;

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PutMapping
    public ResponseEntity<NoIdUserDto> updateUser(@RequestBody NoIdUserDto UserDto){
        NoIdUserDto updatedUser = userService.updateUser(UserDto);
        return ResponseEntity.ok(updatedUser);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<UserDto> addUser(@RequestBody UserDto UserDto){

        UserDto savedUser = userService.addUser(UserDto);
        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("{id}")
    public ResponseEntity<UserDto> getUser(@PathVariable("id") Long UserId){
        UserDto UserDto = userService.getUser(UserId);
        return new ResponseEntity<>(UserDto, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public Page<UserDto> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        return userService.getAllUsers(page, size, sortBy, sortDir);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("{id}")
    public ResponseEntity<String> deleteUser(@PathVariable("id") Long UserId){
        userService.deleteUser(UserId);
        return ResponseEntity.ok("User deleted successfully!.");
    }

}