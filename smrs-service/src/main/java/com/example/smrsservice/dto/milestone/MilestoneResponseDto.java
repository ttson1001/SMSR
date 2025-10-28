package com.example.smrsservice.dto.milestone;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
public class MilestoneResponseDto {
    private Integer id;
    private String description;
    private String status;
    private Double progressPercent;
    private Date createDate;
    private Date dueDate;
    private Integer projectId;
    private Integer createById;
}
