package com.sivamachineworks.platform.shared.dto;

import java.util.List;

public class PaginatedResponse<T> {
    private List<T> content;
    private PaginationMeta pagination;

    public PaginatedResponse() {}

    public PaginatedResponse(List<T> content, PaginationMeta pagination) {
        this.content = content;
        this.pagination = pagination;
    }

    public List<T> getContent() { return content; }
    public void setContent(List<T> content) { this.content = content; }

    public PaginationMeta getPagination() { return pagination; }
    public void setPagination(PaginationMeta pagination) { this.pagination = pagination; }
}
