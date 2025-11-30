package com.example.smrsservice.dto.stats.students;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentOverviewDto {
    private Long myProjects;
    private Long projectsAsOwner;
    private Long completedProjects;
    private Double averageScore;

    private String projectsGrowth;
    private String ownerGrowth;
    private String completedGrowth;
    private String scoreGrowth;
}