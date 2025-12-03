package com.example.smrsservice.repository;

import com.example.smrsservice.common.PublicationStatus;
import com.example.smrsservice.common.PublicationType;
import com.example.smrsservice.entity.ProjectPublication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProjectPublicationRepository extends JpaRepository<ProjectPublication, Integer> {

    /**
     * Lấy tất cả publications của 1 project
     */
    List<ProjectPublication> findByProjectId(Integer projectId);

    /**
     * Lấy tất cả publications của 1 author
     */
    @Query("SELECT pp FROM ProjectPublication pp WHERE pp.author.id = :authorId")
    List<ProjectPublication> findByAuthorId(@Param("authorId") Integer authorId);

    /**
     * Lấy publications theo status
     */
    List<ProjectPublication> findByStatus(PublicationStatus status);

    /**
     * Lấy publications theo type
     */
    List<ProjectPublication> findByPublicationType(PublicationType type);

    /**
     * Lấy publications của project theo status
     */
    @Query("SELECT pp FROM ProjectPublication pp WHERE pp.project.id = :projectId AND pp.status = :status")
    List<ProjectPublication> findByProjectIdAndStatus(
            @Param("projectId") Integer projectId,
            @Param("status") PublicationStatus status
    );

    /**
     * Lấy publications của author theo status
     */
    @Query("SELECT pp FROM ProjectPublication pp WHERE pp.author.id = :authorId AND pp.status = :status")
    List<ProjectPublication> findByAuthorIdAndStatus(
            @Param("authorId") Integer authorId,
            @Param("status") PublicationStatus status
    );

    /**
     * Đếm số publications của 1 project
     */
    @Query("SELECT COUNT(pp) FROM ProjectPublication pp WHERE pp.project.id = :projectId")
    Long countByProjectId(@Param("projectId") Integer projectId);

    /**
     * Đếm số publications của 1 author
     */
    @Query("SELECT COUNT(pp) FROM ProjectPublication pp WHERE pp.author.id = :authorId")
    Long countByAuthorId(@Param("authorId") Integer authorId);

    /**
     * Đếm số publications đã xuất bản của 1 project
     */
    @Query("SELECT COUNT(pp) FROM ProjectPublication pp WHERE pp.project.id = :projectId AND pp.status = 'PUBLISHED'")
    Long countPublishedByProjectId(@Param("projectId") Integer projectId);

    /**
     * Đếm số publications đã xuất bản của 1 author
     */
    @Query("SELECT COUNT(pp) FROM ProjectPublication pp WHERE pp.author.id = :authorId AND pp.status = 'PUBLISHED'")
    Long countPublishedByAuthorId(@Param("authorId") Integer authorId);
}
