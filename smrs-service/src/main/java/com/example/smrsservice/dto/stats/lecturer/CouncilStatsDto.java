package com.example.smrsservice.dto.stats.lecturer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouncilStatsDto {
    private Integer councilId;
    private String councilName;
    private String councilCode;
    private String myRole;
    private Integer totalProjects;
    private Integer projectsScored;
    private Integer projectsToScore;
}