package com.example.smrsservice.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ProjectStatus {
    AVAILABLE("Available"),
    PENDING("Pending"),
    IN_REVIEW("InReview"),
    APPROVED("Approved"),
    REJECTED("Rejected"),
    IN_PROGRESS("InProgress"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled"),
    ARCHIVED("Archived");

    private final String jsonName;

    ProjectStatus(String jsonName) {
        this.jsonName = jsonName;
    }

    @JsonValue
    public String getJsonName() {
        return jsonName;
    }

    @JsonCreator
    public static ProjectStatus from(String value) {
        if (value == null) throw new IllegalArgumentException("Status is required");
        for (ProjectStatus s : values()) {
            if (s.jsonName.equalsIgnoreCase(value) || s.name().equalsIgnoreCase(value)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Invalid status: " + value);
    }

    // ✅ CẬP NHẬT: Thêm logic chuyển đổi cho AVAILABLE
    public boolean canTransitionTo(ProjectStatus target) {
        if (this == target) return true;
        return switch (this) {
            case AVAILABLE -> target == PENDING || target == CANCELLED || target == ARCHIVED;  // ✅ THÊM
            case PENDING -> target == IN_REVIEW || target == CANCELLED || target == AVAILABLE;  // ✅ Có thể quay lại AVAILABLE
            case IN_REVIEW -> target == APPROVED || target == REJECTED || target == CANCELLED;
            case APPROVED -> target == IN_PROGRESS || target == CANCELLED;
            case IN_PROGRESS -> target == COMPLETED || target == CANCELLED;
            case REJECTED -> target == AVAILABLE || target == CANCELLED;  // ✅ Có thể mở lại cho người khác
            case ARCHIVED -> target == AVAILABLE || target == PENDING;  // ✅ Có thể kích hoạt lại
            case COMPLETED, CANCELLED -> false;
        };
    }

    public boolean isTerminal() {
        return this == COMPLETED || this == CANCELLED;
    }

    // ✅ THÊM: Kiểm tra project có thể đăng ký không
    public boolean isAvailableForRegistration() {
        return this == AVAILABLE;
    }

    // ✅ THÊM: Kiểm tra project đã có owner chưa
    public boolean hasOwner() {
        return this != AVAILABLE && this != ARCHIVED;
    }
}