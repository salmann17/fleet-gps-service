package com.example.fleetgpsservice.repository;

import com.example.fleetgpsservice.entity.GpsLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface GpsLogRepository extends JpaRepository<GpsLog, Long> {

    Optional<GpsLog> findTopByVehicleIdOrderByTimestampDesc(Long vehicleId);

    List<GpsLog> findByVehicleIdAndTimestampBetween(Long vehicleId, Instant from, Instant to);

    Page<GpsLog> findByVehicleIdAndTimestampBetween(Long vehicleId, Instant from, Instant to, Pageable pageable);
}

