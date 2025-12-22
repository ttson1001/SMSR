package com.example.smrsservice.service;

import com.example.smrsservice.dto.milestone.*;
import com.example.smrsservice.entity.Account;
import com.example.smrsservice.entity.Milestone;
import com.example.smrsservice.entity.Project;
import com.example.smrsservice.repository.AccountRepository;
import com.example.smrsservice.repository.MilestoneRepository;
import com.example.smrsservice.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MilestoneService {

    private final MilestoneRepository milestoneRepository;
    private final ProjectRepository projectRepository;
    private final AccountRepository accountRepository;

    /**
     * Tạo milestone mới
     */
    @Transactional
    public MilestoneResponseDto createMilestone(MilestoneCreateDto dto) {
        Project project = projectRepository.findById(dto.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found"));

        Account creator = accountRepository.findById(dto.getCreateById())
                .orElseThrow(() -> new RuntimeException("Account not found"));

        // Kiểm tra nếu đánh dấu isFinal, chỉ được có 1 milestone final
        if (Boolean.TRUE.equals(dto.getIsFinal())) {
            boolean existsFinalMilestone = milestoneRepository
                    .existsByProjectIdAndIsFinalTrue(dto.getProjectId());

            if (existsFinalMilestone) {
                throw new RuntimeException("Project already has a final milestone");
            }
        }

        Milestone milestone = new Milestone();
        milestone.setDescription(dto.getDescription());
        milestone.setDueDate(dto.getDueDate());
        milestone.setProject(project);
        milestone.setCreateBy(creator);
        milestone.setStatus("Pending");
        milestone.setProgressPercent(0.0);
        milestone.setIsFinal(dto.getIsFinal() != null ? dto.getIsFinal() : false);

        milestoneRepository.save(milestone);

        return mapToDto(milestone);
    }

    /**
     * Cập nhật milestone
     */
    @Transactional
    public MilestoneResponseDto updateMilestone(Integer id, MilestoneUpdateDto dto) {
        Milestone milestone = milestoneRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Milestone not found"));

        if (dto.getDescription() != null) {
            milestone.setDescription(dto.getDescription());
        }
        if (dto.getStatus() != null) {
            milestone.setStatus(dto.getStatus());
        }
        if (dto.getProgressPercent() != null) {
            milestone.setProgressPercent(dto.getProgressPercent());
        }
        if (dto.getDueDate() != null) {
            milestone.setDueDate(dto.getDueDate());
        }

        milestoneRepository.save(milestone);
        return mapToDto(milestone);
    }

    /**
     * Xóa milestone
     */
    @Transactional
    public void deleteMilestone(Integer id) {
        milestoneRepository.deleteById(id);
    }

    /**
     * Lấy tất cả milestones của project
     */
    public List<MilestoneResponseDto> getMilestonesByProject(Integer projectId) {
        return milestoneRepository.findByProject_Id(projectId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Lấy milestone theo ID
     */
    public MilestoneResponseDto getMilestoneById(Integer id) {
        Milestone milestone = milestoneRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Milestone not found"));
        return mapToDto(milestone);
    }

    /**
     * Thành viên nhóm nộp report cho milestone
     */
    @Transactional
    public MilestoneResponseDto submitReport(
            Integer milestoneId,
            MilestoneSubmitReportDto dto,
            Authentication authentication) {

        // Lấy milestone
        Milestone milestone = milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new RuntimeException("Milestone not found"));

        // Lấy user hiện tại
        Account currentUser = getCurrentAccount(authentication);

        // ✅ Kiểm tra quyền nộp report - Tất cả thành viên đều được nộp
        Project project = milestone.getProject();

        // Kiểm tra owner
        boolean isOwner = project.getOwner() != null &&
                project.getOwner().getId().equals(currentUser.getId());

        // Kiểm tra member
        boolean isMember = project.getMembers() != null &&
                project.getMembers().stream()
                        .anyMatch(m -> m.getAccount().getId().equals(currentUser.getId()));

        if (!isOwner && !isMember) {
            throw new RuntimeException("Only project members can submit milestone report");
        }

        // Kiểm tra đã nộp report chưa
        if (milestone.getReportUrl() != null) {
            throw new RuntimeException("Report already submitted for this milestone");
        }

        // ✅ Kiểm tra bắt buộc phải có file
        if (dto.getReportUrl() == null || dto.getReportUrl().trim().isEmpty()) {
            throw new RuntimeException("Bạn chưa nộp file báo cáo. Vui lòng tải file lên trước khi nộp.");
        }

        // Cập nhật thông tin report
        milestone.setReportUrl(dto.getReportUrl());
        milestone.setReportComment(dto.getReportComment());
        milestone.setReportSubmittedAt(new Date());
        milestone.setReportSubmittedBy(currentUser);

        // Cập nhật status và progress
        milestone.setStatus("Submitted");
        milestone.setProgressPercent(100.0);

        milestoneRepository.save(milestone);

        return mapToDto(milestone);
    }

    /**
     * Lấy final milestone của project
     * ✅ THAY ĐỔI: Dùng method với 2 tham số
     */
    public MilestoneResponseDto getFinalMilestone(Integer projectId) {
        Milestone milestone = milestoneRepository
                .findFirstByProjectIdAndIsFinalOrderByIdDesc(projectId, true)  // ✅ THÊM true
                .orElseThrow(() -> new RuntimeException("Final milestone not found for this project"));

        return mapToDto(milestone);
    }

    /**
     * Map Milestone entity sang DTO
     */
    private MilestoneResponseDto mapToDto(Milestone milestone) {
        return new MilestoneResponseDto(
                milestone.getId(),
                milestone.getDescription(),
                milestone.getStatus(),
                milestone.getProgressPercent(),
                milestone.getCreateDate(),
                milestone.getDueDate(),
                milestone.getProject() != null ? milestone.getProject().getId() : null,
                milestone.getCreateBy() != null ? milestone.getCreateBy().getId() : null,

                // Thông tin report
                milestone.getIsFinal(),
                milestone.getReportUrl(),
                milestone.getReportSubmittedAt(),
                milestone.getReportSubmittedBy() != null ? milestone.getReportSubmittedBy().getId() : null,
                milestone.getReportSubmittedBy() != null ? milestone.getReportSubmittedBy().getName() : null,
                milestone.getReportComment()
        );
    }

    /**
     * Lấy thông tin account hiện tại từ Authentication
     */
    private Account getCurrentAccount(Authentication authentication) {
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
                    .orElseThrow(() -> new RuntimeException("Account not found with email: " + email));
        }

        throw new RuntimeException("Invalid authentication principal type");
    }
}