package com.example.smrsservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "project_score")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private Double score;
    private String comment;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;
}
