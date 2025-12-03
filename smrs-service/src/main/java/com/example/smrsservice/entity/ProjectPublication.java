package com.example.smrsservice.entity;

import com.example.smrsservice.common.PublicationStatus;
import com.example.smrsservice.common.PublicationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "project_publication")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectPublication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private Account author;

    @Enumerated(EnumType.STRING)
    private PublicationStatus status;

    private String publicationName;

    @Enumerated(EnumType.STRING)
    private PublicationType publicationType;

    private String publicationLink;
    private Date registeredDate;
    private Date publishedDate;
    private String notes;
    private String doi;
    private String isbnIssn;
    private Date createdAt;
    private Date updatedAt;
}
