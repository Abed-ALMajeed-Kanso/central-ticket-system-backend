package com.example.ticket_system.config.Seeders;
import com.example.ticket_system.auth.user.entity.User;
import com.example.ticket_system.entity.*;
import com.example.ticket_system.auth.user.repository.UserRepository;
import com.example.ticket_system.integration.aws.S3Service;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(2)
public class TicketSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final S3Service s3Service;

    public TicketSeeder(UserRepository userRepository, S3Service s3Service) {
        this.userRepository = userRepository;
        this.s3Service = s3Service;
    }

    @Override
    @Transactional
    public void run(String... args) {

        if (userRepository.count() == 0) {
            System.out.println("No users found. Skipping TicketSeeder.");
            return;
        }

        boolean ticketsExist = userRepository.findAll()
                .stream()
                .anyMatch(user -> user.getTickets() != null && !user.getTickets().isEmpty());

        if (ticketsExist) {
            System.out.println("Tickets already exist. Skipping TicketSeeder.");
            return;
        }

        User admin = userRepository.findByEmail("admin@example.com")
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        userRepository.findAll().forEach(user -> {

            // Skip admin
            if (user.getEmail().equalsIgnoreCase("admin@example.com")) {
                return;
            }

            for (int t = 1; t <= 2; t++) {

                Ticket ticket = new Ticket();
                ticket.setHeader("Ticket " + t + " for user " + user.getName());
                ticket.setUser(user);
                ticket.setAdmin(admin);

                // Create 2 messages per ticket
                for (int m = 1; m <= 2; m++) {

                    TicketMessage message = new TicketMessage();

                    if (m == 1) {
                        message.setMessage("User opened a new ticket");
                        message.setSender(user);
                    } else {
                        message.setMessage("Admin replied on opened ticket");
                        message.setSender(admin);
                    }

                    message.setTicket(ticket);

                    for (int a = 1; a <= 2; a++) {
                        TicketMessageAttachment attachment = new TicketMessageAttachment();
                        attachment.setAttachment(s3Service.getPublicUrl("default.pdf"));
                        attachment.setTicketMessage(message);
                        message.getAttachments().add(attachment);
                    }

                    ticket.getMessages().add(message);
                }

                user.getTickets().add(ticket);
            }

            userRepository.save(user);
        });

        System.out.println("---- Tickets, Messages & Attachments Seeded Successfully ----");
    }
}

