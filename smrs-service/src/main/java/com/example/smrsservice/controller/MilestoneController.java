package com.example.smrsservice.controller;

import com.example.smrsservice.dto.milestone.MilestoneCreateDto;
import com.example.smrsservice.dto.milestone.MilestoneResponseDto;
import com.example.smrsservice.dto.milestone.MilestoneSubmitReportDto;
import com.example.smrsservice.dto.milestone.MilestoneUpdateDto;
import com.example.smrsservice.service.MilestoneService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/milestones")
@RequiredArgsConstructor
public class MilestoneController {

    private final MilestoneService milestoneService;

    @PostMapping
    public MilestoneResponseDto create(@RequestBody MilestoneCreateDto dto) {
        return milestoneService.createMilestone(dto);
    }

    @PutMapping("/{id}")
    public MilestoneResponseDto update(@PathVariable Integer id, @RequestBody MilestoneUpdateDto dto) {
        return milestoneService.updateMilestone(id, dto);
    }

    @GetMapping("/project/{projectId}")
    public List<MilestoneResponseDto> getByProject(@PathVariable Integer projectId) {
        return milestoneService.getMilestonesByProject(projectId);
    }

    @GetMapping("/{id}")
    public MilestoneResponseDto getById(@PathVariable Integer id) {
        return milestoneService.getMilestoneById(id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        milestoneService.deleteMilestone(id);
    }

    // ✅ ENDPOINT MỚI: Leader nộp report
    @PostMapping("/{id}/submit-report")
    public ResponseEntity<MilestoneResponseDto> submitReport(
            @PathVariable Integer id,
            @RequestBody MilestoneSubmitReportDto dto,
            Authentication authentication) {
        return ResponseEntity.ok(milestoneService.submitReport(id, dto, authentication));
    }

    // ✅ ENDPOINT MỚI: Lấy final milestone của project
    @GetMapping("/project/{projectId}/final")
    public ResponseEntity<MilestoneResponseDto> getFinalMilestone(@PathVariable Integer projectId) {
        return ResponseEntity.ok(milestoneService.getFinalMilestone(projectId));
    }
}
