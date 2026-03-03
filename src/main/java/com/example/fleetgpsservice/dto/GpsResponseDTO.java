package com.example.fleetgpsservice.dto;

import java.time.Instant;

public record GpsResponseDTO(
        Long id,
        Long vehicleId,
        double latitude,
        double longitude,
        double speed,
        Instant timestamp,
        boolean speedViolation
) {}

