package com.example.ticket_system.jobs;

import com.example.ticket_system.entity.Ticket;
import com.example.ticket_system.entity.TicketStatus;
import com.example.ticket_system.repository.TicketRepository;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class ExpireTicketsJob implements Job {

    @Autowired
    private TicketRepository ticketRepository;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        // LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1);

        List<Ticket> ticketsToExpire = ticketRepository.findAllByStatusAndCreatedAtBefore(
                "pending",
                oneWeekAgo
        );

        for (Ticket ticket : ticketsToExpire) {
            ticket.setStatus(TicketStatus.valueOf("urgent"));
        }

        ticketRepository.saveAll(ticketsToExpire);
    }
}

