package com.camss.honeymoney.dto;

import java.util.List;
import java.util.Map;

public record ExpenseListResponse(
    List<ExpenseResponse> data,
    Meta meta
) {
    public record Meta(
        long totalCount,
        int totalPages,
        int currentPage,
        int pageSize,
        boolean isLast,
        Map<String, Object> appliedFilters
    ) {}
}
