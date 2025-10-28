package com.example.smrsservice.service;
import com.example.smrsservice.dto.milestone.MilestoneCreateDto;
import com.example.smrsservice.dto.milestone.MilestoneResponseDto;
import com.example.smrsservice.dto.milestone.MilestoneUpdateDto;
import com.example.smrsservice.entity.Account;
import com.example.smrsservice.entity.Milestone;
import com.example.smrsservice.entity.Project;
import com.example.smrsservice.repository.AccountRepository;
import com.example.smrsservice.repository.MilestoneRepository;
import com.example.smrsservice.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MilestoneService {

    private final MilestoneRepository milestoneRepository;
    private final ProjectRepository projectRepository;
    private final AccountRepository accountRepository;

    public MilestoneResponseDto createMilestone(MilestoneCreateDto dto) {
        Project project = projectRepository.findById(dto.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found"));

        Account creator = accountRepository.findById(dto.getCreateById())
                .orElseThrow(() -> new RuntimeException("Account not found"));

        Milestone milestone = new Milestone();
        milestone.setDescription(dto.getDescription());
        milestone.setDueDate(dto.getDueDate());
        milestone.setProject(project);
        milestone.setCreateBy(creator);

        milestoneRepository.save(milestone);

        return mapToDto(milestone);
    }

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

    private MilestoneResponseDto mapToDto(Milestone milestone) {
        return new MilestoneResponseDto(
                milestone.getId(),
                milestone.getDescription(),
                milestone.getStatus(),
                milestone.getProgressPercent(),
                milestone.getCreateDate(),
                milestone.getDueDate(),
                milestone.getProject() != null ? milestone.getProject().getId() : null,
                milestone.getCreateBy() != null ? milestone.getCreateBy().getId() : null
        );
    }
}
