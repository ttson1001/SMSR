package com.example.smrsservice.dto.stats.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityDto {
    private String type;
    private String description;
    private Integer userId;
    private String userName;
    private Integer projectId;
    private String projectName;
    private Instant timestamp;
    private String icon;
}
