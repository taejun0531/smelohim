package com.site.elohim.controller;

import com.site.elohim.dto.AttendanceItemDto;
import com.site.elohim.dto.AttendanceLoadRequest;
import com.site.elohim.dto.AttendanceSaveRequest;
import com.site.elohim.model.Members;
import com.site.elohim.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

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
            log.debug("[Attendance] empty request from userId={}", loginUserId);
            return false;
        }

        try {
            attendanceService.updateAttendanceItems(request.getItems());
            log.info("[Attendance] successfully updated attendance. userId={}, itemCount={}",
                    loginUserId, request.getItems().size());
            return true;
        } catch (IllegalArgumentException e) {
            // 서비스 계층에서 유효하지 않은 입력에 대해 IllegalArgumentException 던지는 경우
            log.warn("[Attendance] invalid attendance request. userId={}, reason={}",
                    loginUserId, e.getMessage());
            return false;
        } catch (Exception e) {
            // 예기치 못한 모든 에러 방어 (DB 오류 등)
            log.error("[Attendance] failed to update attendance. userId={}", loginUserId, e);
            return false;
        }
    }

    // 조회용: 날짜 + 멤버 집합 기반, Map<memberId, AttendanceItemDto> 반환
    @PostMapping("/user/loadAttendance")
    @ResponseBody
    public Map<Long, AttendanceItemDto> loadAttendance(@RequestBody AttendanceLoadRequest request,
                                                       @AuthenticationPrincipal UserDetails user) {

        if (request == null ||
                request.getAttendanceDate() == null ||
                request.getAttendingMemberIdList() == null ||
                request.getAttendingMemberIdList().isEmpty()) {
            return Collections.emptyMap();
        }

        String loginUserId = user.getUsername();

        return attendanceService.getAttendanceMapForDateAndMembers(
                loginUserId,
                request.getAttendanceDate(),
                request.getAttendingMemberIdList()
        );
    }
}
