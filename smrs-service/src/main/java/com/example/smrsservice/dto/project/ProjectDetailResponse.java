package com.example.smrsservice.dto.project;

import com.example.smrsservice.common.ProjectStatus;
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
public class ProjectDetailResponse {
    private Integer id;
    private String name;
    private String description;
    private String type;
    private ProjectStatus status;
    private Date createDate;
    private Date dueDate;

    // Thông tin owner
    private OwnerInfo owner;

    // Giảng viên hướng dẫn
    private LecturerInfo lecturer;

    // Danh sách thành viên
    private List<MemberInfo> members;

    // Danh sách files
    private List<FileInfo> files;

    // Danh sách images
    private List<ImageInfo> images;

    // Thống kê
    private Statistics statistics;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OwnerInfo {
        private Integer id;
        private String name;
        private String email;
        private String role;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LecturerInfo {
        private Integer id;
        private Integer accountId;
        private String name;
        private String email;
        private String status; // Pending, Approved, Cancelled
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberInfo {
        private Integer id;
        private Integer accountId;
        private String name;
        private String email;
        private String role; // STUDENT, LECTURER
        private String status; // Pending, Approved, Cancelled
        private Date joinedDate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileInfo {
        private Integer id;
        private String filePath;
        private String type;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageInfo {
        private Integer id;
        private String url;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Statistics {
        private int totalMembers;
        private int approvedMembers;
        private int pendingMembers;
        private int totalStudents;
        private int totalFiles;
        private int totalImages;
        private boolean hasLecturer;
    }
}
