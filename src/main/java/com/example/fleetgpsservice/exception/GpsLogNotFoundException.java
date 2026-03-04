package com.example.fleetgpsservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class GpsLogNotFoundException extends RuntimeException {

    public GpsLogNotFoundException(Long vehicleId) {
        super("No GPS logs found for vehicle id: " + vehicleId);
    }
}

