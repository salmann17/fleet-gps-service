package com.example.fleetgpsservice.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record GpsRequestDTO(

        @NotNull
        Long vehicleId,

        @NotNull
        @DecimalMin(value = "-90.0")
        @DecimalMax(value = "90.0")
        Double latitude,

        @NotNull
        @DecimalMin(value = "-180.0")
        @DecimalMax(value = "180.0")
        Double longitude,

        @NotNull
        @DecimalMin(value = "0.0")
        Double speed,

        @NotNull
        Instant timestamp
) {}


