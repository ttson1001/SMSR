package com.example.smrsservice.dto.stats.dean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeanOverviewDto {
    private Long totalCouncils;
    private Long pendingProjects;
    private Long approvedProjects;
    private Long totalLecturers;

    private String councilsGrowth;
    private String pendingGrowth;
    private String approvedGrowth;
    private String lecturersGrowth;
}
