package com.example.smrsservice.dto.task;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskResponse {
    private Integer id;
    private String name;
    private String description;

    private AccountSummary createdBy;
    private AccountSummary assignedTo;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private Date startDate;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private Date deadline;

    private Double progressPercent;
    private String status;

    private MilestoneSummary milestone;
    private ProjectSummary project; // lấy từ milestone.getProject()

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AccountSummary {
        private Integer id;
        private String name;
        private String email;
        private String avatar;
        private String phone;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MilestoneSummary {
        private Integer id;
        private String description;
        private String status;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ProjectSummary {
        private Integer id;
        private String name;
    }
}
