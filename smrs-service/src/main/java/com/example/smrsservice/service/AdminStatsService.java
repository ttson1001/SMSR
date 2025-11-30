package com.example.smrsservice.service;

import com.example.smrsservice.common.ProjectStatus;
import com.example.smrsservice.dto.common.PaginatedResponseDto;
import com.example.smrsservice.dto.stats.admin.*;
import com.example.smrsservice.entity.Account;
import com.example.smrsservice.entity.Project;
import com.example.smrsservice.repository.AccountRepository;
import com.example.smrsservice.repository.CouncilRepository;
import com.example.smrsservice.repository.ProjectRepository;
import com.example.smrsservice.repository.ProjectScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminStatsService {

    private final ProjectRepository projectRepository;
    private final AccountRepository accountRepository;
    private final CouncilRepository councilRepository;
    private final ProjectScoreRepository projectScoreRepository;

    /**
     * 1. Overview Cards - Top metrics
     */
    public AdminOverviewDto getOverview() {
        long totalProjects = projectRepository.count();
        long totalUsers = accountRepository.count();
        long totalCouncils = councilRepository.count();
        long activeProjects = projectRepository.countByStatus(ProjectStatus.APPROVED)
                + projectRepository.countByStatus(ProjectStatus.IN_REVIEW);

        return AdminOverviewDto.builder()
                .totalProjects(totalProjects)
                .totalUsers(totalUsers)
                .totalCouncils(totalCouncils)
                .activeProjects(activeProjects)
                .projectsGrowth("+15%") // Mock data
                .usersGrowth("+8%")
                .councilsGrowth("+2%")
                .activeProjectsGrowth("+12%")
                .build();
    }

    /**
     * 2. Projects by Status - Pie Chart Data
     */
    public Map<String, Long> getProjectsByStatus() {
        Map<String, Long> result = new LinkedHashMap<>();

        result.put("PENDING", projectRepository.countByStatus(ProjectStatus.PENDING));
        result.put("APPROVED", projectRepository.countByStatus(ProjectStatus.APPROVED));
        result.put("IN_REVIEW", projectRepository.countByStatus(ProjectStatus.IN_REVIEW));
        result.put("COMPLETED", projectRepository.countByStatus(ProjectStatus.COMPLETED));
        result.put("REJECTED", projectRepository.countByStatus(ProjectStatus.REJECTED));
        result.put("ARCHIVED", projectRepository.countByStatus(ProjectStatus.ARCHIVED));

        return result;
    }

    /**
     * 3. Projects Timeline - Bar Chart theo tháng
     */
    public ProjectsTimelineDto getProjectsTimeline(int year, int months) {
        List<String> labels = new ArrayList<>();
        List<Long> created = new ArrayList<>();
        List<Long> completed = new ArrayList<>();

        LocalDateTime now = LocalDateTime.now();

        for (int i = months - 1; i >= 0; i--) {
            YearMonth yearMonth = YearMonth.from(now.minusMonths(i));
            labels.add(yearMonth.getMonth().toString().substring(0, 3));

            LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
            LocalDateTime end = yearMonth.atEndOfMonth().atTime(23, 59, 59);

            Date startDate = Date.from(start.atZone(ZoneId.systemDefault()).toInstant());
            Date endDate = Date.from(end.atZone(ZoneId.systemDefault()).toInstant());

            long createdCount = projectRepository.countCreatedBetween(startDate, endDate);
            long completedCount = projectRepository.countByStatusAndCreateDateBetween(
                    ProjectStatus.COMPLETED, startDate, endDate);

            created.add(createdCount);
            completed.add(completedCount);
        }

        return ProjectsTimelineDto.builder()
                .labels(labels)
                .created(created)
                .completed(completed)
                .build();
    }

    /**
     * 4. Users by Role - Pie Chart
     */
    public Map<String, Long> getUsersByRole() {
        Map<String, Long> result = new LinkedHashMap<>();

        result.put("STUDENT", accountRepository.countByRoleName("STUDENT"));
        result.put("LECTURER", accountRepository.countByRoleName("LECTURER"));
        result.put("DEAN", accountRepository.countByRoleName("DEAN"));
        result.put("ADMIN", accountRepository.countByRoleName("ADMIN"));

        return result;
    }

    /**
     * 5. Top Active Users
     */
    public List<TopUserDto> getTopUsers(int limit) {
        List<Account> students = accountRepository.findByRoleName("STUDENT");

        return students.stream()
                .map(student -> {
                    long projectsCount = projectRepository.countByOwnerId(student.getId());
                    Double avgScore = projectScoreRepository.getAverageScoreByStudentId(student.getId());

                    return TopUserDto.builder()
                            .userId(student.getId())
                            .userName(student.getName())
                            .role("STUDENT")
                            .projectsCount((int) projectsCount)
                            .averageScore(avgScore != null ? avgScore : 0.0)
                            .build();
                })
                .sorted((a, b) -> Double.compare(b.getAverageScore(), a.getAverageScore()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * 6. Recent Activities
     */
    public PaginatedResponseDto<List<ActivityDto>> getRecentActivities(int page, int limit) {
        // Lấy tất cả activities
        List<Project> allProjects = projectRepository.findAll();

        List<ActivityDto> allActivities = allProjects.stream()
                .map(p -> ActivityDto.builder()
                        .type("PROJECT_CREATED")
                        .userId(p.getOwner().getId())
                        .userName(p.getOwner().getName())
                        .projectId(p.getId())
                        .projectName(p.getName())
                        .description("Created new project")
                        .timestamp(p.getCreateDate().toInstant())
                        .icon("up")
                        .build())
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .collect(Collectors.toList());

        // Pagination logic
        long totalElements = allActivities.size();
        int totalPages = (int) Math.ceil((double) totalElements / limit);
        int startIndex = page * limit;
        int endIndex = Math.min(startIndex + limit, (int) totalElements);

        List<ActivityDto> pagedActivities = (startIndex >= totalElements)
                ? new ArrayList<>()
                : allActivities.subList(startIndex, endIndex);

        PaginatedResponseDto.PaginationInfo pagination = PaginatedResponseDto.PaginationInfo.builder()
                .currentPage(page)
                .pageSize(limit)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .hasNext(page < totalPages - 1)
                .hasPrevious(page > 0)
                .build();

        return PaginatedResponseDto.success(pagedActivities, pagination, "Success");
    }

    /**
     * 7. System Health Metrics
     */
    public SystemHealthDto getSystemHealth() {
        return SystemHealthDto.builder()
                .systemStatus("OPTIMUM")
                .activeUsers(120)
                .responseTime("250ms")
                .uptime("99.9%")
                .storageUsed("65%")
                .build();
    }
}