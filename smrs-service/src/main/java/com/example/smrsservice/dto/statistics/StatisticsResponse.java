package com.example.smrsservice.dto.statistics;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatisticsResponse {
    private Long totalProjects;
    private Long totalAccounts;
    private Long totalStudents;
    private Long totalLecturers;
    private Long totalDeans;
    private Long totalAdmins;

    private Map<String, Long> projectsByStatus;
    private Long archivedProjects;
    private Long activeProjects;
    private Long completedProjects;

    private Long myProjects;
    private Long myTasks;
    private Long myCompletedTasks;
    private Long myPendingTasks;
    private Long mentoringProjects;
    private Long studentProjects;
}
