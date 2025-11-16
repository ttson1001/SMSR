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
}
