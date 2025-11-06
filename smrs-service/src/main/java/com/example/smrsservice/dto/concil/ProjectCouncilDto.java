package com.example.smrsservice.dto.concil;

import com.example.smrsservice.common.ProjectStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectCouncilDto {
    private Integer id;

    private Integer projectId;
    private String projectName;
    private String projectDescription;
    private ProjectStatus projectStatus;

    private String councilCode;
    private String councilName;

    private String decision;
    private String comment;
    private Instant decisionDate;
}
