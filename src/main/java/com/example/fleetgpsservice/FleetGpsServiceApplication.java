package com.example.fleetgpsservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FleetGpsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FleetGpsServiceApplication.class, args);
    }

}
