package zz.sc.exception;

public class InvalidStateTransitionException extends DomainException {
    public InvalidStateTransitionException(String message) {
        super(message);
    }
}
