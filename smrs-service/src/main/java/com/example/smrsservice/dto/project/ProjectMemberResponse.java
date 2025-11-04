package com.example.smrsservice.dto.project;

import com.example.smrsservice.common.ProjectStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectMemberResponse {
    private Integer id;

    // Project info
    private Integer projectId;
    private String projectName;
    private String projectDescription;
    private String projectType;
    private ProjectStatus projectStatus;  // ← THÊM DÒNG NÀY

    // Member info
    private String memberRole;
    private String status;

    // Owner info
    private String ownerName;
    private String ownerEmail;

    // Dates
    private Date createDate;
    private Date dueDate;
}
