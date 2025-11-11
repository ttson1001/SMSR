package com.example.smrsservice.dto.milestone;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MilestoneSubmitReportDto {
    private String reportUrl;
    private String reportComment;
}
