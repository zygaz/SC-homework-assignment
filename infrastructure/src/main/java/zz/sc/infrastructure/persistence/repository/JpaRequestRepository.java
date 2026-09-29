package zz.sc.infrastructure.persistence.repository;

import zz.sc.infrastructure.persistence.entity.RequestEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import zz.sc.model.RequestState;

import java.util.UUID;


public interface JpaRequestRepository extends JpaRepository<RequestEntity, UUID> {
    
    @Query("SELECT r FROM RequestEntity r WHERE (:name IS NULL OR r.name = :name) AND (:state IS NULL OR r.state = :state)")
    Page<RequestEntity> findAllByNameAndState(String name, RequestState state, Pageable pageable);

    @Query(value = "SELECT nextval('request_sequence')", nativeQuery = true)
    Long getNextSequenceNumber();
}
