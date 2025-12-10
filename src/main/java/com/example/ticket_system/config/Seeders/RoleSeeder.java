package com.example.ticket_system.config.Seeders;

import com.example.ticket_system.auth.user.entity.Role;
import com.example.ticket_system.auth.user.entity.User;
import com.example.ticket_system.auth.user.repository.RoleRepository;
import com.example.ticket_system.auth.user.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@Order(1)
public class RoleSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    public RoleSeeder(RoleRepository roleRepository, UserRepository userRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (roleRepository.count() == 0 && userRepository.count() == 0) {
            seedRolesAndUsers();
            System.out.println("--- Roles and Users Seeded ---");
        } else {
            System.out.println("--- Roles or Users already exist. Skipping Seeder ---");
        }
    }

    private void seedRolesAndUsers() {
        // --- 1. Create roles ---
        roleRepository.save(new Role("ROLE_ADMIN")); // ID will usually be 1
        roleRepository.save(new Role("ROLE_USER"));   // ID will usually be 2

        Role user_Role = roleRepository.findByName("ROLE_USER");
        Role admin_Role = roleRepository.findByName("ROLE_ADMIN");

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        // --- 2. Create users ---
        String PLACEHOLDER_PASSWORD = passwordEncoder.encode("Password123@"); // Optionally encode if using Spring Security

        User adminUser = new User();
        adminUser.setName("Super Admin");
        adminUser.setNumber("+961 70 123 456");
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword(PLACEHOLDER_PASSWORD);
        adminUser.setAddress("Beirut");
        Set<Role> adminRole = new HashSet<>();
        adminRole.add(admin_Role);
        adminUser.setRoles(adminRole);
        adminUser.setCreatedBy("seeding");
        userRepository.save(adminUser);

        String[][] usersData = {
                {"John Doe", "+961 70 123 456", "user1@example.com"},
                {"Sara Smith", "+961 71 222 333", "user2@example.com"},
                {"Mike Johnson", "+961 76 444 555", "user3@example.com"},
                {"Lina Haddad", "+961 78 666 777", "user4@example.com"},
                {"Omar Khalil", "+961 79 888 999", "user5@example.com"}
        };

        for (String[] u : usersData) {
            User user = new User();
            user.setName(u[0]);
            user.setNumber(u[1]);
            user.setEmail(u[2]);
            user.setPassword(PLACEHOLDER_PASSWORD);
            user.setAddress("Beirut");
            Set<Role> userRole = new HashSet<>();
            userRole.add(user_Role);
            user.setRoles(userRole);
            user.setCreatedBy("seeding");
            userRepository.save(user);
        }

        System.out.println("--- Users roles manually inserted into users_roles table ---");
    }
}