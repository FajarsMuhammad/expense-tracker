package com.fajars.expensetracker.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.TimeZone;

/**
 * Jackson configuration for consistent timezone handling.
 * Ensures all date/time serialization uses Asia/Jakarta timezone.
 */
@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Register Java 8 Date/Time module
        mapper.registerModule(new JavaTimeModule());

        // Set timezone to Asia/Jakarta
        mapper.setTimeZone(TimeZone.getTimeZone("Asia/Jakarta"));

        // Disable writing dates as timestamps
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return mapper;
    }
}
