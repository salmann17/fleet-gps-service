package com.example.fleetgpsservice.service;

import com.example.fleetgpsservice.dto.GpsRequestDTO;
import com.example.fleetgpsservice.dto.GpsResponseDTO;
import com.example.fleetgpsservice.entity.GpsLog;
import com.example.fleetgpsservice.entity.Vehicle;
import com.example.fleetgpsservice.exception.GpsLogNotFoundException;
import com.example.fleetgpsservice.exception.InvalidDateRangeException;
import com.example.fleetgpsservice.exception.VehicleNotFoundException;
import com.example.fleetgpsservice.repository.GpsLogRepository;
import com.example.fleetgpsservice.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class GpsService {

    private static final double SPEED_VIOLATION_THRESHOLD = 100.0;

    private final VehicleRepository vehicleRepository;
    private final GpsLogRepository gpsLogRepository;

    @Transactional
    public GpsResponseDTO recordGpsLog(GpsRequestDTO request) {
        Vehicle vehicle = vehicleRepository.findById(request.vehicleId())
                .orElseThrow(() -> new VehicleNotFoundException(request.vehicleId()));

        GpsLog gpsLog = GpsLog.builder()
                .vehicle(vehicle)
                .latitude(request.latitude())
                .longitude(request.longitude())
                .speed(request.speed())
                .timestamp(request.timestamp())
                .speedViolation(request.speed() > SPEED_VIOLATION_THRESHOLD)
                .build();

        GpsLog saved = gpsLogRepository.save(gpsLog);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public GpsResponseDTO getLastLocation(Long vehicleId) {
        if (!vehicleRepository.existsById(vehicleId)) {
            throw new VehicleNotFoundException(vehicleId);
        }
        GpsLog log = gpsLogRepository.findTopByVehicleIdOrderByTimestampDesc(vehicleId)
                .orElseThrow(() -> new GpsLogNotFoundException(vehicleId));
        return toResponse(log);
    }

    @Transactional(readOnly = true)
    public Page<GpsResponseDTO> getHistory(Long vehicleId, Instant from, Instant to, Pageable pageable) {
        if (from.isAfter(to)) {
            throw new InvalidDateRangeException();
        }
        if (!vehicleRepository.existsById(vehicleId)) {
            throw new VehicleNotFoundException(vehicleId);
        }
        return gpsLogRepository
                .findByVehicleIdAndTimestampBetween(vehicleId, from, to, pageable)
                .map(this::toResponse);
    }

    private GpsResponseDTO toResponse(GpsLog log) {
        return new GpsResponseDTO(
                log.getId(),
                log.getVehicle().getId(),
                log.getLatitude(),
                log.getLongitude(),
                log.getSpeed(),
                log.getTimestamp(),
                log.isSpeedViolation()
        );
    }
}

