package com.example.smrsservice.controller;

import com.example.smrsservice.dto.milestone.MilestoneCreateDto;
import com.example.smrsservice.dto.milestone.MilestoneResponseDto;
import com.example.smrsservice.dto.milestone.MilestoneUpdateDto;
import com.example.smrsservice.service.MilestoneService;
import lombok.RequiredArgsConstructor;
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
}
