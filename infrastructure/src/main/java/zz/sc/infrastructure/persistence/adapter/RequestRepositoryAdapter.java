package zz.sc.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import zz.sc.infrastructure.persistence.entity.AuditLogEntity;
import zz.sc.infrastructure.persistence.entity.RequestEntity;
import zz.sc.infrastructure.persistence.repository.JpaAuditLogRepository;
import zz.sc.infrastructure.persistence.repository.JpaRequestRepository;
import zz.sc.model.PageQuery;
import zz.sc.model.PageResult;
import zz.sc.model.Request;
import zz.sc.model.RequestState;
import zz.sc.port.RequestRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RequestRepositoryAdapter implements RequestRepository {

    private final JpaRequestRepository jpaRequestRepository;
    private final JpaAuditLogRepository jpaAuditLogRepository;

    @Override
    public Request save(Request request) {
        RequestEntity entity = RequestMapper.toEntity(request);
        RequestEntity saved = jpaRequestRepository.save(entity);

        saveAuditLogIfStateChanged(saved);

        return RequestMapper.toDomain(saved);
    }

    @Override
    public Optional<Request> findById(UUID id) {
        return jpaRequestRepository.findById(id).map(RequestMapper::toDomain);
    }

    @Override
    public PageResult<Request> findAll(String name, RequestState state, PageQuery pageQuery) {
        Pageable pageable = PageRequest.of(pageQuery.pageNumber(), pageQuery.pageSize());
        Page<RequestEntity> entityPage = jpaRequestRepository.findAllByNameAndState(name, state, pageable);

        List<Request> requests = entityPage.getContent().stream()
                .map(RequestMapper::toDomain)
                .collect(Collectors.toList());

        return new PageResult<>(requests, entityPage.getTotalElements(),
                pageQuery.pageNumber(), pageQuery.pageSize());
    }

    @Override
    public Long getNextSequenceNumber() {
        return jpaRequestRepository.getNextSequenceNumber();
    }

    private void saveAuditLogIfStateChanged(RequestEntity saved) {
        Optional<AuditLogEntity> lastLog = jpaAuditLogRepository.findFirstByRequestIdOrderByCreatedAtDesc(saved.getId());
        if (lastLog.isEmpty() || hasDifferentState(lastLog.get().getState(), saved.getState())) {
            AuditLogEntity auditLog = AuditLogEntity.builder()
                    .requestId(saved.getId())
                    .state(saved.getState())
                    .createdAt(Instant.now())
                    .build();
            jpaAuditLogRepository.save(auditLog);
        }
    }

    private boolean hasDifferentState(RequestState lastLogged, RequestState saved) {
        return lastLogged != saved;
    }

}
