package com.example.smrsservice.dto.project;

import com.example.smrsservice.common.ProjectStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectForCouncilDto {
        private Integer id;
        private String name;
        private String description;
        private String type;
        private ProjectStatus status;
        private Date createDate;
        private Date dueDate;

        // Owner info
        private Integer ownerId;
        private String ownerName;
        private String ownerEmail;

        // Council info (nếu đã được gán hội đồng)
        private String councilCode;
        private String councilName;
        private Integer totalCouncilMembers;
        private Integer evaluatedMembers;
        private Double averageScore;

        // Statistics
        private boolean hasCouncil;  // Đã được gán hội đồng chưa
        private boolean isEvaluated; // Đã được chấm điểm chưa
}
