package com.example.smrsservice.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
public enum PublicationStatus {
    REGISTERED("Registered", "Đã đăng ký"),
    PUBLISHED("Published", "Đã xuất bản"),
    CANCELLED("Cancelled", "Đã hủy");

    private final String jsonName;
    private final String displayName;
}
