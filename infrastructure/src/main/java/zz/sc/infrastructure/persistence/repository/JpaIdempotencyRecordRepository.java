package zz.sc.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import zz.sc.infrastructure.persistence.entity.IdempotencyRecordEntity;

import java.util.UUID;
import java.time.Instant;

public interface JpaIdempotencyRecordRepository extends JpaRepository<IdempotencyRecordEntity, UUID> {
    long deleteByCreatedAtBefore(Instant timestamp);
}
