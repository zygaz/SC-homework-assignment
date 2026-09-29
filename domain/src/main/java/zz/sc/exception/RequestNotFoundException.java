package zz.sc.exception;

public class RequestNotFoundException extends DomainException {
    public RequestNotFoundException(String message) {
        super(message);
    }
}
