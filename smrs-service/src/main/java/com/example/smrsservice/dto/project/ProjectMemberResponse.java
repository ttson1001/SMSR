package com.example.smrsservice.dto.project;

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
    private Integer projectId;
    private String projectName;
    private String projectDescription;
    private String projectType;
    private String memberRole; // TEACHER hoặc STUDENT
    private String ownerName;
    private String ownerEmail;
    private String status;
    private Date createDate;
    private Date dueDate;
}
