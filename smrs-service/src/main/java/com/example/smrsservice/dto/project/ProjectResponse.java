package com.example.smrsservice.dto.project;

import com.example.smrsservice.common.ProjectStatus;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class ProjectResponse {
    private Integer id;
    private String name;
    private String description;
    private String type;
    private Date dueDate;
    private Integer ownerId;
    private String ownerName;
    private ProjectStatus status;
}
