package com.site.elohim.repository;

import com.site.elohim.model.Members;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MembersRepository extends JpaRepository<Members, Long> {

    Optional<Members> findById(Long id);
    boolean existsByCellName(String newName);
    boolean existsMembersByMemberNameAndMemberBirth(String memberName, LocalDate memberBirth);
    List<Members> findByCellLeaderStatusTrueAndCellName(String cellName);
    List<Members> findByCellLeaderStatusOrderByMemberNameAsc(boolean cellLeaderStatus);
    List<Members> findByMemberNameContainingOrderByMemberNameAsc(String memberName);
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


    @Query("""
        select m from Members m
        where year(m.memberBirth) = :year
        order by m.memberName asc
        """)
    List<Members> findByBirthYear(@Param("year") int year);


    // ② 특정 '월' 검색 (연도 상관없이)
    @Query("""
        select m from Members m
        where month(m.memberBirth) = :month
        order by m.memberName asc
        """)
    List<Members> findByBirthMonth(@Param("month") int month);

    // ③ 특정 '년도 + 월' 검색
    @Query("""
        select m from Members m
        where year(m.memberBirth) = :year
          and month(m.memberBirth) = :month
        order by m.memberName asc
        """)
    List<Members> findByBirthYearAndMonth(@Param("year") int year, @Param("month") int month);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
    update Members m set
        m.memberName        = :memberName,
        m.memberBirth       = :memberBirth,
        m.memberPhoneNumber = :memberPhoneNumber,
        m.memberAddress     = :memberAddress,
        m.baptismStatus     = :baptismStatus,
        m.nurtureYear       = :nurtureYear,
        m.nurtureSemester   = :nurtureSemester,
        m.growthYear        = :growthYear,
        m.growthSemester    = :growthSemester,
        m.memberMemo        = :memberMemo,
        m.cellLeaderStatus  = :cellLeaderStatus,
        m.cellKey           = :cellKey,
        m.cellName          = :cellName
    where m.id = :id
    """)
    int updateMemberById(
            @Param("id") Long id,
            @Param("memberName") String memberName,
            @Param("memberBirth") LocalDate memberBirth,
            @Param("memberPhoneNumber") String memberPhoneNumber,
            @Param("memberAddress") String memberAddress,
            @Param("baptismStatus") String baptismStatus,
            @Param("nurtureYear") String nurtureYear,
            @Param("nurtureSemester") String nurtureSemester,
            @Param("growthYear") String growthYear,
            @Param("growthSemester") String growthSemester,
            @Param("memberMemo") String memberMemo,
            @Param("cellLeaderStatus") boolean cellLeaderStatus,
            @Param("cellKey") Long cellKey,
            @Param("cellName") String cellName
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
    update Members m
       set m.cellName = :cellName
     where m.cellKey = :leaderId
       and m.id <> :leaderId
    """)
    void updateCellNameByCellKeyExceptSelf(
            @Param("leaderId") Long leaderId,
            @Param("cellName") String cellName
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
    update Members m
        set m.cellKey = NULL, m.cellName = NULL
    where m.cellKey = :leaderId
    """)
    void resetCellNameByCellKey(@Param("leaderId") Long leaderId);

    @Query("""
        select m
        from Members m
        join Users u
          on m.cellKey = u.memberId
        where u.id = :loginuser_PkId
        order by m.memberName asc
        """)
    List<Members> findMembersByLeaderUserId(@Param("loginuser_PkId") Long loginuser_PkId);
}