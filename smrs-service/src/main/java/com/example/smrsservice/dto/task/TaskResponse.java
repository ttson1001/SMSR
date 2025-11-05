package com.example.smrsservice.dto.task;

import lombok.*;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskResponse {
    private Integer id;
    private String name;
    private String description;
    private AccountShortDto createdBy;
    private AccountShortDto assignedTo;
    private Date startDate;
    private Date deadline;
    private Double progressPercent;
    private String status;

    // Milestone info
    private Integer milestoneId;
    private String milestoneName;

    // Project info
    private Integer projectId;
    private String projectName;
}
