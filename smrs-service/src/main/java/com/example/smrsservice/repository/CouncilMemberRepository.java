package com.example.smrsservice.repository;

import com.example.smrsservice.entity.CouncilMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CouncilMemberRepository extends JpaRepository<CouncilMember, String> {


    boolean existsByCouncilIdAndLecturerId(Integer councilId, Integer lecturerId);

    void deleteByCouncilIdAndLecturerId(Integer councilId, Integer lecturerId);

    /**
     * Find council members by lecturer ID
     */
    @Query("SELECT cm FROM CouncilMember cm WHERE cm.lecturer.id = :lecturerId")
    List<CouncilMember> findByLecturerId(@Param("lecturerId") Integer lecturerId);

    /**
     * Find council members by council ID
     */
    @Query("SELECT cm FROM CouncilMember cm WHERE cm.council.id = :councilId")
    List<CouncilMember> findByCouncilId(@Param("councilId") Integer councilId);

    /**
     * Tìm member theo councilId và lecturerId
     * Dùng để xóa member khỏi council
     */
    Optional<CouncilMember> findByCouncilIdAndLecturerId(
            Integer councilId,
            Integer lecturerId
    );

    
}
