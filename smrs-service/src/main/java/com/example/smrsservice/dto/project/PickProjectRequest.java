package com.example.smrsservice.dto.project;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PickProjectRequest {
    private String description;
    private List<FileDto> files;
    private List<ImageDto> images;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileDto {
        private String filePath;
        private String type;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageDto {
        private String url;
    }
}
