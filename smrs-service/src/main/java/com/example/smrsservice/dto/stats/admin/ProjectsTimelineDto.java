package com.example.smrsservice.dto.stats.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectsTimelineDto {
    private List<String> labels;
    private List<Long> created;
    private List<Long> completed;
}
