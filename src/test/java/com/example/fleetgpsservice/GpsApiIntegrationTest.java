package com.example.fleetgpsservice;

import com.example.fleetgpsservice.entity.GpsLog;
import com.example.fleetgpsservice.entity.Vehicle;
import com.example.fleetgpsservice.repository.GpsLogRepository;
import com.example.fleetgpsservice.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GpsApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private GpsLogRepository gpsLogRepository;

    private Vehicle testVehicle;

    @BeforeEach
    void setUp() {
        gpsLogRepository.deleteAll();
        vehicleRepository.deleteAll();

        testVehicle = vehicleRepository.save(
                Vehicle.builder()
                        .plateNumber("B1234XYZ")
                        .name("Truck A")
                        .type("TRUCK")
                        .build()
        );
    }

    @Test
    void postGps_validRequest_returnsCreated() throws Exception {
        String json = """
                {
                  "vehicleId": %d,
                  "latitude": -6.200000,
                  "longitude": 106.816666,
                  "speed": 85,
                  "timestamp": "2025-01-01T10:00:00Z"
                }
                """.formatted(testVehicle.getId());

        mockMvc.perform(post("/api/gps")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.vehicleId").value(testVehicle.getId()))
                .andExpect(jsonPath("$.latitude").value(-6.2))
                .andExpect(jsonPath("$.longitude").value(106.816666))
                .andExpect(jsonPath("$.speed").value(85))
                .andExpect(jsonPath("$.speedViolation").value(false));
    }

    @Test
    void postGps_speedAboveThreshold_flagsViolation() throws Exception {
        String json = """
                {
                  "vehicleId": %d,
                  "latitude": -6.200000,
                  "longitude": 106.816666,
                  "speed": 150,
                  "timestamp": "2025-01-01T10:00:00Z"
                }
                """.formatted(testVehicle.getId());

        mockMvc.perform(post("/api/gps")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.speed").value(150))
                .andExpect(jsonPath("$.speedViolation").value(true));
    }

    @Test
    void postGps_invalidLatitude_returnsBadRequest() throws Exception {
        String json = """
                {
                  "vehicleId": %d,
                  "latitude": 95.0,
                  "longitude": 106.0,
                  "speed": 50,
                  "timestamp": "2025-01-01T10:00:00Z"
                }
                """.formatted(testVehicle.getId());

        mockMvc.perform(post("/api/gps")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postGps_invalidLongitude_returnsBadRequest() throws Exception {
        String json = """
                {
                  "vehicleId": %d,
                  "latitude": -6.0,
                  "longitude": 200.0,
                  "speed": 50,
                  "timestamp": "2025-01-01T10:00:00Z"
                }
                """.formatted(testVehicle.getId());

        mockMvc.perform(post("/api/gps")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postGps_negativeSpeed_returnsBadRequest() throws Exception {
        String json = """
                {
                  "vehicleId": %d,
                  "latitude": -6.0,
                  "longitude": 106.0,
                  "speed": -10,
                  "timestamp": "2025-01-01T10:00:00Z"
                }
                """.formatted(testVehicle.getId());

        mockMvc.perform(post("/api/gps")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postGps_vehicleNotFound_returnsNotFound() throws Exception {
        String json = """
                {
                  "vehicleId": 99999,
                  "latitude": -6.0,
                  "longitude": 106.0,
                  "speed": 50,
                  "timestamp": "2025-01-01T10:00:00Z"
                }
                """;

        mockMvc.perform(post("/api/gps")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    void getLastLocation_returnsLatestLog() throws Exception {
        gpsLogRepository.save(GpsLog.builder()
                .vehicle(testVehicle)
                .latitude(-6.1).longitude(106.1).speed(40)
                .timestamp(Instant.parse("2025-01-01T08:00:00Z"))
                .speedViolation(false)
                .build());

        gpsLogRepository.save(GpsLog.builder()
                .vehicle(testVehicle)
                .latitude(-6.2).longitude(106.2).speed(60)
                .timestamp(Instant.parse("2025-01-01T12:00:00Z"))
                .speedViolation(false)
                .build());

        mockMvc.perform(get("/api/vehicles/{id}/last-location", testVehicle.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.latitude").value(-6.2))
                .andExpect(jsonPath("$.longitude").value(106.2))
                .andExpect(jsonPath("$.speed").value(60))
                .andExpect(jsonPath("$.timestamp").value("2025-01-01T12:00:00Z"));
    }

    @Test
    void getLastLocation_vehicleNotFound_returnsNotFound() throws Exception {
        mockMvc.perform(get("/api/vehicles/{id}/last-location", 99999))
                .andExpect(status().isNotFound());
    }

    @Test
    void getLastLocation_noLogs_returnsNotFound() throws Exception {
        mockMvc.perform(get("/api/vehicles/{id}/last-location", testVehicle.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getHistory_returnsLogsWithinRange() throws Exception {
        gpsLogRepository.save(GpsLog.builder()
                .vehicle(testVehicle)
                .latitude(-6.1).longitude(106.1).speed(40)
                .timestamp(Instant.parse("2025-01-01T08:00:00Z"))
                .speedViolation(false)
                .build());

        gpsLogRepository.save(GpsLog.builder()
                .vehicle(testVehicle)
                .latitude(-6.2).longitude(106.2).speed(60)
                .timestamp(Instant.parse("2025-01-01T12:00:00Z"))
                .speedViolation(false)
                .build());

        gpsLogRepository.save(GpsLog.builder()
                .vehicle(testVehicle)
                .latitude(-6.3).longitude(106.3).speed(80)
                .timestamp(Instant.parse("2025-01-02T08:00:00Z"))
                .speedViolation(false)
                .build());

        mockMvc.perform(get("/api/vehicles/{id}/history", testVehicle.getId())
                        .param("from", "2025-01-01T00:00:00Z")
                        .param("to", "2025-01-01T23:59:59Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].latitude").value(-6.1))
                .andExpect(jsonPath("$.content[1].latitude").value(-6.2));
    }

    @Test
    void getHistory_vehicleNotFound_returnsNotFound() throws Exception {
        mockMvc.perform(get("/api/vehicles/{id}/history", 99999)
                        .param("from", "2025-01-01T00:00:00Z")
                        .param("to", "2025-01-01T23:59:59Z"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getHistory_noLogs_returnsEmptyPage() throws Exception {
        mockMvc.perform(get("/api/vehicles/{id}/history", testVehicle.getId())
                        .param("from", "2025-01-01T00:00:00Z")
                        .param("to", "2025-01-01T23:59:59Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    void getHistory_fromAfterTo_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/vehicles/{id}/history", testVehicle.getId())
                        .param("from", "2025-01-02T00:00:00Z")
                        .param("to", "2025-01-01T00:00:00Z"))
                .andExpect(status().isBadRequest());
    }
}


