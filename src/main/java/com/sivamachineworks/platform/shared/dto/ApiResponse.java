package com.sivamachineworks.platform.shared.dto;

import java.time.Instant;

public class ApiResponse<T> {
    private boolean success;
    private Instant timestamp;
    private T data;
    private PaginationMeta pagination;

    public ApiResponse() {}

    public ApiResponse(boolean success, Instant timestamp, T data, PaginationMeta pagination) {
        this.success = success;
        this.timestamp = timestamp;
        this.data = data;
        this.pagination = pagination;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }

    public PaginationMeta getPagination() { return pagination; }
    public void setPagination(PaginationMeta pagination) { this.pagination = pagination; }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, Instant.now(), data, null);
    }
}
