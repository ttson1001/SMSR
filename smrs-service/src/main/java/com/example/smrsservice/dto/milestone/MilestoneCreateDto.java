package com.example.smrsservice.dto.milestone;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class MilestoneCreateDto {
    private String description;
    private Date dueDate;
    private Integer projectId;
    private Integer createById;
}
