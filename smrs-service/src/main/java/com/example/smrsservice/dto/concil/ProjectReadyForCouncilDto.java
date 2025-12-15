package com.example.smrsservice.dto.concil;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectReadyForCouncilDto {
    // Project info
    private Integer projectId;
    private String projectName;
    private String projectDescription;
    private String projectType;
    private String projectStatus;
    private Date projectCreateDate;
    private Date projectDueDate;

    // Owner info
    private Integer ownerId;
    private String ownerName;
    private String ownerEmail;
    private String ownerRole;

    // Major info
    private Integer majorId;
    private String majorName;

    // Final Report info
    private Boolean hasFinalReport;
    private Integer finalMilestoneId;
    private String reportUrl;
    private Date reportSubmittedAt;
    private String reportSubmittedBy;

    // Council assignment info
    private Boolean alreadyAssignedToCouncil;
    private Integer assignedCouncilId;
    private String assignedCouncilName;
    private String assignedCouncilCode;
}