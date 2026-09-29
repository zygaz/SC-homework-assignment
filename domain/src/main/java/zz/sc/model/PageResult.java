package zz.sc.model;

import lombok.Getter;
import java.util.List;

@Getter
public class PageResult<T> {
    private final List<T> items;
    private final long totalElements;
    private final int totalPages;
    private final int pageNumber;
    private final int pageSize;

    public PageResult(List<T> items, long totalElements, int pageNumber, int pageSize) {
        this.items = items;
        this.totalElements = totalElements;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalPages = pageSize > 0 ? (int) ((totalElements + pageSize - 1) / pageSize) : 0;
    }
}
