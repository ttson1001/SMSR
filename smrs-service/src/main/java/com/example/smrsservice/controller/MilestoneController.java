package com.example.smrsservice.controller;

import com.example.smrsservice.dto.milestone.MilestoneCreateDto;
import com.example.smrsservice.dto.milestone.MilestoneResponseDto;
import com.example.smrsservice.dto.milestone.MilestoneSubmitReportDto;
import com.example.smrsservice.dto.milestone.MilestoneUpdateDto;
import com.example.smrsservice.service.MilestoneService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/milestones")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class MilestoneController {

    private final MilestoneService milestoneService;

    @PostMapping
    public ResponseEntity<MilestoneResponseDto> createMilestone(@RequestBody MilestoneCreateDto dto) {
        return ResponseEntity.ok(milestoneService.createMilestone(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MilestoneResponseDto> updateMilestone(
            @PathVariable Integer id,
            @RequestBody MilestoneUpdateDto dto) {
        return ResponseEntity.ok(milestoneService.updateMilestone(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMilestone(@PathVariable Integer id) {
        milestoneService.deleteMilestone(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<MilestoneResponseDto>> getMilestonesByProject(@PathVariable Integer projectId) {
        return ResponseEntity.ok(milestoneService.getMilestonesByProject(projectId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MilestoneResponseDto> getMilestoneById(@PathVariable Integer id) {
        return ResponseEntity.ok(milestoneService.getMilestoneById(id));
    }

    @PostMapping("/{id}/submit-report")
    public ResponseEntity<MilestoneResponseDto> submitReport(
            @PathVariable Integer id,
            @RequestBody MilestoneSubmitReportDto dto,
            Authentication authentication) {
        return ResponseEntity.ok(milestoneService.submitReport(id, dto, authentication));
    }

    @GetMapping("/project/{projectId}/final")
    public ResponseEntity<MilestoneResponseDto> getFinalMilestone(@PathVariable Integer projectId) {
        return ResponseEntity.ok(milestoneService.getFinalMilestone(projectId));
    }
}
