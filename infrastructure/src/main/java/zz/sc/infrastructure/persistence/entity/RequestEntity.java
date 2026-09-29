package zz.sc.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import zz.sc.model.ActionReason;
import zz.sc.model.RequestState;

import java.util.UUID;

@Entity
@Table(name = "requests")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RequestEntity {
    @Id
    private UUID id;
    @Version
    private Long version;
    @Column(nullable = false, length = 255)
    private String name;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private RequestState state;
    @Column(name = "action_reason", columnDefinition = "TEXT")
    @Enumerated(EnumType.STRING)
    private ActionReason actionReason;
    @Column(name = "sequence_number", unique = true)
    private Long sequenceNumber;
}
