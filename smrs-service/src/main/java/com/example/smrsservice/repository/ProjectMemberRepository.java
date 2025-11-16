package com.example.smrsservice.repository;

import com.example.smrsservice.entity.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Integer> {

    /**
     * Lấy danh sách lời mời/members của user theo status
     */
    List<ProjectMember> findByAccountIdAndStatus(Integer accountId, String status);

    /**
     * Tìm member trong project theo accountId
     */
    Optional<ProjectMember> findByProjectIdAndAccountId(Integer projectId, Integer accountId);

    /**
     * Kiểm tra user đã được mời vào project chưa
     */
    boolean existsByProjectIdAndAccountId(Integer projectId, Integer accountId);

    /**
     * Đếm số lượng members theo role và status trong project
     */
    @Query("SELECT COUNT(pm) FROM ProjectMember pm WHERE pm.project.id = :projectId " +
            "AND pm.memberRole = :role AND pm.status = :status")
    long countByProjectIdAndMemberRoleAndStatus(
            @Param("projectId") Integer projectId,
            @Param("role") String role,
            @Param("status") String status
    );

    /**
     * Lấy giảng viên mentor đã approved của project
     * ✅ Mỗi project chỉ có 1 lecturer mentor (đã approved)
     */
    @Query("SELECT pm FROM ProjectMember pm WHERE pm.project.id = :projectId " +
            "AND pm.memberRole = 'LECTURER' AND pm.status = 'Approved'")
    Optional<ProjectMember> findLecturerByProjectId(@Param("projectId") Integer projectId);

    /**
     * Lấy tất cả members của project theo status
     */
    List<ProjectMember> findByProjectIdAndStatus(Integer projectId, String status);

    /**
     * Lấy tất cả members của project (bất kể status)
     */
    List<ProjectMember> findByProjectId(Integer projectId);

    /**
     * Lấy tất cả projects mà user đang tham gia (đã approved và project đang active)
     * ✅ User có thể tham gia nhiều projects
     */
    @Query("SELECT pm FROM ProjectMember pm " +
            "WHERE pm.account.id = :accountId " +
            "AND pm.status = 'Approved' " +
            "AND pm.project.status NOT IN ('COMPLETED', 'CANCELLED')")
    List<ProjectMember> findActiveProjectsByAccountId(@Param("accountId") Integer accountId);

    /**
     * Lấy giảng viên mentor của project (bất kể status - bao gồm cả Pending)
     * Dùng để check xem đã có ai được mời làm lecturer chưa
     */
    @Query("SELECT pm FROM ProjectMember pm WHERE pm.project.id = :projectId " +
            "AND pm.memberRole = 'LECTURER'")
    Optional<ProjectMember> findLecturerByProjectIdAllStatus(@Param("projectId") Integer projectId);

    /**
     * Đếm số lượng projects mà user đang mentor (role = LECTURER, status = Approved)
     */
    @Query("SELECT COUNT(pm) FROM ProjectMember pm " +
            "WHERE pm.account.id = :accountId " +
            "AND pm.memberRole = 'LECTURER' " +
            "AND pm.status = 'Approved' " +
            "AND pm.project.status NOT IN ('COMPLETED', 'CANCELLED')")
    long countActiveMentoringProjects(@Param("accountId") Integer accountId);

    /**
     * Đếm số lượng projects mà user đang tham gia (role = STUDENT, status = Approved)
     */
    @Query("SELECT COUNT(pm) FROM ProjectMember pm " +
            "WHERE pm.account.id = :accountId " +
            "AND pm.memberRole = 'STUDENT' " +
            "AND pm.status = 'Approved' " +
            "AND pm.project.status NOT IN ('COMPLETED', 'CANCELLED')")
    long countActiveStudentProjects(@Param("accountId") Integer accountId);

    /**
     * ✅ FIXED: Chỉ return ProjectMember, không phải Project hay Major
     * Lấy tất cả project members theo accountId
     */
    List<ProjectMember> findByAccountId(Integer accountId);
}