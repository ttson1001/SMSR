package com.example.smrsservice.controller;

import com.example.smrsservice.dto.common.ResponseDto;
import com.example.smrsservice.dto.projectpublication.CreatePublicationRequest;
import com.example.smrsservice.dto.projectpublication.ProjectPublicationDto;
import com.example.smrsservice.dto.projectpublication.UpdatePublicationRequest;
import com.example.smrsservice.service.ProjectPublicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/publications")
@RequiredArgsConstructor
public class ProjectPublicationController {

    private final ProjectPublicationService publicationService;

    /**
     * POST /api/publications
     * Đăng ký publication mới
     * Role: LECTURER, STUDENT (phải là member của project)
     */
    @PostMapping
    public ResponseEntity<ResponseDto<ProjectPublicationDto>> createPublication(
            @RequestBody CreatePublicationRequest request,
            Authentication authentication) {

        ResponseDto<ProjectPublicationDto> response = publicationService.createPublication(
                request, authentication
        );

        return ResponseEntity.status(
                response.isSuccess() ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST
        ).body(response);
    }

    /**
     * PUT /api/publications/{id}
     * Cập nhật publication
     * Role: Chỉ author đăng ký
     */
    @PutMapping("/{id}")
    public ResponseEntity<ResponseDto<ProjectPublicationDto>> updatePublication(
            @PathVariable Integer id,
            @RequestBody UpdatePublicationRequest request,
            Authentication authentication) {

        ResponseDto<ProjectPublicationDto> response = publicationService.updatePublication(
                id, request, authentication
        );

        return ResponseEntity.status(
                response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST
        ).body(response);
    }

    /**
     * DELETE /api/publications/{id}
     * Xóa publication
     * Role: Chỉ author đăng ký
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDto<String>> deletePublication(
            @PathVariable Integer id,
            Authentication authentication) {

        ResponseDto<String> response = publicationService.deletePublication(
                id, authentication
        );

        return ResponseEntity.status(
                response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST
        ).body(response);
    }

    /**
     * GET /api/publications
     * Lấy tất cả publications
     * Role: ADMIN only
     */
    @GetMapping
    public ResponseEntity<ResponseDto<List<ProjectPublicationDto>>> getAllPublications(
            Authentication authentication) {

        ResponseDto<List<ProjectPublicationDto>> response = publicationService.getAllPublications(
                authentication
        );

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/publications/{id}
     * Xem chi tiết publication
     * Role: All
     */
    @GetMapping("/{id}")
    public ResponseEntity<ResponseDto<ProjectPublicationDto>> getPublicationById(
            @PathVariable Integer id) {

        ResponseDto<ProjectPublicationDto> response = publicationService.getPublicationById(id);

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/publications/project/{projectId}
     * Lấy publications của 1 project
     * Role: All
     */
    @GetMapping("/project/{projectId}")
    public ResponseEntity<ResponseDto<List<ProjectPublicationDto>>> getPublicationsByProject(
            @PathVariable Integer projectId) {

        ResponseDto<List<ProjectPublicationDto>> response = publicationService.getPublicationsByProject(
                projectId
        );

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/publications/my
     * Lấy publications của user hiện tại
     * Role: LECTURER, STUDENT
     */
    @GetMapping("/my")
    public ResponseEntity<ResponseDto<List<ProjectPublicationDto>>> getMyPublications(
            Authentication authentication) {

        ResponseDto<List<ProjectPublicationDto>> response = publicationService.getMyPublications(
                authentication
        );

        return ResponseEntity.ok(response);
    }
}
