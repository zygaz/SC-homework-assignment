package zz.sc.infrastructure.persistence.repository;

import zz.sc.infrastructure.persistence.entity.AuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface JpaAuditLogRepository extends JpaRepository<AuditLogEntity, Long> {
    Optional<AuditLogEntity> findFirstByRequestIdOrderByCreatedAtDesc(UUID requestId);
}
