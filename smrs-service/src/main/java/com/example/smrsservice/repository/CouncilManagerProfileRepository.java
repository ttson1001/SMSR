//package com.example.smrsservice.repository;
//
//import com.example.smrsservice.common.CouncilManagerStatus;
//import com.example.smrsservice.entity.CouncilManagerProfile;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//import java.util.Optional;
//@Repository
//public interface CouncilManagerProfileRepository extends JpaRepository<CouncilManagerProfile, Integer> {
//    // Tìm tất cả profiles của một account
//    List<CouncilManagerProfile> findByAccountId(Integer accountId);
//
//    // Tìm theo employee code
//    Optional<CouncilManagerProfile> findByEmployeeCode(String employeeCode);
//
//    // Kiểm tra tồn tại employee code
//    boolean existsByEmployeeCode(String employeeCode);
//
//    // Tìm theo council code
//    List<CouncilManagerProfile> findByCouncilCode(String councilCode);
//
//
//
//}
