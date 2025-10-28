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

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id")
    private Account account;

    @Column(name = "employee_code", length = 32, nullable = false)
    private String employeeCode;         // Mã cán bộ (duy nhất)

    @Column(name = "department", length = 100)
    private String department;           // Khoa/Phòng/Ban

    @Column(name = "position_title", length = 100)
    private String positionTitle;        // Chức danh (VD: Trưởng ban, Điều phối…)

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private CouncilManagerStatus status = CouncilManagerStatus.ACTIVE;

    @Column(name = "start_date")
    private LocalDate startDate;         // Ngày bắt đầu bổ nhiệm

    @Column(name = "end_date")
    private LocalDate endDate;           // Ngày kết thúc (nếu có)

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;                 // Ghi chú

    // Audit
    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
