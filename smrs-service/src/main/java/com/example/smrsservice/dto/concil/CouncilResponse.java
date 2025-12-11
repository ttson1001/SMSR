package com.example.smrsservice.dto.concil;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouncilResponse {
    private Integer id;
    private String councilCode;
    private String councilName;
    private String department;
    private String description;
    private String status;
    private Instant createdAt;

    private Integer deanId;
    private String deanName;
    private String deanEmail;

    private List<MemberInfo> members;
    private Integer totalProjects;




    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberInfo {
        private Integer id;
        private Integer lecturerId;
        private String lecturerName;
        private String lecturerEmail;
        private String role;
        private String status;
    }
}
