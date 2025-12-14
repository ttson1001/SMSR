package com.example.smrsservice.controller;

import com.example.smrsservice.common.ProjectStatus;
import com.example.smrsservice.dto.common.ResponseDto;
import com.example.smrsservice.dto.milestone.MilestoneResponseDto;
import com.example.smrsservice.dto.project.*;
import com.example.smrsservice.dto.score.ProjectScoreCreateDto;
import com.example.smrsservice.dto.score.ProjectScoreUpdateDto;  // ⭐ THÊM IMPORT
import com.example.smrsservice.dto.score.ProjectScoreResponseDto;
import com.example.smrsservice.service.MilestoneFinalReportService;
import com.example.smrsservice.service.ProjectService;
import com.example.smrsservice.service.ProjectScoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final ProjectScoreService projectScoreService;
    private final MilestoneFinalReportService milestoneFinalReportService;

    @GetMapping
    public ResponseEntity<Page<ProjectResponse>> getAllProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) ProjectStatus status,
            @RequestParam(required = false) Integer ownerId,
            @RequestParam(required = false) Integer majorId,
            @RequestParam(required = false) Boolean isMine,
            Authentication authentication
    ) {
        Page<ProjectResponse> projects = projectService.getAllProjects(
                page, size, sortBy, sortDir, name, status, ownerId, majorId, isMine, authentication
        );
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ProjectResponse>> searchProjects(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        Page<ProjectResponse> projects = projectService.searchProjects(
                name, description, page, size, sortBy, sortDir
        );
        return ResponseEntity.ok(projects);
    }

    @PostMapping
    public ResponseEntity<ResponseDto<ProjectResponse>> createProject(
            @RequestBody ProjectCreateDto dto,
            Authentication authentication
    ) {
        ResponseDto<ProjectResponse> response = projectService.createProject(dto, authentication);
        return ResponseEntity.status(
                response.isSuccess() ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST
        ).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDto<ProjectDetailResponse>> getProjectDetail(
            @PathVariable("id") Integer id
    ) {
        ResponseDto<ProjectDetailResponse> response = projectService.getProjectDetail(id);
        return ResponseEntity.status(
                response.isSuccess() ? HttpStatus.OK : HttpStatus.NOT_FOUND
        ).body(response);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ProjectResponse> updateProjectStatus(
            @PathVariable("id") Integer id,
            @RequestBody UpdateProjectStatusRequest req
    ) {
        ProjectResponse response = projectService.updateProjectStatus(id, req);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/pick")
    public ResponseEntity<ResponseDto<ProjectResponse>> pickArchivedProject(
            @PathVariable("id") Integer id,
            @RequestBody PickProjectRequest request,
            Authentication authentication
    ) {
        ResponseDto<ProjectResponse> response = projectService.pickArchivedProject(
                id, request, authentication
        );
        return ResponseEntity.status(
                response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST
        ).body(response);
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseDto<List<ProjectImportDto>>> importProjectsFromExcel(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        ResponseDto<List<ProjectImportDto>> response = projectService.importProjectsFromExcel(file, authentication);
        return ResponseEntity.status(response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST).body(response);
    }

    // ==================== NEW ENDPOINTS ====================

    @GetMapping("/to-review")
    public ResponseEntity<ResponseDto<List<ProjectReviewDto>>> getProjectsToReview(
            Authentication authentication
    ) {
        ResponseDto<List<ProjectReviewDto>> response = projectService.getProjectsToReview(authentication);
        return ResponseEntity.status(
                response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST
        ).body(response);
    }

    @PostMapping("/{projectId}/scores")
    public ResponseEntity<ResponseDto<ProjectScoreResponseDto>> scoreProject(
            @PathVariable Integer projectId,
            @RequestBody ProjectScoreCreateDto dto,
            Authentication authentication
    ) {
        ResponseDto<ProjectScoreResponseDto> response = projectScoreService.createScore(dto, authentication);
        return ResponseEntity.status(
                response.isSuccess() ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST
        ).body(response);
    }

    @PutMapping("/{projectId}/scores/{scoreId}")
    public ResponseEntity<ResponseDto<ProjectScoreResponseDto>> updateScore(
            @PathVariable Integer projectId,
            @PathVariable Integer scoreId,
            @RequestBody ProjectScoreUpdateDto dto,
            Authentication authentication
    ) {
        ResponseDto<ProjectScoreResponseDto> response = projectScoreService.updateScore(
                scoreId, dto, authentication
        );
        return ResponseEntity.status(
                response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST
        ).body(response);
    }

    @GetMapping("/{projectId}/scores")
    public ResponseEntity<ResponseDto<List<ProjectScoreResponseDto>>> getProjectScores(
            @PathVariable Integer projectId,
            Authentication authentication
    ) {
        ResponseDto<List<ProjectScoreResponseDto>> response = projectScoreService.getScoresByProject(
                projectId
        );
        return ResponseEntity.status(
                response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST
        ).body(response);
    }

    @GetMapping("/{projectId}/scores/my-score")
    public ResponseEntity<ResponseDto<ProjectScoreResponseDto>> getMyScore(
            @PathVariable Integer projectId,
            Authentication authentication
    ) {
        ResponseDto<ProjectScoreResponseDto> response = projectService.getMyScoreForProject(
                projectId, authentication
        );
        return ResponseEntity.status(
                response.isSuccess() ? HttpStatus.OK : HttpStatus.NOT_FOUND
        ).body(response);
    }

    @GetMapping("/{projectId}/scores/average")
    public ResponseEntity<ResponseDto<Double>> getAverageScore(
            @PathVariable Integer projectId
    ) {
        ResponseDto<Double> response = projectScoreService.getAverageScoreByProject(projectId);
        return ResponseEntity.status(
                response.isSuccess() ? HttpStatus.OK : HttpStatus.NOT_FOUND
        ).body(response);
    }

    @GetMapping("/{projectId}/final-reports")
    public ResponseEntity<ResponseDto<MilestoneResponseDto>> getFinalReports(
            @PathVariable Integer projectId,
            Authentication authentication
    ) {
        ResponseDto<MilestoneResponseDto> response = milestoneFinalReportService.getFinalReportByProject(
                projectId
        );
        return ResponseEntity.status(
                response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST
        ).body(response);
    }

    /**
     * API cho user xem final report của các project mà họ tham gia
     * Áp dụng cho cả STUDENT và LECTURER
     *
     * @param authentication - thông tin user hiện tại
     * @return danh sách projects có final report
     */
    @GetMapping("/my-final-reports")
    public ResponseEntity<ResponseDto<List<MyProjectReviewDto>>> getMyProjectsWithFinalReport(
            Authentication authentication) {
        return ResponseEntity.ok(projectService.getMyProjectsWithFinalReport(authentication));
    }

    @GetMapping("/mentoring")
    public ResponseEntity<ResponseDto<List<MentoringProjectDto>>> getMyMentoringProjects(
            Authentication authentication) {

        ResponseDto<List<MentoringProjectDto>> response =
                projectService.getMyMentoringProjects(authentication);

        return ResponseEntity.status(
                response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST
        ).body(response);
    }
}