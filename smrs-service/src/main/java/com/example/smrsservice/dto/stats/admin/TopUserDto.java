package com.example.smrsservice.dto.stats.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopUserDto {
    private Integer userId;
    private String userName;
    private String role;
    private String userEmail;
    private Integer projectsCount;
    private Double averageScore;
    private String activityLevel;
}
