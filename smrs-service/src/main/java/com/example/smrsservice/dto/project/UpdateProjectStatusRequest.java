package com.example.smrsservice.dto.project;

import com.example.smrsservice.common.ProjectStatus;
import lombok.Data;

@Data
public class UpdateProjectStatusRequest {
    private ProjectStatus status; // JSON: "InReview", "Completed", ...
    private String note;
}