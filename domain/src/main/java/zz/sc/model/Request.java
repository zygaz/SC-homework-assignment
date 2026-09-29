package zz.sc.model;

import zz.sc.exception.ContentModificationNotAllowedException;
import zz.sc.exception.InvalidStateTransitionException;
import lombok.Getter;

import java.util.UUID;
import java.util.Objects;

@Getter
public class Request {
    private final UUID id;
    private Long version;
    private final String name;
    private String content;
    private RequestState state;
    private ActionReason actionReason;
    private Long sequenceNumber;

    public Request(UUID id, String name, String content, Long version) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be null or blank");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Content cannot be null or blank");
        }
        if (name.length() > 255) {
            throw new IllegalArgumentException("Name cannot exceed 255 characters");
        }
        this.id = id;
        this.version = version;
        this.name = name;
        this.content = content;
        this.state = RequestState.CREATED;
    }

    public Request(UUID id, String name, String content, 
                   RequestState state, ActionReason actionReason, Long sequenceNumber, Long version) {
        this.id = id;
        this.name = name;
        this.content = content;
        this.state = state;
        this.actionReason = actionReason;
        this.sequenceNumber = sequenceNumber;
        this.version = version;
    }

    public void updateContent(String newContent) {
        if (state != RequestState.CREATED && state != RequestState.VERIFIED) {
            throw new ContentModificationNotAllowedException(
                "Content can only be modified in CREATED or VERIFIED state. Current state: " + state);
        }
        if (newContent == null || newContent.isBlank()) {
            throw new IllegalArgumentException("Content cannot be null or blank");
        }
        this.content = newContent;
    }

    public void verify() {
        if (state != RequestState.CREATED) {
            throw new InvalidStateTransitionException("Cannot transition to VERIFIED from " + state);
        }
        this.state = RequestState.VERIFIED;
    }

    public void accept() {
        if (state != RequestState.VERIFIED) {
            throw new InvalidStateTransitionException("Cannot transition to ACCEPTED from " + state);
        }
        this.state = RequestState.ACCEPTED;
    }

    public void publish(Long generatedSequenceNumber) {
        if (state != RequestState.ACCEPTED) {
            throw new InvalidStateTransitionException("Cannot transition to PUBLISHED from " + state);
        }
        if (generatedSequenceNumber == null) {
            throw new IllegalArgumentException("Sequence number must be provided for publication");
        }
        this.state = RequestState.PUBLISHED;
        this.sequenceNumber = generatedSequenceNumber;
    }

    public void reject(ActionReason reason) {
        if (state != RequestState.VERIFIED && state != RequestState.ACCEPTED) {
            throw new InvalidStateTransitionException("Cannot transition to REJECTED from " + state);
        }
        if (reason == null) {
            throw new IllegalArgumentException("Reason must be provided for rejection");
        }
        this.state = RequestState.REJECTED;
        this.actionReason = reason;
    }

    public void delete(ActionReason reason) {
        if (state != RequestState.CREATED) {
            throw new InvalidStateTransitionException("Cannot transition to DELETED from " + state);
        }
        if (reason == null) {
            throw new IllegalArgumentException("Reason must be provided for deletion");
        }
        this.state = RequestState.DELETED;
        this.actionReason = reason;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Request request = (Request) o;
        return Objects.equals(id, request.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
