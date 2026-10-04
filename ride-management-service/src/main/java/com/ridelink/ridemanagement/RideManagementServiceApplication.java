package com.ridelink.ridemanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/**
 * Main entry point for the RideLink Ride Management Microservice.
 * Owns the independent MongoDB database: ridelink_ride_db (collection: rides).
 */
@SpringBootApplication
@EnableMongoAuditing
public class RideManagementServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RideManagementServiceApplication.class, args);
    }
}
