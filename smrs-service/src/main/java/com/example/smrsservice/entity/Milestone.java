package com.example.smrsservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "milestone")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Milestone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String description;
    private String status = "InProgress";

    @Column(name = "progress_percent")
    private Double progressPercent = 0.0;

    @ManyToOne
    @JoinColumn(name = "create_by")
    private Account createBy;

    @Column(name = "create_date")
    private Date createDate = new Date();

    @Column(name = "due_date")
    private Date dueDate;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(name = "is_final")
    private Boolean isFinal = false;  // Đánh dấu milestone cuối cùng

    @Column(name = "report_url", columnDefinition = "TEXT")
    private String reportUrl;  // URL file report

    @Column(name = "report_submitted_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date reportSubmittedAt;  // Thời gian nộp report

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_submitted_by")
    private Account reportSubmittedBy;  // Người nộp report (leader)

    @Column(name = "report_comment", columnDefinition = "TEXT")
    private String reportComment;  // Ghi chú khi nộp report
}
