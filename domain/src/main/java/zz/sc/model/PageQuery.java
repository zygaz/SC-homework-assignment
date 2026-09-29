package zz.sc.model;

public record PageQuery(int pageNumber, int pageSize) {
    public PageQuery {
        if (pageNumber < 0) {
            throw new IllegalArgumentException("Page number cannot be negative");
        }
        if (pageSize <= 0) {
            throw new IllegalArgumentException("Page size must be greater than zero");
        }
    }
}
