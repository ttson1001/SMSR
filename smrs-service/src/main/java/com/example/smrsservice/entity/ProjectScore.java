package com.example.smrsservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;

@Entity
@Table(name = "project_score")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne
    @JoinColumn(name = "final_milestone_id")
    private Milestone finalMilestone;

    @ManyToOne
    @JoinColumn(name = "lecturer_id", nullable = false)
    private Account lecturer;

    @Column(name = "criteria_1_score")
    private Double criteria1Score;

    @Column(name = "criteria_2_score")
    private Double criteria2Score;

    @Column(name = "criteria_3_score")
    private Double criteria3Score;

    @Column(name = "criteria_4_score")
    private Double criteria4Score;

    @Column(name = "criteria_5_score")
    private Double criteria5Score;

    @Column(name = "criteria_6_score")
    private Double criteria6Score;

    @Column(name = "bonus_score_1")
    private Double bonusScore1;

    @Column(name = "bonus_score_2")
    private Double bonusScore2;

    @Column(name = "total_score")
    private Double totalScore;

    @Column(name = "final_score")
    private Double finalScore;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "score_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date scoreDate;

    @Column(name = "create_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createDate;

    @Column(name = "update_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateDate;

    @PrePersist
    protected void onCreate() {
        createDate = new Date();
        scoreDate = new Date();
        calculateTotalScore();
    }

    @PreUpdate
    protected void onUpdate() {
        updateDate = new Date();
        calculateTotalScore();
    }

    private void calculateTotalScore() {
        totalScore = 0.0;
        if (criteria1Score != null) totalScore += criteria1Score;
        if (criteria2Score != null) totalScore += criteria2Score;
        if (criteria3Score != null) totalScore += criteria3Score;
        if (criteria4Score != null) totalScore += criteria4Score;
        if (criteria5Score != null) totalScore += criteria5Score;
        if (criteria6Score != null) totalScore += criteria6Score;

        finalScore = totalScore;
        if (bonusScore1 != null) finalScore += bonusScore1;
        if (bonusScore2 != null) finalScore += bonusScore2;

        if (finalScore > 100) finalScore = 100.0;
    }
}