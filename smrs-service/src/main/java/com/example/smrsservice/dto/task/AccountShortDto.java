package com.example.smrsservice.dto.task;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccountShortDto {
    private Integer id;
    private String name;
    private String email;
}
