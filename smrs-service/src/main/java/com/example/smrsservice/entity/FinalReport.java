package com.example.smrsservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;

@Entity
@Table(name = "final_report")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinalReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne
    @JoinColumn(name = "submitted_by", nullable = false)
    private Account submittedBy;

    @Column(name = "report_title")
    private String reportTitle;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_type")
    private String fileType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "submission_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date submissionDate;

    @Column(name = "status")
    private String status;

    @Column(name = "version")
    private Integer version;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "create_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createDate;

    @Column(name = "update_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateDate;

    @PrePersist
    protected void onCreate() {
        createDate = new Date();
        submissionDate = new Date();
        if (status == null) status = "PENDING";
        if (version == null) version = 1;
    }

    @PreUpdate
    protected void onUpdate() {
        updateDate = new Date();
    }
}