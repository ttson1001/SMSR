package com.example.smrsservice.dto.project;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResubmitRequest {

    private String description;  // Mô tả đã sửa gì

    private List<FileDto> files;  // Files mới sau khi sửa

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileDto {
        private String filePath;
        private String type;
    }
}
