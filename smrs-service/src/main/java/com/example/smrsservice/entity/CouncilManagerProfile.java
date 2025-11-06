package com.example.smrsservice.entity;

import com.example.smrsservice.common.CouncilManagerStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "council_manager_profile")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CouncilManagerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "account_id", nullable = false, unique = true)
    private Account account;

    @Column(name = "employee_code", length = 50, unique = true)
    private String employeeCode;

    @Column(length = 200)
    private String department;

    @Column(name = "position_title", length = 100)
    private String positionTitle;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private CouncilManagerStatus status = CouncilManagerStatus.ACTIVE;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    // ✅ THÊM CÁC TIMESTAMP
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @OneToMany(mappedBy = "dean", cascade = CascadeType.ALL)
    private List<Council> councils = new ArrayList<>();

    @OneToMany(mappedBy = "decidedBy")
    private List<ProjectCouncil> decisions = new ArrayList<>();
}
