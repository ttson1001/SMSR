package com.example.smrsservice.dto.stats.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminOverviewDto {
    private Long totalProjects;
    private Long totalUsers;
    private Long totalCouncils;
    private Long activeProjects;

    private String projectsGrowth;
    private String usersGrowth;
    private String councilsGrowth;
    private String activeProjectsGrowth;
}



