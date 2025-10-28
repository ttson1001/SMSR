package com.example.smrsservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "task")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private String description;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private Account createdBy;

    @ManyToOne
    @JoinColumn(name = "assigned_to")
    private Account assignedTo;

    @Column(name = "start_date")
    private Date startDate = new Date();

    private Date deadline;

    @Column(name = "progress_percent")
    private Double progressPercent = 0.0;

    private String status = "Pending";

    @ManyToOne
    @JoinColumn(name = "milestone_id")
    private Milestone milestone;
}
