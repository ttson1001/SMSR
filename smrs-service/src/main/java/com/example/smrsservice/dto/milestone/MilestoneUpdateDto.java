package com.example.smrsservice.dto.milestone;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class MilestoneUpdateDto {
    private String description;
    private String status;
    private Double progressPercent;
    private Date dueDate;
}
