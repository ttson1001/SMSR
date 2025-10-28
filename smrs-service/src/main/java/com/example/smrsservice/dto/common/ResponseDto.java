package com.example.smrsservice.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponseDto<T> {
    private boolean success;
    private String message;
    private T data;
    private PaginationInfo pagination;

    @Data
    @Builder
    public static class PaginationInfo {
        private int pageNumber;
        private int pageSize;
        private long totalElements;
        private int totalPages;
        private boolean first;
        private boolean last;
        private boolean empty;
    }

    public static <T> ResponseDto<T> success(T data, String message) {
        return ResponseDto.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ResponseDto<T> successWithPagination(T data, String message, Page<?> page) {
        return ResponseDto.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .pagination(PaginationInfo.builder()
                        .pageNumber(page.getNumber())
                        .pageSize(page.getSize())
                        .totalElements(page.getTotalElements())
                        .totalPages(page.getTotalPages())
                        .first(page.isFirst())
                        .last(page.isLast())
                        .empty(page.isEmpty())
                        .build())
                .build();
    }

    public static <T> ResponseDto<T> fail(String message) {
        return ResponseDto.<T>builder()
                .success(false)
                .message(message)
                .build();
    }
}