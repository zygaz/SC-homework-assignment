package zz.sc.service;

import zz.sc.exception.RequestNotFoundException;
import zz.sc.model.*;
import zz.sc.port.RequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zz.sc.usecase.RequestManagementUseCase;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RequestService implements RequestManagementUseCase {

    private final RequestRepository requestRepository;

    @Override
    @Transactional
    public Request createRequest(String name, String content) {
        Request request = new Request(UUID.randomUUID(), name, content, null);
        return requestRepository.save(request);
    }

    @Override
    @Transactional
    public Request updateContent(UUID id, String newContent) {
        Request request = getRequest(id);
        request.updateContent(newContent);
        return requestRepository.save(request);
    }

    @Override
    @Transactional
    public Request verifyRequest(UUID id) {
        Request request = getRequest(id);
        request.verify();
        return requestRepository.save(request);
    }

    @Override
    @Transactional
    public Request acceptRequest(UUID id) {
        Request request = getRequest(id);
        request.accept();
        return requestRepository.save(request);
    }

    @Override
    @Transactional
    public Request publishRequest(UUID id) {
        Request request = getRequest(id);
        Long seq = requestRepository.getNextSequenceNumber();
        request.publish(seq);
        return requestRepository.save(request);
    }

    @Override
    @Transactional
    public Request rejectRequest(UUID id, ActionReason reason) {
        Request request = getRequest(id);
        request.reject(reason);
        return requestRepository.save(request);
    }

    @Override
    @Transactional
    public Request deleteRequest(UUID id, ActionReason reason) {
        Request request = getRequest(id);
        request.delete(reason);
        return requestRepository.save(request);
    }

    @Override
    @Transactional(readOnly = true)
    public Request getRequest(UUID id) {
        return requestRepository.findById(id)
                .orElseThrow(() -> new RequestNotFoundException("Request not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<Request> getRequests(String name, RequestState state, PageQuery pageQuery) {
        return requestRepository.findAll(name, state, pageQuery);
    }
}
