package com.example.smrsservice.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedResponseDto<T> {
    private boolean success;
    private String message;
    private T data;
    private PaginationInfo pagination;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaginationInfo {
        private int currentPage;
        private int pageSize;
        private long totalElements;
        private int totalPages;
        private boolean hasNext;
        private boolean hasPrevious;
    }

    public static <T> PaginatedResponseDto<T> success(T data, PaginationInfo pagination, String message) {
        return PaginatedResponseDto.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .pagination(pagination)
                .build();
    }

    public static <T> PaginatedResponseDto<T> fail(String message) {
        return PaginatedResponseDto.<T>builder()
                .success(false)
                .message(message)
                .data(null)
                .pagination(null)
                .build();
    }
}