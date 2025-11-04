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
    private List<ProjectFileDto> files;
    private List<ProjectImageDto> images;
    private List<String> invitedEmails;

    @Data
    public static class FileDto {
        private String filePath;
        private String type;
    }

    @Data
    public static class ImageDto {
        private String url;
    }
}

