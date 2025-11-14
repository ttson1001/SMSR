package com.example.smrsservice.service;

import com.example.smrsservice.common.ProjectStatus;
import com.example.smrsservice.dto.common.ResponseDto;
import com.example.smrsservice.dto.statistics.StatisticsResponse;
import com.example.smrsservice.entity.Account;
import com.example.smrsservice.repository.AccountRepository;
import com.example.smrsservice.repository.ProjectMemberRepository;
import com.example.smrsservice.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final ProjectRepository projectRepository;
    private final AccountRepository accountRepository;
    private final ProjectMemberRepository projectMemberRepository;

    public ResponseDto<StatisticsResponse> getMyStatistics() {
        try {
            Account currentUser = getCurrentAccount();

            if (currentUser.getRole() == null) {
                return ResponseDto.fail("User has no role");
            }

            String roleName = currentUser.getRole().getRoleName();

            StatisticsResponse stats;

            switch (roleName.toUpperCase()) {
                case "ADMIN":
                    stats = getAdminStatistics();
                    break;
                case "DEAN":
                    stats = getDeanStatistics();
                    break;
                case "LECTURER":
                    stats = getLecturerStatistics(currentUser.getId());
                    break;
                case "STUDENT":
                    stats = getStudentStatistics(currentUser.getId());
                    break;
                default:
                    return ResponseDto.fail("Invalid role");
            }

            return ResponseDto.success(stats, "Get statistics successfully");

        } catch (Exception e) {
            return ResponseDto.fail(e.getMessage());
        }
    }

    private StatisticsResponse getAdminStatistics() {
        long totalProjects = projectRepository.count();
        long totalAccounts = accountRepository.count();

        long totalStudents = accountRepository.countByRoleName("STUDENT");
        long totalLecturers = accountRepository.countByRoleName("LECTURER");
        long totalDeans = accountRepository.countByRoleName("DEAN");
        long totalAdmins = accountRepository.countByRoleName("ADMIN");

        return StatisticsResponse.builder()
                .totalProjects(totalProjects)
                .totalAccounts(totalAccounts)
                .totalStudents(totalStudents)
                .totalLecturers(totalLecturers)
                .totalDeans(totalDeans)
                .totalAdmins(totalAdmins)
                .build();
    }

    private StatisticsResponse getDeanStatistics() {
        Map<String, Long> projectsByStatus = new HashMap<>();

        for (ProjectStatus status : ProjectStatus.values()) {
            long count = projectRepository.countByStatus(status);
            projectsByStatus.put(status.getJsonName(), count);
        }

        long archivedProjects = projectRepository.countByStatus(ProjectStatus.ARCHIVED);
        long activeProjects = projectRepository.countByStatus(ProjectStatus.IN_PROGRESS);
        long completedProjects = projectRepository.countByStatus(ProjectStatus.COMPLETED);

        return StatisticsResponse.builder()
                .projectsByStatus(projectsByStatus)
                .archivedProjects(archivedProjects)
                .activeProjects(activeProjects)
                .completedProjects(completedProjects)
                .build();
    }

    private StatisticsResponse getLecturerStatistics(Integer lecturerId) {
        long myProjects = projectRepository.countByOwnerId(lecturerId);
        long mentoringProjects = projectMemberRepository.countActiveMentoringProjects(lecturerId);

        return StatisticsResponse.builder()
                .myProjects(myProjects)
                .mentoringProjects(mentoringProjects)
                .myTasks(0L)
                .myCompletedTasks(0L)
                .myPendingTasks(0L)
                .build();
    }

    private StatisticsResponse getStudentStatistics(Integer studentId) {
        long myProjects = projectRepository.countByOwnerId(studentId);
        long studentProjects = projectMemberRepository.countActiveStudentProjects(studentId);

        return StatisticsResponse.builder()
                .myProjects(myProjects)
                .studentProjects(studentProjects)
                .myTasks(0L)
                .myCompletedTasks(0L)
                .myPendingTasks(0L)
                .build();
    }

    private Account getCurrentAccount() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new RuntimeException("User not authenticated");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof Account) {
            return (Account) principal;
        }

        if (principal instanceof String) {
            String email = (String) principal;
            return accountRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Account not found"));
        }

        throw new RuntimeException("Invalid authentication principal");
    }
}