package com.site.elohim.service;

import com.site.elohim.dto.AttendanceItemDto;
import com.site.elohim.model.Attendances;
import com.site.elohim.model.Members;
import com.site.elohim.model.Users;
import com.site.elohim.repository.AttendancesRepository;
import com.site.elohim.repository.MembersRepository;
import com.site.elohim.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceService {

    private final MembersRepository membersRepository;
    private final UsersRepository usersRepository;
    private final AttendancesRepository attendancesRepository;

    /**
     * 로그인한 셀 리더의 셀원 목록 가져오기
     */
    public List<Members> getMyCellMembers(String loginUserId) {

        Users loginUser = usersRepository.findByUserId(loginUserId)
                .orElseThrow(() -> new IllegalStateException("로그인 정보를 찾을 수 없습니다."));

        Long loginUserPkId = loginUser.getId();

        // 리포지토리 구현체에서 loginUserPkId 를 이용해
        // 해당 리더가 담당하는 셀원 목록을 조회
        return membersRepository.findMembersByLeaderUserId(loginUserPkId);
    }

    /**
     * 출석 업서트 (있으면 UPDATE, 없으면 INSERT)
     */
    @Transactional
    public void updateAttendanceItems(List<AttendanceItemDto> items) {
        // 조금 더 실무처럼 바꿔보기.

        if (items == null || items.isEmpty())
            return;

        for (AttendanceItemDto dto : items) {

            // 필수 값 체크 (실무에서라면 예외 처리 or 로그)
            if (dto.getMemberId() == null || dto.getAttendanceDate() == null)
                continue;

            // 1) 기존 데이터 조회
            Attendances attendances = attendancesRepository
                    .findByMemberIdAndAttendanceDate(dto.getMemberId(), dto.getAttendanceDate())
                    .orElseGet(() ->
                            Attendances.builder()
                                    .memberId(dto.getMemberId())
                                    .attendanceDate(dto.getAttendanceDate())
                                    .build()
                    );

            // 2) 값 세팅 (null-safe 처리)
            attendances.setWorshipStatus(Boolean.TRUE.equals(dto.getWorshipStatus()));
            attendances.setCellStatus(Boolean.TRUE.equals(dto.getCellStatus()));

            // 메모가 공백이면 null 로 저장 (불필요한 빈 문자열 방지)
            String memo = dto.getAttendanceMemo();
            attendances.setAttendanceMemo(
                    (memo != null && !memo.isBlank()) ? memo : null
            );

            // 3) 저장 (새 엔티티면 insert, 기존이면 update)
            attendancesRepository.save(attendances);
        }
    }

    /**
     * 로그인한 리더의 셀원들 중,
     * 요청으로 들어온 memberIdList에 포함되는 멤버들에 한해서
     * 특정 날짜(attendanceDate)의 출석 데이터를 Map<Long, AttendanceItemDto> 로 반환
     */
    public Map<Long, AttendanceItemDto> getAttendanceMapForDateAndMembers(
            String loginUserId, LocalDate attendanceDate, List<Long> requestedMemberIdList) {

        if (attendanceDate == null || requestedMemberIdList == null || requestedMemberIdList.isEmpty())
            return Collections.emptyMap();

        // 1) 로그인한 리더의 셀원 목록 조회
        List<Members> myMembers = getMyCellMembers(loginUserId);
        if (myMembers.isEmpty())
            return Collections.emptyMap();

        // 2) 내 셀원 ID 집합
        Set<Long> myMemberIdSet = myMembers.stream()
                .map(Members::getId)
                .collect(Collectors.toSet());

        // 3) 요청으로 들어온 memberIds 중, 내 셀원에 해당하는 애들만 남김 (보안용 더블 체크)
        List<Long> targetIdList = requestedMemberIdList.stream()
                .filter(Objects::nonNull)
                .filter(myMemberIdSet::contains)
                .distinct()
                .collect(Collectors.toList());

        // 내 셀원에 해당하는 애들이 없다면 빈 Map 객체 반환
        if (targetIdList.isEmpty())
            return Collections.emptyMap();

        // 4) 해당 날짜 + targetIdList 에 대한 출석 데이터 조회
        List<Attendances> attendanceList =
                attendancesRepository.findByAttendanceDateAndMemberIdIn(attendanceDate, targetIdList);

        // 5) Map<Long, List<AttendanceItemDto>> 로 변환
        Map<Long, AttendanceItemDto> result = new HashMap<>();

        for (Attendances a : attendanceList) {
            Long memberId = a.getMemberId();
            AttendanceItemDto dto = new AttendanceItemDto();
            dto.setMemberId(memberId);
            dto.setAttendanceDate(a.getAttendanceDate());
            dto.setWorshipStatus(a.isWorshipStatus());
            dto.setCellStatus(a.isCellStatus());
            dto.setAttendanceMemo(a.getAttendanceMemo());

            result.put(memberId, dto);
        }

        return result;
    }
}
