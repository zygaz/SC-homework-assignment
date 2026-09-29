package zz.sc.model;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PaginationTest {

    @Test
    void shouldCalculateTotalPagesCorrectly() {
        // total 25, size 10 -> 3 pages
        PageResult<String> result = new PageResult<>(List.of("1", "2"), 25, 0, 10);
        assertEquals(3, result.getTotalPages());
        assertEquals(25, result.getTotalElements());
        assertEquals(0, result.getPageNumber());
        assertEquals(10, result.getPageSize());
        assertEquals(2, result.getItems().size());
    }

    @Test
    void shouldHandleExactMultipleForTotalPages() {
        // total 20, size 10 -> 2 pages
        PageResult<String> result = new PageResult<>(List.of("1", "2"), 20, 0, 10);
        assertEquals(2, result.getTotalPages());
    }

    @Test
    void shouldHandleZeroTotalElements() {
        // total 0 -> 0 pages
        PageResult<String> result = new PageResult<>(List.of(), 0, 0, 10);
        assertEquals(0, result.getTotalPages());
    }

    @Test
    void shouldHandleZeroPageSize() {
        PageResult<String> result = new PageResult<>(List.of(), 10, 0, 0);
        assertEquals(0, result.getTotalPages());
    }

    @Test
    void shouldCreatePageQuery() {
        PageQuery query = new PageQuery(2, 20);
        assertEquals(2, query.pageNumber());
        assertEquals(20, query.pageSize());
    }
    @Test
    void shouldThrowExceptionWhenPageNumberIsNegative() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new PageQuery(-1, 10);
        });
    }

    @Test
    void shouldThrowExceptionWhenPageSizeIsZeroOrNegative() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new PageQuery(0, 0);
        });
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new PageQuery(0, -5);
        });
    }
}
