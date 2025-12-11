package com.example.smrsservice.dto.major;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MajorWithStatsDto {
    private Integer id;
    private String name;
    private String code;
    private String description;
    private Boolean isActive;

    private Integer lecturerCount;
}
