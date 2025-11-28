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
public class ProjectReviewDto {

    // ==================== PROJECT INFO ====================
    private Integer projectId;
    private String projectName;
    private String projectDescription;
    private String projectType;
    private String projectStatus;
    private Date projectCreateDate;
    private Date projectDueDate;

    // ==================== FINAL MILESTONE INFO ====================
    private Integer finalMilestoneId;         // ID milestone final
    private String reportTitle;               // (Milestone không có title → đặt mặc định)
    private String reportDescription;         // reportComment
    private String reportFilePath;            // reportUrl
    private String reportSubmissionDate;      // reportSubmittedAt
    private String reportSubmittedBy;         // Người nộp report

    // ==================== COUNCIL INFO ====================
    private Integer councilId;
    private String councilName;
    private String councilCode;
    private String councilDepartment;

    // ==================== SCORING INFO ====================
    private Boolean hasScored;           // Tôi đã chấm chưa?
    private Integer myScoreId;           // ID điểm tôi chấm
    private Double myFinalScore;         // Điểm tôi chấm
    private Double currentAverage;       // Điểm TB hiện tại
    private Integer totalScores;         // Số GV đã chấm
    private Integer totalCouncilMembers; // Tổng số GV trong council

    // ==================== PROJECT OWNER INFO ====================
    private Integer ownerId;
    private String ownerName;
    private String ownerEmail;
    private String ownerRole;

    // ==================== ADDITIONAL INFO ====================
    private Integer totalMembers;        // Tổng số thành viên project
    private Integer totalStudents;       // Số sinh viên
    private Boolean hasLecturer;         // Có giảng viên hướng dẫn chưa
}
