package com.site.elohim.repository;

import com.site.elohim.model.Attendances;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendancesRepository extends JpaRepository<Attendances, Long> {

    // memberId + attendanceDate 로 한 건 찾기
    Optional<Attendances> findByMemberIdAndAttendanceDate(Long memberId, LocalDate attendanceDate);

    List<Attendances> findByAttendanceDateAndMemberIdIn(LocalDate attendanceDate, List<Long> memberIdList);

    /**
     * 기간 + 여러 memberId에 대한 출석 데이터 조회 (통계용)
     */
    List<Attendances> findByAttendanceDateBetweenAndMemberIdIn(
            LocalDate startDate,
            LocalDate endDate,
            List<Long> memberIdList
    );


    /**
     * 예배, 셀모임 횟수 가져오는 쿼리 문
     */
    interface AttendanceAgg {
        Long getMemberId();
        Long getAttendCount(); // worship=true OR cell=true
        Long getCellCount();   // cell=true
    }

    @Query("""
        select
            a.memberId as memberId,
            sum(case when (a.worshipStatus = true or a.cellStatus = true) then 1 else 0 end) as attendCount,
            sum(case when (a.cellStatus = true) then 1 else 0 end) as cellCount
        from Attendances a
        where a.attendanceDate between :startDate and :endDate
          and a.memberId in :memberIdList
        group by a.memberId
        """)
    List<AttendanceAgg> aggregateAttendance(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("memberIdList") List<Long> memberIdList
    );
}
