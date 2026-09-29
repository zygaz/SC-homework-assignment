package zz.sc.model;

import zz.sc.exception.ContentModificationNotAllowedException;
import zz.sc.exception.InvalidStateTransitionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RequestTest {

    private final UUID id = UUID.randomUUID();

    @Test
    void shouldCreateRequestInCreatedState() {
        Request request = new Request(id, "My Request", "Initial content", null);

        assertEquals(id, request.getId());
        assertEquals("My Request", request.getName());
        assertEquals("Initial content", request.getContent());
        assertEquals(RequestState.CREATED, request.getState());
        assertNull(request.getActionReason());
        assertNull(request.getSequenceNumber());
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldFailCreationIfNameInvalid(String name) {
        assertThrows(IllegalArgumentException.class, () -> new Request(id, name, "content", null));
    }

    @Test
    void shouldFailCreationIfNameTooLong() {
        String longName = "a".repeat(256);
        assertThrows(IllegalArgumentException.class, () -> new Request(id, longName, "content", null));
    }

    @Test
    void shouldUpdateContentInCreatedState() {
        Request request = new Request(id, "Name", "Content", null);
        request.updateContent("New Content");
        assertEquals("New Content", request.getContent());
    }

    @Test
    void shouldUpdateContentInVerifiedState() {
        Request request = new Request(id, "Name", "Content", null);
        request.verify();
        request.updateContent("New Content");
        assertEquals("New Content", request.getContent());
    }

    @Test
    void shouldFailToUpdateContentInAcceptedState() {
        Request request = new Request(id, "Name", "Content", null);
        request.verify();
        request.accept();
        assertThrows(ContentModificationNotAllowedException.class, () -> request.updateContent("New"));
    }

    @Test
    void shouldTransitionToVerified() {
        Request request = new Request(id, "Name", "Content", null);
        request.verify();
        assertEquals(RequestState.VERIFIED, request.getState());
    }

    @Test
    void shouldFailToPublishIfNotAccepted() {
        Request request = new Request(id, "Name", "Content", null);
        assertThrows(InvalidStateTransitionException.class, () -> request.publish(1L));
    }

    @Test
    void shouldTransitionToPublished() {
        Request request = new Request(id, "Name", "Content", null);
        request.verify();
        request.accept();
        request.publish(123L);
        assertEquals(RequestState.PUBLISHED, request.getState());
        assertEquals(123L, request.getSequenceNumber());
    }

    @Test
    void shouldTransitionToRejectedFromVerified() {
        Request request = new Request(id, "Name", "Content", null);
        request.verify();
        request.reject(ActionReason.OTHER);
        assertEquals(RequestState.REJECTED, request.getState());
        assertEquals(ActionReason.OTHER, request.getActionReason());
    }

    @Test
    void shouldTransitionToDeletedFromCreated() {
        Request request = new Request(id, "Name", "Content", null);
        request.delete(ActionReason.NOT_NEEDED);
        assertEquals(RequestState.DELETED, request.getState());
        assertEquals(ActionReason.NOT_NEEDED, request.getActionReason());
    }

    @Test
    void shouldFailToAcceptIfNotVerified() {
        Request request = new Request(id, "Name", "Content", null);
        assertThrows(InvalidStateTransitionException.class, request::accept);
    }

    @Test
    void shouldFailToVerifyIfAlreadyVerified() {
        Request request = new Request(id, "Name", "Content", null);
        request.verify();
        assertThrows(InvalidStateTransitionException.class, request::verify);
    }

    @Test
    void shouldFailToRejectIfCreated() {
        Request request = new Request(id, "Name", "Content", null);
        assertThrows(InvalidStateTransitionException.class, () -> request.reject(ActionReason.OTHER));
    }

    @Test
    void shouldFailToDeleteIfNotCreated() {
        Request request = new Request(id, "Name", "Content", null);
        request.verify();
        assertThrows(InvalidStateTransitionException.class, () -> request.delete(ActionReason.OTHER));
    }

    @Test
    void shouldBeEqualIfIdsAreSame() {
        UUID commonId = UUID.randomUUID();
        Request request1 = new Request(commonId, "Name1", "Content1", null);
        Request request2 = new Request(commonId, "Name2", "Content2", null);
        Request request3 = new Request(UUID.randomUUID(), "Name1", "Content1", null);

        assertEquals(request1, request2);
        assertNotEquals(request1, request3);
        assertEquals(request1.hashCode(), request2.hashCode());
        assertNotEquals(request1.hashCode(), request3.hashCode());
    }
}
