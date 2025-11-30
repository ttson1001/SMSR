package com.example.smrsservice.repository;

import com.example.smrsservice.common.ProjectStatus;
import com.example.smrsservice.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Integer>, JpaSpecificationExecutor<Project> {

    Page<Project> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Project> findByDescriptionContainingIgnoreCase(String description, Pageable pageable);

    Page<Project> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String name, String description, Pageable pageable
    );

    /**
     * Lấy projects theo status (pagination)
     */
    Page<Project> findByStatus(ProjectStatus status, Pageable pageable);

    /**
     * Tìm kiếm projects với filter status
     */
    @Query("SELECT p FROM Project p " +
            "WHERE (:status IS NULL OR p.status = :status) " +
            "AND (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND (:description IS NULL OR LOWER(p.description) LIKE LOWER(CONCAT('%', :description, '%')))")
    Page<Project> searchProjectsWithFilters(
            @Param("status") ProjectStatus status,
            @Param("name") String name,
            @Param("description") String description,
            Pageable pageable);

    /**
     * Lấy projects đang cần review (cho council)
     */
    @Query("SELECT p FROM Project p WHERE p.status = 'IN_REVIEW'")
    Page<Project> findProjectsForReview(Pageable pageable);

    /**
     * Tìm kiếm projects đang IN_REVIEW với filter
     */
    @Query("SELECT p FROM Project p " +
            "WHERE p.status = 'IN_REVIEW' " +
            "AND (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND (:type IS NULL OR LOWER(p.type) LIKE LOWER(CONCAT('%', :type, '%')))")
    Page<Project> searchProjectsForReview(
            @Param("name") String name,
            @Param("type") String type,
            Pageable pageable);

    @Query("SELECT DISTINCT p FROM Project p " +
            "LEFT JOIN FETCH p.files " +
            "LEFT JOIN FETCH p.images " +
            "WHERE p.id = :id")
    Optional<Project> findByIdWithFilesAndImages(@Param("id") Integer id);

    /**
     * Lấy danh sách projects theo ownerId
     */
    List<Project> findByOwnerId(Integer ownerId);

    // ==================== STATS METHODS ====================

    /**
     * Count projects by status
     */
    long countByStatus(ProjectStatus status);

    /**
     * Count projects by owner
     */
    @Query("SELECT COUNT(p) FROM Project p WHERE p.owner.id = :ownerId")
    long countByOwnerId(@Param("ownerId") Integer ownerId);

    /**
     * Count projects created between dates
     */
    @Query("SELECT COUNT(p) FROM Project p WHERE p.createDate BETWEEN :startDate AND :endDate")
    long countCreatedBetween(
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate);

    /**
     * Count projects by status and created between dates
     */
    @Query("SELECT COUNT(p) FROM Project p WHERE p.status = :status AND p.createDate BETWEEN :startDate AND :endDate")
    long countByStatusAndCreateDateBetween(
            @Param("status") ProjectStatus status,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate);

    /**
     * Find ALL projects by status (no pagination - for stats)
     */
    List<Project> findAllByStatus(ProjectStatus status);

    /**
     * Find projects by multiple statuses
     */
    List<Project> findByStatusIn(List<ProjectStatus> statuses);

    /**
     * Find projects created between dates
     */
    @Query("SELECT p FROM Project p WHERE p.createDate BETWEEN :startDate AND :endDate")
    List<Project> findByCreateDateBetween(
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate);
}