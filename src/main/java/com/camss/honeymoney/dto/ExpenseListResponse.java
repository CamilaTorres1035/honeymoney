package com.camss.honeymoney.dto;

import java.util.List;
import java.util.Map;

public record ExpenseListResponse(
    List<ExpenseResponse> data,
    Meta meta
) {
    public record Meta(
        long totalCount,
        Map<String, Object> appliedFilters
    ) {}
}
