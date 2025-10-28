    package com.example.smrsservice.entity;

    import jakarta.persistence.*;
    import lombok.AllArgsConstructor;
    import lombok.Getter;
    import lombok.NoArgsConstructor;
    import lombok.Setter;

    @Entity
    @Table(name = "project_file")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public class ProjectFile {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Integer id;

        @Column(name = "file_path")
        private String filePath;

        private String type;

        @ManyToOne
        @JoinColumn(name = "project_id")
        private Project project;
    }
