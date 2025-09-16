package com.site.elohim.repository;

import com.site.elohim.model.Members;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MembersRepository extends JpaRepository<Members, Long> {

    Optional<Members> deleteMembersById(Long id);
    Optional<Members> findById(Long id);
    boolean existsById(Long id);
    boolean existsMembersByMemberNameAndMemberBirth(String memberName, String memberBirth);
    List<Members> findByCellLeaderStatusOrderByMemberNameAsc(boolean cellLeaderStatus);
    List<Members> findByMemberNameContainingOrderByMemberNameAsc(String memberName);
    List<Members> findByMemberBirthContainingOrderByMemberNameAsc(String memberBirth);
    List<Members> findByCellKeyOrderByMemberNameAsc(Long cellKey);
    List<Members> findByBaptismStatusOrderByMemberNameAsc(String baptismStatus);
    List<Members> findByNurtureYearContainingOrderByMemberNameAsc(String nurtureYear);
    List<Members> findByNurtureSemesterOrderByMemberNameAsc(String nurtureSemester);
    List<Members> findByNurtureYearContainingAndNurtureSemesterOrderByMemberNameAsc(String nurtureYear, String nurtureSemester);
    List<Members> findByGrowthYearContainingOrderByMemberNameAsc(String growthYear);
    List<Members> findByGrowthSemesterOrderByMemberNameAsc(String growthSemester);
    List<Members> findByGrowthYearContainingAndGrowthSemesterOrderByMemberNameAsc(String growthYear, String growthSemester);
    List<Members> findAllByOrderByMemberNameAsc();
    List<Members> findAllByOrderByMemberNameDesc();
    List<Members> findAllByOrderByMemberBirthAsc();
    List<Members> findAllByOrderByMemberBirthDesc();
}