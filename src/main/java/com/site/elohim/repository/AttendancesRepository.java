package com.site.elohim.repository;

import com.site.elohim.model.Attendances;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendancesRepository extends JpaRepository<Attendances, Long> {

    // memberId + attendanceDate 로 한 건 찾기
    Optional<Attendances> findByMemberIdAndAttendanceDate(Long memberId, LocalDate attendanceDate);

    List<Attendances> findByAttendanceDateAndMemberIdIn(LocalDate attendanceDate, List<Long> memberIdList);
}
