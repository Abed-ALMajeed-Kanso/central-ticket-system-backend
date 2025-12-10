package com.example.ticket_system.integration.slack;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class SlackService {

    @Value("${slack.webhook.url}")
    private String webhookUrl;

    private final RestTemplate restTemplate = new RestTemplate();


    public void sendMessage(String message) throws InterruptedException {
        Thread.sleep(1500);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> payload = new HashMap<>();
        payload.put("text", message);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(payload, headers);
        restTemplate.postForEntity(webhookUrl, request, String.class);
    }
}

//import java.util.HashMap;
//import java.util.Map;
//
//@Service
//public class SlackService {
//
//    @Value("${slack.webhook.url}")
//    private String webhookUrl;
//
//    private final UserRepository userRepository;
//    private final RestTemplate restTemplate = new RestTemplate();
//
//    // Constructor injection ensures userRepository is not null
//    public SlackService(UserRepository userRepository) {
//        this.userRepository = userRepository;
//    }
//
//    public void sendMessage(SlackMessageDto slackMessageDto) {
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        String email = auth.getName();
//
//        User user = userRepository.findByEmail(email)
//                .orElseThrow(() -> new ResourceNotFoundException("Logged-in user not found"));
//
//        String roleName = user.getRoles().stream()
//                .findFirst()
//                .map(Role::getName)
//                .orElse("USER");
//
//        StringBuilder messageBuilder = new StringBuilder();
//
//        if ("ROLE_ADMIN".equals(roleName)) {
//            messageBuilder.append("Reply on Ticket \"")
//                    .append(slackMessageDto.getHeader())
//                    .append("\" by Admin:\n")
//                    .append(slackMessageDto.getMessage());
//
//        } else if ("ROLE_USER".equals(roleName)) {
//            messageBuilder.append("Ticket Opened by User: ").append(user.getName()).append("\n")
//                    .append(slackMessageDto.getHeader()).append("\n")
//                    .append(slackMessageDto.getMessage());
//        }
//
//        if (slackMessageDto.getAttachments() != null && !slackMessageDto.getAttachments().isEmpty()) {
//            messageBuilder.append("\nAttachments: ")
//                    .append(String.join(", ", slackMessageDto.getAttachments()));
//        }
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//
//        Map<String, String> payload = new HashMap<>();
//        payload.put("text", messageBuilder.toString());
//
//        HttpEntity<Map<String, String>> request = new HttpEntity<>(payload, headers);
//
//        // Optional: log payload before sending for debugging
//        System.out.println("Sending Slack message: " + payload);
//
//        restTemplate.postForEntity(webhookUrl, request, String.class);
//    }
//}
