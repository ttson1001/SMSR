package com.example.smrsservice.dto.report;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinalReportResponseDto {
    private Integer id;
    private Integer projectId;
    private String projectName;
    private Integer submittedById;
    private String submittedByName;
    private String reportTitle;
    private String description;
    private String filePath;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String submissionDate;
    private String status;
    private Integer version;
    private String remarks;
}
