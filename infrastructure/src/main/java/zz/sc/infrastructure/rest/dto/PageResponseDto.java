package zz.sc.infrastructure.rest.dto;

import zz.sc.model.PageResult;
import lombok.Data;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Data
public class PageResponseDto<T> {
    private List<T> items;
    private long totalElements;
    private int totalPages;
    private int pageNumber;
    private int pageSize;

    public static <D, T> PageResponseDto<T> fromPageResult(PageResult<D> result, Function<D, T> mapper) {
        PageResponseDto<T> dto = new PageResponseDto<>();
        dto.setItems(result.getItems().stream().map(mapper).collect(Collectors.toList()));
        dto.setTotalElements(result.getTotalElements());
        dto.setTotalPages(result.getTotalPages());
        dto.setPageNumber(result.getPageNumber());
        dto.setPageSize(result.getPageSize());
        return dto;
    }
}
