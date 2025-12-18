package com.example.smrsservice.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ProjectStatus {
    AVAILABLE("Available"),
    PENDING("Pending"),
    REVISION_REQUIRED("RevisionRequired"),  // ⭐ THÊM MỚI
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

    public boolean canTransitionTo(ProjectStatus target) {
        if (this == target) return true;
        return switch (this) {
            case AVAILABLE -> target == PENDING || target == CANCELLED || target == ARCHIVED;
            case PENDING -> target == IN_REVIEW
                    || target == REVISION_REQUIRED
                    || target == ARCHIVED
                    || target == CANCELLED
                    || target == AVAILABLE;
            case REVISION_REQUIRED -> target == PENDING
                    || target == ARCHIVED;
            case IN_REVIEW -> target == APPROVED || target == REJECTED || target == CANCELLED;
            case APPROVED -> target == IN_PROGRESS || target == CANCELLED;
            case IN_PROGRESS -> target == COMPLETED || target == CANCELLED;
            case REJECTED -> target == AVAILABLE || target == CANCELLED;
            case ARCHIVED -> target == AVAILABLE || target == PENDING;
            case COMPLETED, CANCELLED -> false;
        };
    }

    public boolean isTerminal() {
        return this == COMPLETED || this == CANCELLED;
    }

    public boolean isAvailableForRegistration() {
        return this == AVAILABLE;
    }

    public boolean hasOwner() {
        return this != AVAILABLE && this != ARCHIVED;
    }
}