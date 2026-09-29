package zz.sc.port;

import zz.sc.model.PageQuery;
import zz.sc.model.PageResult;
import zz.sc.model.Request;
import zz.sc.model.RequestState;

import java.util.Optional;
import java.util.UUID;

public interface RequestRepository {
    Request save(Request request);
    Optional<Request> findById(UUID id);
    PageResult<Request> findAll(String name, RequestState state, PageQuery pageQuery);
    Long getNextSequenceNumber();
}
