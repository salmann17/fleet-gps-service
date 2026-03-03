package com.example.fleetgpsservice.controller;

import com.example.fleetgpsservice.dto.GpsRequestDTO;
import com.example.fleetgpsservice.dto.GpsResponseDTO;
import com.example.fleetgpsservice.service.GpsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/gps")
@RequiredArgsConstructor
public class GpsController {

    private final GpsService gpsService;

    @PostMapping
    public ResponseEntity<GpsResponseDTO> recordGpsLog(@Valid @RequestBody GpsRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(gpsService.recordGpsLog(request));
    }
}

