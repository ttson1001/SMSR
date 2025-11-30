package com.example.smrsservice.dto.stats.dean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimelineChartDto {
    private List<String> labels;
    private List<Long> dataset1;
    private List<Long> dataset2;
    private String dataset1Label;
    private String dataset2Label;
}
