package zz.sc.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;


@Entity
@Table(name = "idempotency_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IdempotencyRecordEntity {
    @Id
    @Column(name = "idempotency_key")
    private UUID idempotencyKey;
    @Column(name = "response_status")
    private Integer responseStatus;
    @Column(name = "response_body", columnDefinition = "TEXT")
    private String responseBody;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
