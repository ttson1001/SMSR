package com.example.smrsservice.dto.projectpublication;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectPublicationDto {

    // Publication info
    private Integer id;
    private String status;
    private String publicationName;
    private String publicationType;
    private String publicationLink;
    private Date registeredDate;
    private Date publishedDate;
    private String notes;
    private String doi;
    private String isbnIssn;
    private Date createdAt;
    private Date updatedAt;

    // Full project info
    private ProjectInfo project;

    // Full author info
    private AuthorInfo author;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectInfo {
        private Integer projectId;
        private String projectName;
        private String projectDescription;
        private String projectType;
        private String projectStatus;
        private Date projectDueDate;
        private Date projectCreateDate;

        // Owner
        private Integer ownerId;
        private String ownerName;
        private String ownerEmail;
        private String ownerRole;

        // Major
        private Integer majorId;
        private String majorName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthorInfo {
        private Integer authorId;
        private String authorName;
        private String authorEmail;
        private String authorPhone;
        private String authorAvatar;
        private String authorRole;
        private Integer authorAge;
        private String authorStatus;
    }
}