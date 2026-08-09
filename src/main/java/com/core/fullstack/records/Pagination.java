package com.core.fullstack.records;

import java.util.List;

public record Pagination(
        int page,
        int size,
        int totalElements,
        int totalPages,
        boolean first,
        boolean last,
        List<Object> content) {
}
