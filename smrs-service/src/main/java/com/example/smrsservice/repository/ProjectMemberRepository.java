package com.example.smrsservice.repository;

import com.example.smrsservice.entity.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Integer> {
    List<ProjectMember> findByAccountIdAndStatus(Integer accountId, String status);

    Optional<ProjectMember> findByProjectIdAndAccountId(Integer projectId, Integer accountId);

    boolean existsByProjectIdAndAccountId(Integer projectId, Integer accountId);

    @Query("SELECT COUNT(pm) FROM ProjectMember pm WHERE pm.project.id = :projectId " +
            "AND pm.memberRole = :role AND pm.status = :status")
    long countByProjectIdAndMemberRoleAndStatus(
            @Param("projectId") Integer projectId,
            @Param("role") String role,
            @Param("status") String status
    );

    // Lấy giảng viên hướng dẫn của project
    @Query("SELECT pm FROM ProjectMember pm WHERE pm.project.id = :projectId " +
            "AND pm.memberRole = 'LECTURER' AND pm.status = 'Approved'")
    Optional<ProjectMember> findLecturerByProjectId(@Param("projectId") Integer projectId);

    List<ProjectMember> findByProjectIdAndStatus(Integer projectId, String status);

    /**
     * Kiểm tra user có đang tham gia project nào đang active không
     */
    @Query("SELECT pm FROM ProjectMember pm " +
            "WHERE pm.account.id = :accountId " +
            "AND pm.status = 'Approved' " +
            "AND pm.project.status NOT IN ('COMPLETED', 'CANCELLED')")
    List<ProjectMember> findActiveProjectsByAccountId(@Param("accountId") Integer accountId);

    /**
     * Kiểm tra user có project active không
     */
    @Query("SELECT CASE WHEN COUNT(pm) > 0 THEN true ELSE false END " +
            "FROM ProjectMember pm " +
            "WHERE pm.account.id = :accountId " +
            "AND pm.status = 'Approved' " +
            "AND pm.project.status NOT IN ('COMPLETED', 'CANCELLED')")
    boolean hasActiveProject(@Param("accountId") Integer accountId);

    List<ProjectMember> findByProjectId(Integer projectId);

    /**
     * Lấy giảng viên của project (bất kể status)
     */
    @Query("SELECT pm FROM ProjectMember pm WHERE pm.project.id = :projectId " +
            "AND pm.memberRole = 'LECTURER'")
    Optional<ProjectMember> findLecturerByProjectIdAllStatus(@Param("projectId") Integer projectId);
}

