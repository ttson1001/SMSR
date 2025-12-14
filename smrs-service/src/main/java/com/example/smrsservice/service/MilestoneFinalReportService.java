package com.example.smrsservice.service;

import com.example.smrsservice.dto.common.ResponseDto;
import com.example.smrsservice.dto.milestone.MilestoneResponseDto;
import com.example.smrsservice.dto.upload.FileUploadResponse;
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
import org.springframework.web.multipart.MultipartFile;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MilestoneFinalReportService {

    private final MilestoneRepository milestoneRepository;
    private final AccountRepository accountRepository;

    @Transactional
    public ResponseDto<MilestoneResponseDto> reviewFinalReport(
            Integer milestoneId,
            String status,
            String reviewComment,
            Double progressPercent,
            Authentication authentication) {

        try {
            Account reviewer = currentAccount(authentication);

            String role = reviewer.getRole().getRoleName();
            if (!"ADMIN".equalsIgnoreCase(role) && !"LECTURER".equalsIgnoreCase(role)) {
                return ResponseDto.fail("You are not authorized to review final report");
            }

            Milestone milestone = milestoneRepository.findById(milestoneId)
                    .orElseThrow(() -> new RuntimeException("Milestone not found"));

            if (milestone.getIsFinal() == null || !milestone.getIsFinal()) {
                return ResponseDto.fail("This milestone is not a final milestone");
            }

            if (status != null && !status.isBlank()) {
                milestone.setStatus(status);
            }
            if (progressPercent != null) {
                milestone.setProgressPercent(progressPercent);
            }
            if (reviewComment != null && !reviewComment.isBlank()) {
                milestone.setReportComment(reviewComment);
            }

            milestoneRepository.save(milestone);

            return ResponseDto.success(toResponseDto(milestone), "Final report reviewed successfully");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseDto.fail(e.getMessage());
        }
    }

    public ResponseDto<MilestoneResponseDto> getFinalReportByProject(Integer projectId) {
        try {
            Milestone milestone = milestoneRepository
                    .findFirstByProjectIdAndIsFinalOrderByIdDesc(projectId, true)
                    .orElseThrow(() -> new RuntimeException("Final milestone not found for this project"));

            return ResponseDto.success(toResponseDto(milestone), "OK");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseDto.fail(e.getMessage());
        }
    }

    public ResponseDto<List<MilestoneResponseDto>> getAllFinalReports() {
        try {
            List<Milestone> milestones = milestoneRepository.findByIsFinal(true);
            List<MilestoneResponseDto> result = milestones.stream()
                    .map(this::toResponseDto)
                    .collect(Collectors.toList());
            return ResponseDto.success(result, "OK");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseDto.fail(e.getMessage());
        }
    }

    public ResponseDto<List<MilestoneResponseDto>> getPendingFinalReports() {
        try {
            List<Milestone> milestones = milestoneRepository
                    .findByIsFinalAndStatus(true, "Submitted");
            List<MilestoneResponseDto> result = milestones.stream()
                    .map(this::toResponseDto)
                    .collect(Collectors.toList());
            return ResponseDto.success(result, "OK");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseDto.fail(e.getMessage());
        }
    }

    public ResponseDto<List<MilestoneResponseDto>> getMyFinalReports(Authentication authentication) {
        try {
            Account submitter = currentAccount(authentication);
            List<Milestone> milestones = milestoneRepository
                    .findByReportSubmittedById(submitter.getId());

            // chỉ lấy những milestone là final
            List<MilestoneResponseDto> result = milestones.stream()
                    .filter(m -> Boolean.TRUE.equals(m.getIsFinal()))
                    .map(this::toResponseDto)
                    .collect(Collectors.toList());

            return ResponseDto.success(result, "OK");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseDto.fail(e.getMessage());
        }
    }

    // ================== Helper Methods ==================

    private Account currentAccount(Authentication authentication) {
        if (authentication == null) throw new RuntimeException("User not authenticated");
        Object principal = authentication.getPrincipal();
        if (principal instanceof Account) return (Account) principal;
        if (principal instanceof String) {
            String email = (String) principal;
            return accountRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Account not found with email: " + email));
        }
        throw new RuntimeException("Invalid authentication principal type");
    }

    private MilestoneResponseDto toResponseDto(Milestone milestone) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        return MilestoneResponseDto.builder()
                .id(milestone.getId())
                .description(milestone.getDescription())
                .status(milestone.getStatus())
                .progressPercent(milestone.getProgressPercent())
                .createById(milestone.getCreateBy() != null ? milestone.getCreateBy().getId() : null)
                .createDate(milestone.getCreateDate() != null ? milestone.getCreateDate() : null)
                .dueDate(milestone.getDueDate() != null ? milestone.getDueDate() : null)
                .projectId(milestone.getProject() != null ? milestone.getProject().getId() : null)
                .isFinal(milestone.getIsFinal())
                .reportUrl(milestone.getReportUrl())
                .reportComment(milestone.getReportComment())
                .reportSubmittedAt(milestone.getReportSubmittedAt() != null ?
                        milestone.getReportSubmittedAt() : null)
                .reportSubmittedById(milestone.getReportSubmittedBy() != null ?
                        milestone.getReportSubmittedBy().getId() : null)
                .reportSubmittedByName(milestone.getReportSubmittedBy() != null ?
                        milestone.getReportSubmittedBy().getName() : null)
                .build();
    }
}
