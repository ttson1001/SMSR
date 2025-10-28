package com.example.smrsservice.entity;

import com.example.smrsservice.common.ProjectStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "project")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private String description;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ProjectStatus status = ProjectStatus.PENDING;
    private String type;

    @Column(name = "create_date")
    private Date createDate = new Date();

    @Column(name = "due_date")
    private Date dueDate;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private Account owner;

    @OneToMany(mappedBy = "project")
    private List<ProjectFile> files = new ArrayList<>();

    @OneToMany(mappedBy = "project")
    private List<ProjectImage> images = new ArrayList<>();
}
