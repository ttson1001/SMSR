package com.example.smrsservice.dto.project;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MentoringProjectDto {
    // ========== PROJECT INFO ==========
    private Integer projectId;
    private String projectName;
    private String projectDescription;
    private String projectType;
    private String projectStatus;
    private Date projectCreateDate;
    private Date projectDueDate;

    // ========== OWNER INFO ==========
    private Integer ownerId;
    private String ownerName;
    private String ownerEmail;
    private String ownerRole;

    // ========== MAJOR INFO ==========
    private Integer majorId;
    private String majorName;

    // ========== MENTORING INFO ==========
    private Integer projectMemberId;  // ID của lecturer trong bảng project_member
    private String mentoringStatus;   // Approved, Pending, Rejected

    // ========== STUDENTS INFO ==========
    private List<StudentInfo> students;
    private Integer totalStudents;
    private Integer approvedStudents;
    private Integer pendingStudents;

    // ========== MILESTONES INFO ==========
    private Integer totalMilestones;
    private Integer completedMilestones;
    private Boolean hasFinalReport;
    private String finalReportUrl;

    // ========== SCORING INFO ==========
    private Double averageScore;
    private Boolean hasBeenScored;

    // ========== NESTED DTO: STUDENT INFO ==========
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentInfo {
        private Integer projectMemberId;
        private Integer accountId;
        private String name;
        private String email;
        private String status;  // Approved, Pending, Rejected
    }
}
