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
public class MyProjectReviewDto {

    // ==================== Project Basic Info ====================
    private Integer projectId;
    private String projectName;
    private String projectDescription;
    private String projectType;
    private String projectStatus;
    private Date projectCreateDate;
    private Date projectDueDate;

    // ==================== Owner Info ====================
    private Integer ownerId;
    private String ownerName;
    private String ownerEmail;
    private String ownerRole;

    // ==================== My Role In Project ====================
    private String myRoleInProject; // "OWNER" or "MEMBER"
    private Integer myMemberId; // ID trong bảng ProjectMember nếu là member

    // ==================== Final Milestone/Report Info ====================
    private Integer finalMilestoneId;
    private String reportTitle;
    private String reportDescription;
    private String reportFilePath;
    private String reportSubmissionDate;
    private String reportSubmittedBy;

    // ==================== Council Info ====================
    private Integer councilId;
    private String councilName;
    private String councilCode;
    private String councilDepartment;
    private List<CouncilMemberInfo> councilMembers;

    // ==================== Lecturer Mentor Info ====================
    private Boolean hasLecturerMentor;
    private Integer lecturerMentorId;
    private String lecturerMentorName;
    private String lecturerMentorEmail;
    private String lecturerMentorStatus; // "Approved", "Pending", "Rejected"

    // ==================== Scoring Info ====================
    private Boolean hasBeenScored; // Đã có giảng viên chấm điểm chưa
    private Double averageScore; // Điểm trung bình
    private Integer totalScores; // Số giảng viên đã chấm
    private Integer expectedTotalScores; // Tổng số giảng viên trong hội đồng
    private List<LecturerScoreInfo> lecturerScores; // Chi tiết điểm từng giảng viên

    // ==================== Member Statistics ====================
    private Integer totalMembers; // Tổng số thành viên (students + lecturer)
    private Integer totalStudents; // Số sinh viên
    private Integer approvedStudents; // Số sinh viên đã approved
    private Integer pendingStudents; // Số sinh viên đang pending
    private List<MemberInfo> members; // Danh sách thành viên

    // ==================== Nested Classes ====================

    /**
     * Thông tin thành viên hội đồng
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CouncilMemberInfo {
        private Integer councilMemberId;
        private Integer lecturerId;
        private String lecturerName;
        private String lecturerEmail;
        private String role; // "Chủ tịch", "Thư ký", "Ủy viên"
        private Boolean hasScored; // Giảng viên này đã chấm chưa
    }

    /**
     * Thông tin điểm của từng giảng viên
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LecturerScoreInfo {
        private Integer scoreId;
        private Integer lecturerId;
        private String lecturerName;
        private String lecturerEmail;

        // Chi tiết điểm
        private Double criteria1Score;
        private Double criteria2Score;
        private Double criteria3Score;
        private Double criteria4Score;
        private Double criteria5Score;
        private Double criteria6Score;
        private Double bonusScore1;
        private Double bonusScore2;

        private Double totalScore;
        private Double finalScore;
        private String comment;
        private String scoreDate;
    }

    /**
     * Thông tin thành viên trong project
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberInfo {
        private Integer projectMemberId;
        private Integer accountId;
        private String name;
        private String email;
        private String role; // "STUDENT" or "LECTURER"
        private String status; // "Approved", "Pending", "Rejected"
    }
}