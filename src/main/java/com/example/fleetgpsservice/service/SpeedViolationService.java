package com.example.fleetgpsservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SpeedViolationService {

    @Value("${app.speed.violation-threshold-kmh:100}")
    private double threshold;

    public boolean isViolation(double speed) {
        return speed > threshold;
    }
}

