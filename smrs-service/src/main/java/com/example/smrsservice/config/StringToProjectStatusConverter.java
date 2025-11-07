package com.example.smrsservice.config;

import com.example.smrsservice.common.ProjectStatus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToProjectStatusConverter implements Converter<String, ProjectStatus> {

    @Override
    public ProjectStatus convert(String source) {
        if (source == null || source.trim().isEmpty()) {
            return null;
        }

        try {
            // Chuyển "Pending" → "PENDING", "in_review" → "IN_REVIEW"
            String normalized = source.trim().toUpperCase();
            return ProjectStatus.valueOf(normalized);

        } catch (IllegalArgumentException e) {
            // Nếu không match, thử với các giá trị thay thế
            String lower = source.trim().toLowerCase();

            switch (lower) {
                case "pending":
                    return ProjectStatus.PENDING;
                case "in_review":
                case "inreview":
                case "in review":
                    return ProjectStatus.IN_REVIEW;
                case "approved":
                    return ProjectStatus.APPROVED;
                case "rejected":
                    return ProjectStatus.REJECTED;
                default:
                    throw new IllegalArgumentException(
                            "Invalid status value: '" + source + "'. " +
                                    "Valid values are: PENDING, IN_REVIEW, APPROVED, REJECTED"
                    );
            }
        }
    }
}