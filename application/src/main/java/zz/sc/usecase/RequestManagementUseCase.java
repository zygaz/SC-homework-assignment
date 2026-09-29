package zz.sc.usecase;

import zz.sc.model.*;

import java.util.UUID;

public interface RequestManagementUseCase {
    Request createRequest(String name, String content);
    Request updateContent(UUID id, String newContent);
    Request verifyRequest(UUID id);
    Request acceptRequest(UUID id);
    Request publishRequest(UUID id);
    Request rejectRequest(UUID id, ActionReason reason);
    Request deleteRequest(UUID id, ActionReason reason);
    Request getRequest(UUID id);
    PageResult<Request> getRequests(String name, RequestState state, PageQuery pageQuery);
}
