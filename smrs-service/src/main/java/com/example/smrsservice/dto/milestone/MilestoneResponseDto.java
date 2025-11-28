package com.example.smrsservice.dto.milestone;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class MilestoneResponseDto {
    private Integer id;
    private String description;
    private String status;
    private Double progressPercent;
    private Date createDate;
    private Date dueDate;
    private Integer projectId;
    private Integer createById;

    private Boolean isFinal;               // Có phải milestone cuối không
    private String reportUrl;              // URL file report
    private Date reportSubmittedAt;        // Thời gian nộp report
    private Integer reportSubmittedById;   // ID người nộp report
    private String reportSubmittedByName;  // Tên người nộp report
    private String reportComment;          // Ghi chú khi nộp
}
