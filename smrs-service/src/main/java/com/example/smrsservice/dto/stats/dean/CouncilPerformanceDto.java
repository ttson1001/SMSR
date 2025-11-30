package com.example.smrsservice.dto.stats.dean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouncilPerformanceDto {
    private Integer councilId;
    private String councilName;
    private String councilCode;
    private Integer totalProjects;
    private Integer completedProjects;
    private Double averageScore;
    private Integer activeMembers;
    private String completionRate;
}
