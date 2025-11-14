package com.example.smrsservice.dto.major;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MajorResponse {
    private Integer id;
    private String name;
    private String code;
    private String description;
    private Boolean isActive;
}
