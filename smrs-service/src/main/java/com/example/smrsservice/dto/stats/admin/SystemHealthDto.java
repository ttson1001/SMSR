package com.example.smrsservice.dto.stats.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemHealthDto {
    private String systemStatus;
    private Integer activeUsers;
    private String responseTime;
    private String uptime;
    private String storageUsed;
}
