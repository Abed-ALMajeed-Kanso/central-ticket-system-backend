package com.example.ticket_system.auth.user.service.impl;

import com.example.ticket_system.auth.user.repository.RoleRepository;
import com.example.ticket_system.auth.dto.NoIdUserDto;
import com.example.ticket_system.auth.user.entity.Role;
import lombok.AllArgsConstructor;
import com.example.ticket_system.auth.dto.UserDto;
import com.example.ticket_system.auth.user.entity.User;
import com.example.ticket_system.exception.ResourceNotFoundException;
import com.example.ticket_system.auth.user.repository.UserRepository;
import com.example.ticket_system.auth.user.service.UserService;
import com.example.ticket_system.utils.ValidatorUtil;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private ModelMapper modelMapper;

    @Override
    public UserDto addUser(UserDto userDto) {
        User user = modelMapper.map(userDto, User.class);

        ValidatorUtil.validateEmail(userDto.getEmail(), userRepository);
        ValidatorUtil.validatePassword(userDto.getPassword());
        ValidatorUtil.validateNumber(userDto.getNumber(), userRepository);

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        userDto.setPassword(passwordEncoder.encode(userDto.getPassword()));

        Role user_Role = roleRepository.findByName("ROLE_USER");
        Set<Role> userRole = new HashSet<>();
        userRole.add(user_Role);
        user.setRoles(userRole);

        User savedUser = userRepository.save(user);
        UserDto savedUserDto = modelMapper.map(savedUser, UserDto.class);

        return savedUserDto;
    }

    @Override
    public UserDto getUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id:" + id));

        return modelMapper.map(user, UserDto.class);
    }

    @Override
    public NoIdUserDto updateUser(NoIdUserDto userDto) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Logged-in user not found"));

        user.setName(userDto.getName());
        user.setAddress(userDto.getAddress());

        User updatedUser = userRepository.save(user);

        NoIdUserDto noIduserDto = modelMapper.map(updatedUser, NoIdUserDto.class);

        String roleName = user.getRoles().stream()
                .findFirst()
                .map(Role::getName)
                .orElse("USER");

        noIduserDto.setRole(roleName);

        return noIduserDto;
    }

    @Override
    public Page<UserDto> getAllUsers(int page, int size, String sortBy, String sortDir) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        User loggedInUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Logged-in user not found"));

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<User> usersPage = userRepository.findAll(pageable);

        List<User> filteredUsers = usersPage
                .getContent()
                .stream()
                .filter(u -> !u.getId().equals(loggedInUser.getId()))
                .toList();

        Page<User> filteredPage = new PageImpl<>(
                filteredUsers,
                pageable,
                usersPage.getTotalElements() - 1 // decrease count by one
        );

        return filteredPage.map(user -> modelMapper.map(user, UserDto.class));
    }



    @Override
    public void deleteUser(Long id) {
        User todo = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id : " + id));

        userRepository.deleteById(id);
    }
}
