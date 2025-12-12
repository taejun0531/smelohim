package com.site.elohim.controller;

import com.site.elohim.dto.*;
import com.site.elohim.model.Members;
import com.site.elohim.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    // ====================== USER (셀리더/셀원용) ======================

    @GetMapping("/user/attendancePage")
    public String attendancePage(@AuthenticationPrincipal UserDetails user, Model model) {

        String loginUserId = user.getUsername(); // userId

        List<Members> memberList = attendanceService.getMyCellMembers(loginUserId);

        String cellName = null;
        if (!memberList.isEmpty())
            cellName = memberList.get(0).getCellName();

        model.addAttribute("cellName", cellName);
        model.addAttribute("memberList", memberList);

        return "userAttendancePage";
    }

    @PostMapping("/user/updateAttendance")
    @ResponseBody
    public boolean updateAttendance(@RequestBody AttendanceSaveRequest request,
                                    @AuthenticationPrincipal UserDetails user) {

        final String loginUserId = user.getUsername();

        // 요청 바디 기본 검증
        if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
            log.debug("[Attendance-USER] empty request from userId={}", loginUserId);
            return false;
        }

        try {
            attendanceService.updateAttendanceItemsAndUpdateMemberFrequency(request.getItems());
            log.info("[Attendance-USER] successfully updated attendance. userId={}, itemCount={}",
                    loginUserId, request.getItems().size());
            return true;
        } catch (IllegalArgumentException e) {
            // 서비스 계층에서 유효하지 않은 입력에 대해 IllegalArgumentException 던지는 경우
            log.warn("[Attendance-USER] invalid attendance request. userId={}, reason={}",
                    loginUserId, e.getMessage());
            return false;
        } catch (Exception e) {
            // 예기치 못한 모든 에러 방어 (DB 오류 등)
            log.error("[Attendance-USER] failed to update attendance. userId={}", loginUserId, e);
            return false;
        }
    }

    // 조회용: 날짜 + 멤버 집합 기반, Map<memberId, AttendanceItemDto> 반환
    @PostMapping("/user/loadAttendance")
    @ResponseBody
    public Map<Long, AttendanceItemDto> loadAttendance(@RequestBody AttendanceLoadRequest request,
                                                       @AuthenticationPrincipal UserDetails user) {

        if (request == null || request.getAttendanceDate() == null ||
                request.getAttendingMemberIdList() == null || request.getAttendingMemberIdList().isEmpty())
            return Collections.emptyMap();

        String loginUserId = user.getUsername();

        return attendanceService.getAttendanceMapForDateAndMembers(
                loginUserId,
                request.getAttendanceDate(),
                request.getAttendingMemberIdList()
        );
    }

    // ====================== ADMIN (임원용) ======================

    /**
     * 임원용 출석 페이지
     * - 전체 셀 목록
     * - 기본 선택 셀(defaultCell)의 셀원 목록
     */
    @GetMapping("/admin/attendancePage")
    public String adminAttendancePage(Model model) {

        List<CellSummaryDto> cellList = attendanceService.getAllCellsForAdmin();

        model.addAttribute("cellList", cellList);

        return "adminAttendancePage";
    }

    /**
     * 임원용: 날짜 + 멤버 집합 출석 조회
     */
    @PostMapping("/admin/loadAttendance")
    @ResponseBody
    public Map<Long, AttendanceItemDto> loadAttendanceForAdmin(@RequestBody AttendanceLoadRequest request) {

        if (request == null ||
                request.getAttendanceDate() == null ||
                request.getAttendingMemberIdList() == null ||
                request.getAttendingMemberIdList().isEmpty()) {
            return Collections.emptyMap();
        }

        return attendanceService.getAttendanceMapForAdmin(
                request.getAttendanceDate(),
                request.getAttendingMemberIdList()
        );
    }

    /**
     * 임원용: 출석 저장 (특정 셀 제한 없이 전체 허용)
     */
    @PostMapping("/admin/updateAttendance")
    @ResponseBody
    public boolean updateAttendanceForAdmin(@RequestBody AttendanceSaveRequest request) {

        if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
            log.debug("[Attendance-ADMIN] empty request.");
            return false;
        }

        try {
            attendanceService.updateAttendanceItemsAndUpdateMemberFrequency(request.getItems());
            log.info("[Attendance-ADMIN] successfully updated attendance. itemCount={}",
                    request.getItems().size());
            return true;
        } catch (Exception e) {
            log.error("[Attendance-ADMIN] failed to update attendance.", e);
            return false;
        }
    }

    /**
     * 임원용: 셀 선택 시 셀원 목록 조회
     */
    @GetMapping("/admin/cellMembers")
    @ResponseBody
    public List<MemberSimpleDto> loadCellMembers(@RequestParam Long cellKey) {

        if (cellKey == null) {
            return Collections.emptyList();
        }

        List<Members> members = attendanceService.getCellMembersByCellKey(cellKey);

        return members.stream()
                .map(m -> {
                    MemberSimpleDto dto = new MemberSimpleDto();
                    dto.setId(m.getId());
                    dto.setMemberName(m.getMemberName());
                    return dto;
                })
                .toList();
    }

    /**
     * 임원용: 개별 통계 조회 (기간 + 셀 기준)
     */
    @PostMapping("/admin/loadAttendanceStats")
    @ResponseBody
    public List<AttendanceStatsItemDto> loadAttendanceStats(@RequestBody AttendanceStatsRequest request) {

        if (request == null ||
                request.getCellKey() == null ||
                request.getStartDate() == null ||
                request.getEndDate() == null) {
            return Collections.emptyList();
        }

        return attendanceService.getAttendanceStatsForCell(
                request.getCellKey(),
                request.getStartDate(),
                request.getEndDate()
        );
    }

    /**
     * 임원용: 전체 멤버 조회 (전체 버튼 클릭 시)
     */
    @GetMapping("/admin/allMembers")
    @ResponseBody
    public List<MemberSimpleDto> loadAllMembers() {
        List<Members> members = attendanceService.getAllMembersForAdmin();

        return members.stream()
                .map(m -> {
                    MemberSimpleDto dto = new MemberSimpleDto();
                    dto.setId(m.getId());
                    dto.setMemberName(m.getMemberName());
                    return dto;
                })
                .toList();
    }
}
