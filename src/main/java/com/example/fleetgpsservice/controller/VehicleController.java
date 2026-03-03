package com.example.fleetgpsservice.controller;

import com.example.fleetgpsservice.dto.GpsResponseDTO;
import com.example.fleetgpsservice.service.GpsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final GpsService gpsService;

    @GetMapping("/{id}/last-location")
    public ResponseEntity<GpsResponseDTO> getLastLocation(@PathVariable Long id) {
        return ResponseEntity.ok(gpsService.getLastLocation(id));
    }
}

