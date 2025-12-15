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

    private OwnerInfo owner;
    private LecturerInfo lecturer;
    private List<MemberInfo> members;
    private List<FileInfo> files;
    private List<ImageInfo> images;
    private Statistics statistics;
    private MajorInfo major;


    private Double averageScore;
    private Integer totalScores;

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
        private String status;
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
        private String role;
        private String status;
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
        private Integer totalMembers;
        private Integer approvedMembers;
        private Integer pendingMembers;
        private Integer totalStudents;
        private Integer totalFiles;
        private Integer totalImages;
        private Boolean hasLecturer;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MajorInfo {
        private Integer id;
        private String name;
    }
}