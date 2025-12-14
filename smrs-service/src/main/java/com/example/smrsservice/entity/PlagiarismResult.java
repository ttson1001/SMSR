package com.example.smrsservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "plagiarism_result")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlagiarismResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String scanId;

    private String status;

    @Column(columnDefinition = "json")
    private String payload;

    private Instant receivedAt = Instant.now();

}
