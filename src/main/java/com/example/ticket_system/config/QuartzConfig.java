package com.example.ticket_system.config;

import com.example.ticket_system.jobs.ExpireTicketsJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {

    @Bean
    public JobDetail expireTicketsJobDetail() {
        return JobBuilder.newJob(ExpireTicketsJob.class)
                .withIdentity("expireTicketsJob")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger expireTicketsJobTrigger() {
        // Cron expression: run every day at 00:00
           CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule("0 0 0 * * ?");
        // every 5 sec testing
        // CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule("*/5 * * * * ?");

        return TriggerBuilder.newTrigger()
                .forJob(expireTicketsJobDetail())
                .withIdentity("expireTicketsTrigger")
                .withSchedule(scheduleBuilder)
                .build();
    }
}

