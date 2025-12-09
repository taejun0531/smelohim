package com.site.elohim.service;

import com.site.elohim.dto.AttendanceItemDto;
import com.site.elohim.dto.AttendanceStatsItemDto;
import com.site.elohim.dto.CellSummaryDto;
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

        // 5) Map<Long, AttendanceItemDto> 로 변환
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

    // ======================== ADMIN (임원용) 서비스 메서드 ========================

    /**
     * [임원용] 전체 셀 목록 가져오기 (셀리더 기준)
     */
    public List<CellSummaryDto> getAllCellsForAdmin() {

        List<Members> leaders =
                membersRepository.findByCellLeaderStatusOrderByMemberNameAsc(true);

        if (leaders == null || leaders.isEmpty())
            return Collections.emptyList();

        List<CellSummaryDto> result = new ArrayList<>();

        for (Members m : leaders) {
            CellSummaryDto dto = new CellSummaryDto();
            dto.setCellKey(m.getId()); // 셀리더 member.id 를 cellKey로 사용

            // cellName이 비어있으면 "이름 + 셀" 형태로 보완
            String cellName = m.getCellName();
            if (cellName == null || cellName.isBlank()) {
                cellName = m.getMemberName() + "셀";
            }

            dto.setCellName(cellName);
            result.add(dto);
        }

        return result;
    }

    /**
     * [임원용] 특정 셀(cellKey)의 셀원 목록
     */
    public List<Members> getCellMembersByCellKey(Long cellKey) {
        if (cellKey == null)
            return Collections.emptyList();

        return membersRepository.findByCellKeyOrderByMemberNameAsc(cellKey);
    }

    /**
     * [임원용] 날짜 + 멤버 집합 기반 출석 Map
     * - 리더 필터 없이, 요청된 memberIdList 전체에 대해 조회
     */
    public Map<Long, AttendanceItemDto> getAttendanceMapForAdmin(
            LocalDate attendanceDate, List<Long> requestedMemberIdList) {

        if (attendanceDate == null || requestedMemberIdList == null || requestedMemberIdList.isEmpty())
            return Collections.emptyMap();

        List<Long> targetIdList = requestedMemberIdList.stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        if (targetIdList.isEmpty())
            return Collections.emptyMap();

        List<Attendances> attendanceList =
                attendancesRepository.findByAttendanceDateAndMemberIdIn(attendanceDate, targetIdList);

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

    /**
     * [임원용] 특정 셀에 대해 기간별 개별 통계 계산
     *  - startDate ~ endDate (포함)
     *  - 셀원별 결석/예배/셀모임 횟수
     */
    public List<AttendanceStatsItemDto> getAttendanceStatsForCell(
            Long cellKey,
            LocalDate startDate,
            LocalDate endDate
    ) {
        if (cellKey == null || startDate == null || endDate == null)
            return Collections.emptyList();

        // 1) 셀원 목록
        List<Members> members = getCellMembersByCellKey(cellKey);
        if (members.isEmpty())
            return Collections.emptyList();

        List<Long> memberIds = members.stream()
                .map(Members::getId)
                .collect(Collectors.toList());

        // 2) 기간 내 출석 데이터 조회
        List<Attendances> attendanceList =
                attendancesRepository.findByAttendanceDateBetweenAndMemberIdIn(
                        startDate, endDate, memberIds
                );

        // 3) 기본 row (출석 데이터 없어도 0으로 보이게)
        Map<Long, AttendanceStatsItemDto> map = new LinkedHashMap<>();
        for (Members m : members) {
            AttendanceStatsItemDto dto = new AttendanceStatsItemDto();
            dto.setMemberId(m.getId());
            dto.setMemberName(m.getMemberName());
            dto.setAbsentCount(0);
            dto.setWorshipCount(0);
            dto.setCellCount(0);
            map.put(m.getId(), dto);
        }

        // 4) 집계 규칙
        //    1) worship=true, cell=true  → 셀모임 +1
        //    2) worship=true, cell=false → 예배 +1
        //    3) worship=false, cell=true → 셀모임 +1
        //    4) 둘 다 false             → 결석 +1
        for (Attendances a : attendanceList) {
            AttendanceStatsItemDto dto = map.get(a.getMemberId());
            if (dto == null)
                continue;

            boolean w = a.isWorshipStatus();
            boolean c = a.isCellStatus();

            if (c) {
                dto.setCellCount(dto.getCellCount() + 1);
            } else if (w) {
                dto.setWorshipCount(dto.getWorshipCount() + 1);
            } else {
                dto.setAbsentCount(dto.getAbsentCount() + 1);
            }
        }

        return new ArrayList<>(map.values());
    }
}
