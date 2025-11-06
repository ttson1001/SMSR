package com.example.smrsservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "council")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Council {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "council_code", length = 50, unique = true, nullable = false)
    private String councilCode;

    @Column(name = "council_name", length = 200, nullable = false)
    private String councilName;

    @Column(length = 200)
    private String department;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dean_id", nullable = false)
    private CouncilManagerProfile dean;

    @Column(length = 20)
    private String status = "ACTIVE";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "council", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CouncilMember> members = new ArrayList<>();

    @OneToMany(mappedBy = "council", cascade = CascadeType.ALL)
    private List<ProjectCouncil> projectCouncils = new ArrayList<>();
}
