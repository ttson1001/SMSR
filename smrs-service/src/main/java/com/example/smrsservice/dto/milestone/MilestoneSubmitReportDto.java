package com.example.smrsservice.dto.milestone;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MilestoneSubmitReportDto {
    @NotBlank(message = "Bạn chưa nộp file báo cáo. Vui lòng tải file lên trước khi nộp.")
    private String reportUrl;
    private String reportComment;
}
