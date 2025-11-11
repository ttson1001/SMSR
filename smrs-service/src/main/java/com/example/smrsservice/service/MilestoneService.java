package com.example.smrsservice.service;
import com.example.smrsservice.dto.milestone.MilestoneCreateDto;
import com.example.smrsservice.dto.milestone.MilestoneResponseDto;
import com.example.smrsservice.dto.milestone.MilestoneSubmitReportDto;
import com.example.smrsservice.dto.milestone.MilestoneUpdateDto;
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

    @Transactional
    public MilestoneResponseDto createMilestone(MilestoneCreateDto dto) {
        Project project = projectRepository.findById(dto.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found"));

        Account creator = accountRepository.findById(dto.getCreateById())
                .orElseThrow(() -> new RuntimeException("Account not found"));

        // ✅ Kiểm tra nếu đánh dấu isFinal, chỉ được có 1 milestone final
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
        milestone.setIsFinal(dto.getIsFinal() != null ? dto.getIsFinal() : false);  // ✅ Set isFinal
        milestone.setStatus("Pending");  // ✅ Default status

        milestoneRepository.save(milestone);

        return mapToDto(milestone);
    }

    @Transactional
    public MilestoneResponseDto updateMilestone(Integer id, MilestoneUpdateDto dto) {
        Milestone milestone = milestoneRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Milestone not found"));

        milestone.setDescription(dto.getDescription());
        milestone.setStatus(dto.getStatus());
        milestone.setProgressPercent(dto.getProgressPercent());
        milestone.setDueDate(dto.getDueDate());

        milestoneRepository.save(milestone);
        return mapToDto(milestone);
    }

    @Transactional
    public void deleteMilestone(Integer id) {
        milestoneRepository.deleteById(id);
    }

    public List<MilestoneResponseDto> getMilestonesByProject(Integer projectId) {
        return milestoneRepository.findByProject_Id(projectId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public MilestoneResponseDto getMilestoneById(Integer id) {
        Milestone milestone = milestoneRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Milestone not found"));
        return mapToDto(milestone);
    }

    // ✅ METHOD MỚI: Leader nộp report
    @Transactional
    public MilestoneResponseDto submitReport(
            Integer milestoneId,
            MilestoneSubmitReportDto dto,
            Authentication authentication) {

        // Lấy milestone
        Milestone milestone = milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new RuntimeException("Milestone not found"));

        // Lấy thông tin user hiện tại
        Account currentUser = getCurrentAccount(authentication);

        // Kiểm tra user có phải owner của project không
        if (!milestone.getProject().getOwner().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Only project owner can submit report");
        }

        // Kiểm tra đã nộp report chưa
        if (milestone.getReportUrl() != null) {
            throw new RuntimeException("Report already submitted for this milestone");
        }

        // Cập nhật thông tin report
        milestone.setReportUrl(dto.getReportUrl());
        milestone.setReportComment(dto.getReportComment());
        milestone.setReportSubmittedAt(new Date());
        milestone.setReportSubmittedBy(currentUser);
        milestone.setStatus("Submitted");  // Đổi status thành Submitted

        milestoneRepository.save(milestone);

        return mapToDto(milestone);
    }

    // ✅ METHOD MỚI: Lấy milestone cuối cùng của project
    public MilestoneResponseDto getFinalMilestone(Integer projectId) {
        Milestone milestone = milestoneRepository
                .findByProjectIdAndIsFinalTrue(projectId)
                .orElseThrow(() -> new RuntimeException("Final milestone not found for this project"));

        return mapToDto(milestone);
    }

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

                // ✅ MAP CÁC FIELDS MỚI
                milestone.getIsFinal(),
                milestone.getReportUrl(),
                milestone.getReportSubmittedAt(),
                milestone.getReportSubmittedBy() != null ? milestone.getReportSubmittedBy().getId() : null,
                milestone.getReportSubmittedBy() != null ? milestone.getReportSubmittedBy().getName() : null,
                milestone.getReportComment()
        );
    }

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
