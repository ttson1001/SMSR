package com.example.smrsservice.repository;

import com.example.smrsservice.entity.CouncilMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CouncilMemberRepository extends JpaRepository<CouncilMember, String> {
    List<CouncilMember> findByCouncilId(Integer councilId);

    List<CouncilMember> findByLecturerId(Integer lecturerId);

    boolean existsByCouncilIdAndLecturerId(Integer councilId, Integer lecturerId);

    void deleteByCouncilIdAndLecturerId(Integer councilId, Integer lecturerId);
}
