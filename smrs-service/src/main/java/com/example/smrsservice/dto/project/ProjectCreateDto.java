package com.example.smrsservice.dto.project;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectCreateDto {
    private String name;
    private String description;
    private String type;
    private Date dueDate;
    private Integer ownerId;
    private List<ProjectFileDto> files;
    private List<ProjectImageDto> images;
}

