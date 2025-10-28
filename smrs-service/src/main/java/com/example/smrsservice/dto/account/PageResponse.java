package com.example.smrsservice.dto.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class PageResponse<T> {
    int currentPages;
    int pageSizes;
    int totalPages;
    long totalElements;
    List<T>data;

}
