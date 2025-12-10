package com.example.ticket_system.utils;

import com.example.ticket_system.exception.ticket_systemAPIException;
import com.example.ticket_system.auth.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;


@Component
public class ValidatorUtil {

    public static void validateEmail(String email, UserRepository userRepository) {

        if(userRepository.existsByEmail(email))
        throw new ticket_systemAPIException(HttpStatus.BAD_REQUEST, "Email already exists!.");

        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        if (!email.matches(emailRegex))
            throw new IllegalArgumentException("Invalid email format");
}

    public static void validatePassword(String password) {
        if (password.length() < 8 ||
                !password.matches(".*[A-Z].*") ||
                !password.matches(".*[a-zA-Z].*") ||
                !password.matches(".*\\d.*") ||
                !password.matches(".*[!@#$%^&*()].*")
        ) {
            throw new IllegalArgumentException("Password must be at least 8 characters, contain one uppercase letter, one number, and one special character");
        }
    }

    public static void validateNumber(String number, UserRepository userRepository) {
        if(userRepository.existsByEmail(number))
            throw new ticket_systemAPIException(HttpStatus.BAD_REQUEST, "Number already exists!.");

        String numberRegex = "^\\+?[0-9]{10,15}$";
        if (!number.matches(numberRegex))
            throw new IllegalArgumentException("Invalid phone number format");
    }
}
