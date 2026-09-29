package zz.sc.service;

import zz.sc.exception.RequestNotFoundException;
import zz.sc.exception.InvalidStateTransitionException;
import zz.sc.model.ActionReason;
import zz.sc.model.*;
import zz.sc.port.RequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestServiceTest {

    @Mock
    private RequestRepository requestRepository;

    @InjectMocks
    private RequestService requestService;

    private Request request;
    private final UUID id = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        request = new Request(id, "Test Name", "Test Content", null);
    }

    @Test
    void shouldCreateRequest() {
        when(requestRepository.save(any(Request.class))).thenAnswer(i -> i.getArguments()[0]);

        Request created = requestService.createRequest("New Request", "New Content");

        assertNotNull(created);
        assertEquals("New Request", created.getName());
        assertEquals("New Content", created.getContent());
        assertEquals(RequestState.CREATED, created.getState());
        verify(requestRepository).save(any(Request.class));
    }

    @Test
    void shouldVerifyRequest() {
        when(requestRepository.findById(id)).thenReturn(Optional.of(request));
        when(requestRepository.save(request)).thenReturn(request);

        Request verified = requestService.verifyRequest(id);

        assertEquals(RequestState.VERIFIED, verified.getState());
        verify(requestRepository).save(request);
    }

    @Test
    void shouldPublishRequest() {
        request.verify();
        request.accept();
        when(requestRepository.findById(id)).thenReturn(Optional.of(request));
        when(requestRepository.getNextSequenceNumber()).thenReturn(100L);
        when(requestRepository.save(request)).thenReturn(request);

        Request published = requestService.publishRequest(id);

        assertEquals(RequestState.PUBLISHED, published.getState());
        assertEquals(100L, published.getSequenceNumber());
        verify(requestRepository).save(request);
    }

    @Test
    void shouldThrowExceptionWhenRequestNotFound() {
        when(requestRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RequestNotFoundException.class, () -> requestService.getRequest(id));
    }

    @Test
    void shouldGetRequestsPage() {
        PageQuery query = new PageQuery(0, 10);
        PageResult<Request> pageResult = new PageResult<>(Collections.singletonList(request), 1, 0, 10);
        when(requestRepository.findAll("Test", RequestState.CREATED, query)).thenReturn(pageResult);

        PageResult<Request> result = requestService.getRequests("Test", RequestState.CREATED, query);

        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getItems().size());
    }
    @Test
    void shouldUpdateContent() {
        when(requestRepository.findById(id)).thenReturn(Optional.of(request));
        when(requestRepository.save(request)).thenReturn(request);

        Request updated = requestService.updateContent(id, "Updated Content");

        assertEquals("Updated Content", updated.getContent());
        verify(requestRepository).save(request);
    }

    @Test
    void shouldAcceptRequest() {
        request.verify();
        when(requestRepository.findById(id)).thenReturn(Optional.of(request));
        when(requestRepository.save(request)).thenReturn(request);

        Request accepted = requestService.acceptRequest(id);

        assertEquals(RequestState.ACCEPTED, accepted.getState());
        verify(requestRepository).save(request);
    }

    @Test
    void shouldRejectRequest() {
        request.verify();
        when(requestRepository.findById(id)).thenReturn(Optional.of(request));
        when(requestRepository.save(request)).thenReturn(request);

        Request rejected = requestService.rejectRequest(id, ActionReason.INAPPROPRIATE_CONTENT);

        assertEquals(RequestState.REJECTED, rejected.getState());
        assertEquals(ActionReason.INAPPROPRIATE_CONTENT, rejected.getActionReason());
        verify(requestRepository).save(request);
    }

    @Test
    void shouldDeleteRequest() {
        when(requestRepository.findById(id)).thenReturn(Optional.of(request));
        when(requestRepository.save(request)).thenReturn(request);

        Request deleted = requestService.deleteRequest(id, ActionReason.NOT_NEEDED);

        assertEquals(RequestState.DELETED, deleted.getState());
        assertEquals(ActionReason.NOT_NEEDED, deleted.getActionReason());
        verify(requestRepository).save(request);
    }

    @Test
    void shouldGetRequest() {
        when(requestRepository.findById(id)).thenReturn(Optional.of(request));

        Request found = requestService.getRequest(id);

        assertNotNull(found);
        assertEquals(id, found.getId());
    }
    @Test
    void shouldThrowExceptionWhenAcceptingUnverifiedRequest() {
        when(requestRepository.findById(id)).thenReturn(Optional.of(request));
        assertThrows(InvalidStateTransitionException.class, () -> requestService.acceptRequest(id));
    }

    @Test
    void shouldThrowExceptionWhenPublishingUnacceptedRequest() {
        when(requestRepository.findById(id)).thenReturn(Optional.of(request));
        assertThrows(InvalidStateTransitionException.class, () -> requestService.publishRequest(id));
    }

    @Test
    void shouldThrowExceptionWhenRejectingCreatedRequest() {
        when(requestRepository.findById(id)).thenReturn(Optional.of(request));
        assertThrows(InvalidStateTransitionException.class, () -> requestService.rejectRequest(id, ActionReason.OTHER));
    }
}
