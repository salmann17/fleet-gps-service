package com.example.fleetgpsservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
    name = "gps_log",
    indexes = {
        @Index(name = "idx_gps_log_vehicle_id",  columnList = "vehicle_id"),
        @Index(name = "idx_gps_log_timestamp",    columnList = "timestamp"),
        @Index(name = "idx_gps_log_vehicle_ts",   columnList = "vehicle_id, timestamp")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GpsLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "vehicle_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_gps_log_vehicle")
    )
    private Vehicle vehicle;

    @Column(name = "latitude", nullable = false)
    private double latitude;

    @Column(name = "longitude", nullable = false)
    private double longitude;

    @Column(name = "speed", nullable = false)
    private double speed;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    @Column(name = "speed_violation", nullable = false)
    @Builder.Default
    private boolean speedViolation = false;
}

