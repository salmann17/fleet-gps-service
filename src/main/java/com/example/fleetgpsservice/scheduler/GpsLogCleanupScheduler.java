package com.example.fleetgpsservice.scheduler;

import com.example.fleetgpsservice.repository.GpsLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class GpsLogCleanupScheduler {

    private final GpsLogRepository gpsLogRepository;

    @Value("${app.cleanup.retention-days:30}")
    private int retentionDays;

    @Transactional
    @Scheduled(cron = "${app.cleanup.cron:0 0 2 * * *}")
    public void cleanUpOldLogs() {
        Instant cutoff = Instant.now().minus(retentionDays, ChronoUnit.DAYS);
        int deleted = gpsLogRepository.deleteByTimestampBefore(cutoff);
        log.info("GPS log cleanup: deleted {} records older than {} days", deleted, retentionDays);
    }
}

