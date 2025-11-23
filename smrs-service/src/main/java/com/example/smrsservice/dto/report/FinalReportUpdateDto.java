package com.example.smrsservice.dto.report;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinalReportUpdateDto {
    private String reportTitle;
    private String description;
    private String status;
    private String remarks;
}
