package com.example.smrsservice.config;

import com.example.smrsservice.common.ProjectStatus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ProjectStatusConverter implements Converter<String, ProjectStatus> {
    @Override
    public ProjectStatus convert(String source) {
        try {
            String normalized = source.toUpperCase()
                    .replace(" ", "_")
                    .replace("-", "_");

            return ProjectStatus.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid project status: " + source +
                            ". Valid values are: PENDING, IN_PROGRESS, COMPLETED, ARCHIVED, REJECTED, AVAILABLE"
            );
        }
    }
}
