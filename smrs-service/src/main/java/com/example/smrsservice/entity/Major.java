package com.example.smrsservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "majors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Major {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 50)
    private String code;

    @Column(length = 500)
    private String description;

    @Column(name = "is_active")
    private Boolean isActive = true;
}
