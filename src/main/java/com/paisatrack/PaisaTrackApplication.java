package com.paisatrack;

import jakarta.annotation.PostConstruct;
import java.util.TimeZone;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the PaisaTrack Spring Boot application.
 * Sets the JVM default time zone to Asia/Kolkata (IST) for consistent Indian date handling.
 */
@SpringBootApplication
public class PaisaTrackApplication {

    @PostConstruct
    public void configureTimeZone() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
    }

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
        SpringApplication.run(PaisaTrackApplication.class, args);
    }
}
