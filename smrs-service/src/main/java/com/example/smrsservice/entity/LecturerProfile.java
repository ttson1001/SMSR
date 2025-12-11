package com.example.smrsservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "lecturer_profile")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LecturerProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    @ManyToOne
    @JoinColumn(name = "major_id")
    private Major major;

    @Column(name = "teaching_major")
    private String teachingMajor;

    @Column(name = "years_experience")
    private Integer yearsExperience;

    private String degree;



}
