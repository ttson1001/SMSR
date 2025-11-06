package com.example.smrsservice.entity;

import com.example.smrsservice.common.DecisionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "project_council")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectCouncil {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "council_id", nullable = false)
    private Council council;

    @Enumerated(EnumType.STRING)  // ✅ Dùng enum
    @Column(length = 20)
    private DecisionStatus decision = DecisionStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "decision_date")
    private Instant decisionDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "decided_by")
    private CouncilManagerProfile decidedBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

}
