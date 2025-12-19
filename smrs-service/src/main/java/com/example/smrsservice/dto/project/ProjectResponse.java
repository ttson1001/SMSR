package com.example.smrsservice.dto.project;

import com.example.smrsservice.common.ProjectStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor      // ⭐ THÊM annotation này
@AllArgsConstructor     // ⭐ THÊM annotation này
public class ProjectResponse {
    private Integer id;
    private String name;
    private String description;
    private String type;
    private Instant dueDate;
    private Integer ownerId;
    private String ownerName;
    private String ownerEmail;
    private String ownerRole;
    private ProjectStatus status;
    private Instant createdAt;

    private Integer majorId;
    private String majorName;

    private List<FileInfo> files;
    private List<String> images;


    private MentorInfo mentor;
    private List<StudentInfo> students;
    private Boolean hasFinalReport;

    private String rejectionReason;     // Lý do reject
    private String rejectionFeedback;   // Nhận xét chi tiết
    private Date revisionDeadline;
    private Boolean isCreatedByDean;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileInfo {
        private Integer id;
        private String fileName;
        private String fileUrl;
        private String fileType;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MentorInfo {
        private Integer projectMemberId;
        private Integer accountId;
        private String name;
        private String email;
        private String status;  // "Approved", "Pending", "Rejected"
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentInfo {
        private Integer projectMemberId;
        private Integer accountId;
        private String name;
        private String email;
        private String status;  // "Approved", "Pending", "Rejected"
    }
}