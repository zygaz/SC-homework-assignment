package zz.sc.exception;

public class ContentModificationNotAllowedException extends DomainException {
    public ContentModificationNotAllowedException(String message) {
        super(message);
    }
}
