package com.example.smrsservice.dto.report;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinalReportCreateDto {
    private Integer projectId;
    private String reportTitle;
    private String description;

    private String filePath;
    private String fileName;
    private String fileType;
    private Long fileSize;
}
