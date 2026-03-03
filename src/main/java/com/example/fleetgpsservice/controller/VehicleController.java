package com.example.fleetgpsservice.controller;

import com.example.fleetgpsservice.dto.GpsResponseDTO;
import com.example.fleetgpsservice.service.GpsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final GpsService gpsService;

    @GetMapping("/{id}/last-location")
    public ResponseEntity<GpsResponseDTO> getLastLocation(@PathVariable Long id) {
        return ResponseEntity.ok(gpsService.getLastLocation(id));
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<Page<GpsResponseDTO>> getHistory(
            @PathVariable Long id,
            @RequestParam Instant from,
            @RequestParam Instant to,
            @PageableDefault(size = 20, sort = "timestamp") Pageable pageable) {
        return ResponseEntity.ok(gpsService.getHistory(id, from, to, pageable));
    }
}


