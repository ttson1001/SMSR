package com.example.smrsservice.dto.project;

import lombok.*;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectCreateDto {
    private String name;
    private String description;
    private String type;
    private Date dueDate;
    private Integer majorId;
    private List<String> invitedEmails;

    // ✅ THÊM 2 FIELDS NÀY
    private List<FileDto> files;
    private List<ImageDto> images;

    // ✅ THÊM INNER CLASS FileDto
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileDto {
        private String filePath;
        private String type;
    }

    // ✅ THÊM INNER CLASS ImageDto
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageDto {
        private String url;
    }
}

