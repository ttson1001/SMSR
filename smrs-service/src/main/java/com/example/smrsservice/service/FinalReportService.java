package com.example.smrsservice.service;

import com.example.smrsservice.dto.common.ResponseDto;
import com.example.smrsservice.dto.report.FinalReportCreateDto;
import com.example.smrsservice.dto.report.FinalReportResponseDto;
import com.example.smrsservice.dto.report.FinalReportUpdateDto;
import com.example.smrsservice.dto.upload.FileUploadResponse;
import com.example.smrsservice.entity.*;
import com.example.smrsservice.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FinalReportService {
    private final FinalReportRepository finalReportRepository;
    private final ProjectRepository projectRepository;
    private final AccountRepository accountRepository;
    private final UploadService uploadService;
    private final CouncilMemberRepository councilMemberRepository;
    private final ProjectCouncilRepository projectCouncilRepository;

    @Transactional
    public ResponseDto<FinalReportResponseDto> submitFinalReport(FinalReportCreateDto dto, MultipartFile file, Authentication authentication) {
        try {
            Account submitter = currentAccount(authentication);

            Project project = projectRepository.findById(dto.getProjectId())
                    .orElseThrow(() -> new RuntimeException("Project not found"));

            // Check if user is owner of project (can be STUDENT or LECTURER)
            boolean isOwner = project.getOwner().getId().equals(submitter.getId());
            if (!isOwner) {
                return ResponseDto.fail("You are not authorized to submit report for this project");
            }

            if (file == null || file.isEmpty()) {
                return ResponseDto.fail("File is required");
            }

            Integer latestVersion = finalReportRepository.findTopByProjectIdOrderByVersionDesc(dto.getProjectId())
                    .map(FinalReport::getVersion)
                    .orElse(0);

            // Upload file lên Cloudinary
            FileUploadResponse uploadResponse = uploadService.uploadFileWithDetails(file);

            FinalReport report = FinalReport.builder()
                    .project(project)
                    .submittedBy(submitter)
                    .reportTitle(dto.getReportTitle())
                    .description(dto.getDescription())
                    .filePath(uploadResponse.getUrl())
                    .fileName(uploadResponse.getFileName())
                    .fileType(uploadResponse.getFileType())
                    .fileSize(uploadResponse.getFileSize())
                    .version(latestVersion + 1)
                    .status("PENDING")
                    .build();

            finalReportRepository.save(report);

            return ResponseDto.success(toResponseDto(report), "Final report submitted successfully");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseDto.fail(e.getMessage());
        }
    }

    @Transactional
    public ResponseDto<FinalReportResponseDto> createFinalReport(
            FinalReportCreateDto dto,
            Authentication authentication) {
        try {
            Account submitter = currentAccount(authentication);

            Project project = projectRepository.findById(dto.getProjectId())
                    .orElseThrow(() -> new RuntimeException("Project not found"));

            // Check if user is owner of project
            boolean isOwner = project.getOwner().getId().equals(submitter.getId());
            if (!isOwner) {
                return ResponseDto.fail("You are not authorized to submit report for this project");
            }

            // Lấy version hiện tại
            Integer latestVersion = finalReportRepository
                    .findTopByProjectIdOrderByVersionDesc(dto.getProjectId())
                    .map(FinalReport::getVersion)
                    .orElse(0);

            FinalReport report = FinalReport.builder()
                    .project(project)
                    .submittedBy(submitter)
                    .reportTitle(dto.getReportTitle())
                    .description(dto.getDescription())
                    .filePath(dto.getFilePath())      // Từ DTO
                    .fileName(dto.getFileName())       // Từ DTO
                    .fileType(dto.getFileType())       // Từ DTO
                    .fileSize(dto.getFileSize())       // Từ DTO
                    .version(latestVersion + 1)
                    .status("PENDING")
                    .build();

            finalReportRepository.save(report);

            return ResponseDto.success(toResponseDto(report), "Final report submitted successfully");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseDto.fail(e.getMessage());
        }
    }

    @Transactional
    public ResponseDto<FinalReportResponseDto> updateReportStatus(Integer reportId, FinalReportUpdateDto dto, Authentication authentication) {
        try {
            Account user = currentAccount(authentication);

            String role = user.getRole().getRoleName();
            if (!"ADMIN".equalsIgnoreCase(role) && !"LECTURER".equalsIgnoreCase(role)) {
                return ResponseDto.fail("You are not authorized to update report status");
            }

            FinalReport report = finalReportRepository.findById(reportId)
                    .orElseThrow(() -> new RuntimeException("Report not found"));

            if (dto.getStatus() != null) report.setStatus(dto.getStatus());
            if (dto.getRemarks() != null) report.setRemarks(dto.getRemarks());
            if (dto.getReportTitle() != null) report.setReportTitle(dto.getReportTitle());
            if (dto.getDescription() != null) report.setDescription(dto.getDescription());

            finalReportRepository.save(report);

            return ResponseDto.success(toResponseDto(report), "Report status updated successfully");

        } catch (Exception e) {
            return ResponseDto.fail(e.getMessage());
        }
    }

    public ResponseDto<List<FinalReportResponseDto>> getReportsByProject(Integer projectId) {
        try {
            List<FinalReport> reports = finalReportRepository.findAllVersionsByProjectId(projectId);
            List<FinalReportResponseDto> result = reports.stream()
                    .map(this::toResponseDto)
                    .collect(Collectors.toList());
            return ResponseDto.success(result, "OK");
        } catch (Exception e) {
            return ResponseDto.fail(e.getMessage());
        }
    }

    public ResponseDto<FinalReportResponseDto> getLatestReport(Integer projectId) {
        try {
            FinalReport report = finalReportRepository.findTopByProjectIdOrderByVersionDesc(projectId)
                    .orElseThrow(() -> new RuntimeException("No report found for this project"));
            return ResponseDto.success(toResponseDto(report), "OK");
        } catch (Exception e) {
            return ResponseDto.fail(e.getMessage());
        }
    }

    public ResponseDto<List<FinalReportResponseDto>> getPendingReports() {
        try {
            List<FinalReport> reports = finalReportRepository.findByStatus("PENDING");
            List<FinalReportResponseDto> result = reports.stream()
                    .map(this::toResponseDto)
                    .collect(Collectors.toList());
            return ResponseDto.success(result, "OK");
        } catch (Exception e) {
            return ResponseDto.fail(e.getMessage());
        }
    }

    public ResponseDto<List<FinalReportResponseDto>> getAllReports() {
        try {
            List<FinalReport> reports = finalReportRepository.findAll();
            List<FinalReportResponseDto> result = reports.stream()
                    .map(this::toResponseDto)
                    .collect(Collectors.toList());
            return ResponseDto.success(result, "OK");
        } catch (Exception e) {
            return ResponseDto.fail(e.getMessage());
        }
    }

    public ResponseDto<List<FinalReportResponseDto>> getReportsByStatus(String status) {
        try {
            List<FinalReport> reports = finalReportRepository.findByStatus(status);
            List<FinalReportResponseDto> result = reports.stream()
                    .map(this::toResponseDto)
                    .collect(Collectors.toList());
            return ResponseDto.success(result, "OK");
        } catch (Exception e) {
            return ResponseDto.fail(e.getMessage());
        }
    }

    public ResponseDto<List<FinalReportResponseDto>> getMySubmittedReports(Authentication authentication) {
        try {
            Account submitter = currentAccount(authentication);
            List<FinalReport> reports = finalReportRepository.findBySubmittedById(submitter.getId());
            List<FinalReportResponseDto> result = reports.stream()
                    .map(this::toResponseDto)
                    .collect(Collectors.toList());
            return ResponseDto.success(result, "OK");
        } catch (Exception e) {
            return ResponseDto.fail(e.getMessage());
        }
    }

    private FinalReportResponseDto toResponseDto(FinalReport report) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return FinalReportResponseDto.builder()
                .id(report.getId())
                .projectId(report.getProject().getId())
                .projectName(report.getProject().getName())
                .submittedById(report.getSubmittedBy().getId())
                .submittedByName(report.getSubmittedBy().getName())
                .reportTitle(report.getReportTitle())
                .description(report.getDescription())
                .filePath(report.getFilePath())
                .fileName(report.getFileName())
                .fileType(report.getFileType())
                .fileSize(report.getFileSize())
                .submissionDate(report.getSubmissionDate() != null ? sdf.format(report.getSubmissionDate()) : null)
                .status(report.getStatus())
                .version(report.getVersion())
                .remarks(report.getRemarks())
                .build();
    }

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

    public ResponseDto<List<FinalReportResponseDto>> getReportsByProject(
            Integer projectId,
            Authentication authentication) {
        try {
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new RuntimeException("Project not found"));

            List<FinalReport> reports = finalReportRepository
                    .findByProjectIdOrderByVersionDesc(projectId);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            List<FinalReportResponseDto> dtos = reports.stream()
                    .map(report -> FinalReportResponseDto.builder()
                            .id(report.getId())
                            .projectId(report.getProject().getId())
                            .projectName(report.getProject().getName())
                            .reportTitle(report.getReportTitle())
                            .description(report.getDescription())
                            .filePath(report.getFilePath())
                            .fileName(report.getFileName())
                            .fileType(report.getFileType())
                            .fileSize(report.getFileSize())
                            .version(report.getVersion())
                            .status(report.getStatus())
                            .submissionDate(report.getSubmissionDate() != null ?
                                    sdf.format(report.getSubmissionDate()) : null)
                            .submittedById(report.getSubmittedBy().getId())
                            .submittedByName(report.getSubmittedBy().getName())
                            .build())
                    .collect(Collectors.toList());

            return ResponseDto.success(dtos, "Found " + dtos.size() + " reports");

        } catch (Exception e) {
            return ResponseDto.fail(e.getMessage());
        }
    }

    /**
     * Lấy report mới nhất của project
     */
    public ResponseDto<FinalReportResponseDto> getLatestReportByProject(
            Integer projectId,
            Authentication authentication) {
        try {
            FinalReport report = finalReportRepository
                    .findTopByProjectIdOrderByVersionDesc(projectId)
                    .orElseThrow(() -> new RuntimeException("No report found for this project"));

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            FinalReportResponseDto dto = FinalReportResponseDto.builder()
                    .id(report.getId())
                    .projectId(report.getProject().getId())
                    .projectName(report.getProject().getName())
                    .reportTitle(report.getReportTitle())
                    .description(report.getDescription())
                    .filePath(report.getFilePath())
                    .fileName(report.getFileName())
                    .fileType(report.getFileType())
                    .fileSize(report.getFileSize())
                    .version(report.getVersion())
                    .status(report.getStatus())
                    .submissionDate(report.getSubmissionDate() != null ?
                            sdf.format(report.getSubmissionDate()) : null)
                    .submittedById(report.getSubmittedBy().getId())
                    .submittedByName(report.getSubmittedBy().getName())
                    .build();

            return ResponseDto.success(dto, "Latest report found");

        } catch (Exception e) {
            return ResponseDto.fail(e.getMessage());
        }
    }




}
