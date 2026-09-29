package zz.sc.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class IdempotencyCleanupScheduler {

    private final JpaIdempotencyRecordRepository repository;

    @Scheduled(fixedRateString = "${idempotency.cleanup.rate:3600000}") // Default 1 hour
    @Transactional
    public void cleanupOldIdempotencyRecords() {
        Instant threshold = Instant.now().minus(24, ChronoUnit.HOURS);
        long deletedCount = repository.deleteByCreatedAtBefore(threshold);
        if (deletedCount > 0) {
            log.info("Deleted {} old idempotency records (older than {})", deletedCount, threshold);
        }
    }
}
