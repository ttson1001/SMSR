package com.example.smrsservice.dto.project;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectImportDto {

    private Integer id;
    private String name;
    private String description;
    private String type;
    private String status;
    private Date dueDate;
    private String majorName;
}
