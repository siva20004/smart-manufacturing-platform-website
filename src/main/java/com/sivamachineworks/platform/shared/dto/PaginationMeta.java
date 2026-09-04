package com.sivamachineworks.platform.shared.dto;

public record PaginationMeta(
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean first,
    boolean last
) {}
