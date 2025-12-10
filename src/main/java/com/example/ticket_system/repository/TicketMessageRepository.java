package com.example.ticket_system.repository;

import com.example.ticket_system.entity.TicketMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketMessageRepository extends JpaRepository<TicketMessage, Long> {

}