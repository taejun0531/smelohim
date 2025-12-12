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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
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
     * [임원용] 기간별 개별 통계 계산
     *  - cellKey == -1  → 전체 멤버 기준
     *  - cellKey != -1 → 해당 셀 멤버 기준
     *  - startDate ~ endDate (포함)
     *  - 해당 기간 내 '주일' 기준으로 셀원별 결석/예배/셀모임 횟수 집계
     *  - 주일에 출석 row 자체가 없으면 결석으로 간주
     */
    public List<AttendanceStatsItemDto> getAttendanceStatsForCell(
            Long cellKey, LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null)
            return Collections.emptyList();

        // 1) 통계 대상 멤버 목록 결정
        List<Members> members;

        // 전체 버튼(ALL): 전체 멤버 기준
        if (cellKey != null && cellKey == -1L)
            members = membersRepository.findAllByOrderByMemberNameAsc();
        else {
            if (cellKey == null)
                return Collections.emptyList();
            members = getCellMembersByCellKey(cellKey);
        }

        if (members == null || members.isEmpty())
            return Collections.emptyList();

        // 2) 기간 내 주일 리스트
        List<LocalDate> sundays = getSundaysBetween(startDate, endDate);
        if (sundays.isEmpty()) {
            // 기간 안에 주일이 하나도 없으면 모든 값이 0인 row 반환
            Map<Long, AttendanceStatsItemDto> emptyMap = new LinkedHashMap<>();
            for (Members m : members) {
                AttendanceStatsItemDto dto = new AttendanceStatsItemDto();
                dto.setMemberId(m.getId());
                dto.setMemberName(m.getMemberName());
                dto.setAbsentCount(0);
                dto.setWorshipCount(0);
                dto.setCellCount(0);
                emptyMap.put(m.getId(), dto);
            }
            return new ArrayList<>(emptyMap.values());
        }

        List<Long> memberIds = members.stream()
                .map(Members::getId)
                .collect(Collectors.toList());

        // 3) 기간 내 출석 데이터 조회 (일단 전체 가져온 뒤, 주일만 필터)
        List<Attendances> attendanceList =
                attendancesRepository.findByAttendanceDateBetweenAndMemberIdIn(startDate, endDate, memberIds);

        // 4) 기본 row (출석 데이터 없어도 0으로 보이게)
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

        // 5) 멤버별 + 날짜별 출석을 빠르게 찾기 위한 맵 구성
        //    memberId -> (date -> Attendances)
        Map<Long, Map<LocalDate, Attendances>> attendMapByMember = new HashMap<>();
        for (Attendances a : attendanceList) {
            LocalDate date = a.getAttendanceDate();
            // 주일만 통계에 포함
            if (!sundays.contains(date))
                continue;

            attendMapByMember
                    .computeIfAbsent(a.getMemberId(), k -> new HashMap<>())
                    .put(date, a);
        }

        // 6) 주일 기준으로 통계 집계
        for (Members m : members) {
            Long memberId = m.getId();
            AttendanceStatsItemDto dto = map.get(memberId);
            if (dto == null)
                continue;

            Map<LocalDate, Attendances> byDateMap = attendMapByMember.getOrDefault(memberId, Collections.emptyMap());

            for (LocalDate sunday : sundays) {
                Attendances a = byDateMap.get(sunday);

                // 출석 row 자체가 없으면 → 결석
                if (a == null) {
                    dto.setAbsentCount(dto.getAbsentCount() + 1);
                    continue;
                }

                // 출결 규칙
                // 1) worship=true, cell=true  → 셀모임 +1
                // 2) worship=true, cell=false → 예배 +1
                // 3) worship=false, cell=true → 셀모임 +1
                // 4) 둘 다 false             → 결석 +1
                if (a.isCellStatus())
                    dto.setCellCount(dto.getCellCount() + 1);
                else if (a.isWorshipStatus())
                    dto.setWorshipCount(dto.getWorshipCount() + 1);
                else
                    dto.setAbsentCount(dto.getAbsentCount() + 1);
            }
        }

        return new ArrayList<>(map.values());
    }

    private List<LocalDate> getSundaysBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null || startDate.isAfter(endDate))
            return Collections.emptyList();

        List<LocalDate> sundays = new ArrayList<>();

        // 시작일 기준으로 첫 번째 주일 찾기
        LocalDate cursor = startDate;
        while (cursor.getDayOfWeek() != java.time.DayOfWeek.SUNDAY) {
            cursor = cursor.plusDays(1);
            // 기간 안에 주일이 하나도 없는 경우
            if (cursor.isAfter(endDate))
                return Collections.emptyList();
        }

        // 첫 주일부터 1주일 간격으로 endDate까지 추가
        while (!cursor.isAfter(endDate)) {
            sundays.add(cursor);
            cursor = cursor.plusWeeks(1);
        }

        return sundays;
    }

    /**
     * 임원용: 전체 멤버 조회
     */
    public List<Members> getAllMembersForAdmin() {
        return membersRepository.findAllByOrderByMemberNameAsc();
    }




    // ============================== 예배, 셀모임 빈도 서비스 ==============================
    /**
     * 출석 저장 완료 후, 변경된 멤버들의 예배, 셀모임 빈도 문자열을 members에 반영
     * - baptismStatus : 예배 비율(예배 or 셀 출석이면 출석 인정)
     * - worshipStatus : 셀모임 비율(셀 출석 true 기준)
     * - 저장 포맷: "올해비율 작년비율" (예: "87.2 97.0")
     */
    @Transactional
    public void updateAttendanceItemsAndUpdateMemberFrequency(List<AttendanceItemDto> items) {

        if (items == null || items.isEmpty())
            return;

        // 1) 변경된 memberId만(중복 제거) 수집
        List<Long> changedMemberIds = extractChangedMemberIds(items);


        // 주일이 아닌 날짜만 들어온 케이스는 저장 X
        if (changedMemberIds.isEmpty())
            return;

        // 2) 출석 업서트 먼저 수행
        updateAttendanceItems(items);

        // 3) 올해/작년 기간 계산
        LocalDate today = LocalDate.now();

        YearWindow thisYear = YearWindow.thisYearUntil(today);
        YearWindow lastYear = YearWindow.lastYearFull(today);

        // 4) 분모(주일 개수)
        long thisSundayCount = countSundays(thisYear.start, thisYear.end);
        long lastSundayCount = countSundays(lastYear.start, lastYear.end);

        // 5) DB 집계(올해/작년 각각 1번씩)
        Map<Long, AggCount> thisAgg = loadAggMap(thisYear.start, thisYear.end, changedMemberIds);
        Map<Long, AggCount> lastAgg = loadAggMap(lastYear.start, lastYear.end, changedMemberIds);

        // 6) 멤버별 문자열 생성 후 업데이트(멤버 수만큼)
        for (Long memberId : changedMemberIds) {

            AggCount t = thisAgg.getOrDefault(memberId, AggCount.ZERO);
            AggCount l = lastAgg.getOrDefault(memberId, AggCount.ZERO);

            String baptismStatusValue = ratioString(t.attendCount, thisSundayCount, l.attendCount, lastSundayCount);
            String worshipStatusValue = ratioString(t.cellCount,   thisSundayCount, l.cellCount,   lastSundayCount);

            membersRepository.updateAttendanceFrequencyById(memberId, baptismStatusValue, worshipStatusValue);
        }
    }

    // ====================== 아래부터는 “유틸” ======================

    private List<Long> extractChangedMemberIds(List<AttendanceItemDto> items) {

        Set<Long> set = new LinkedHashSet<>();

        for (AttendanceItemDto dto : items) {
            if (dto == null || dto.getMemberId() == null || dto.getAttendanceDate() == null)
                continue;

            // 주일만 대상(요구사항: 주일 기준 집계)
            if (dto.getAttendanceDate().getDayOfWeek() != DayOfWeek.SUNDAY)
                continue;

            set.add(dto.getMemberId());
        }

        return new ArrayList<>(set);
    }

    private Map<Long, AggCount> loadAggMap(LocalDate start, LocalDate end, List<Long> memberIds) {

        if (start == null || end == null || memberIds == null || memberIds.isEmpty())
            return Collections.emptyMap();

        List<AttendancesRepository.AttendanceAgg> rows =
                attendancesRepository.aggregateAttendance(start, end, memberIds);

        Map<Long, AggCount> map = new HashMap<>();

        for (AttendancesRepository.AttendanceAgg r : rows) {
            Long memberId = r.getMemberId();
            if (memberId == null) continue;

            long attend = (r.getAttendCount() == null) ? 0 : r.getAttendCount();
            long cell   = (r.getCellCount() == null) ? 0 : r.getCellCount();

            map.put(memberId, new AggCount(attend, cell));
        }

        return map;
    }

    private long countSundays(LocalDate start, LocalDate end) {

        if (start == null || end == null || start.isAfter(end))
            return 0;

        LocalDate cursor = start;

        // 첫 일요일까지 이동
        while (cursor.getDayOfWeek() != DayOfWeek.SUNDAY) {
            cursor = cursor.plusDays(1);
            if (cursor.isAfter(end))
                return 0;
        }

        long count = 0;
        while (!cursor.isAfter(end)) {
            count++;
            cursor = cursor.plusWeeks(1);
        }

        return count;
    }

    /**
     * (분자/분모)*100 을 소수점 1자리로 만든 후
     * "올해 작년" 문자열로 합침
     */
    private String ratioString(long thisNumerator, long thisDenominator,
                               long lastNumerator, long lastDenominator) {

        BigDecimal thisRatio = percentOneDecimal(thisNumerator, thisDenominator);
        BigDecimal lastRatio = percentOneDecimal(lastNumerator, lastDenominator);

        return thisRatio.toPlainString() + " " + lastRatio.toPlainString();
    }

    private BigDecimal percentOneDecimal(long numerator, long denominator) {

        if (denominator <= 0)
            return BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP);

        return BigDecimal.valueOf(numerator)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(denominator), 1, RoundingMode.HALF_UP);
    }

    private static class AggCount {
        final long attendCount; // worship OR cell
        final long cellCount;   // cell only

        static final AggCount ZERO = new AggCount(0, 0);

        AggCount(long attendCount, long cellCount) {
            this.attendCount = attendCount;
            this.cellCount = cellCount;
        }
    }

    private static class YearWindow {
        final LocalDate start;
        final LocalDate end;

        private YearWindow(LocalDate start, LocalDate end) {
            this.start = start;
            this.end = end;
        }

        static YearWindow thisYearUntil(LocalDate today) {
            int y = today.getYear();
            return new YearWindow(LocalDate.of(y, 1, 1), today);
        }

        static YearWindow lastYearFull(LocalDate today) {
            int y = today.getYear() - 1;
            return new YearWindow(LocalDate.of(y, 1, 1), LocalDate.of(y, 12, 31));
        }
    }


}
