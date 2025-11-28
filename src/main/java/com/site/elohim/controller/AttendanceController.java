package com.site.elohim.controller;

import com.site.elohim.model.Members;
import com.site.elohim.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @GetMapping("/user/attendancePage")
    public String attendancePage(@AuthenticationPrincipal UserDetails user, Model model) {

        // 혹시 인증 정보가 없으면 로그인 페이지로 보내도 됨 (SecurityConfig에 따라 조정)
        if (user == null)
            return "redirect:/loginPage";

        String loginUserId = user.getUsername(); // userId

        List<Members> memberList = attendanceService.getMyCellMembers(loginUserId);

        String cellName = null;
        if (!memberList.isEmpty())
            cellName = memberList.get(0).getCellName();

        model.addAttribute("cellName", cellName);
        model.addAttribute("memberList", memberList);

        return "userAttendancePage";
    }

}
