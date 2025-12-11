package com.site.elohim.controller;

import com.site.elohim.dto.CreateMemberRequest;
import com.site.elohim.dto.PersonalDataSearchRequest;
import com.site.elohim.dto.CheckMemberRequest;
import com.site.elohim.model.Members;
import com.site.elohim.service.PersonalDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class PersonalDataController {

    private final PersonalDataService personalDataService;

    /**
     * 관리자 > 인적사항 관리 페이지 (초기 진입)
     */
    @GetMapping("/admin/personalDataPage")
    public String personalDataPage(@AuthenticationPrincipal UserDetails user, Model model) {

        model.addAttribute("username", user.getUsername());

        // 전체 멤버 목록
        List<Members> memberList = personalDataService.getAllMember();
        model.addAttribute("memberList", memberList);

        // 셀 리더 목록
        List<Members> cellLeaderList = personalDataService.getMembersCellLeader();
        model.addAttribute("cellLeaderList", cellLeaderList);

        return "personalDataPage";
    }

    /**
     * 검색 조건에 따른 인적사항 조회
     * - 요청은 JSON으로 오지만, 응답은 personalDataPage.html 렌더링된 전체 HTML
     */
    @PostMapping("/admin/personalDataPage")
    public String findPersonalDataPage(@AuthenticationPrincipal UserDetails user,
                                       @RequestBody PersonalDataSearchRequest req, Model model) {

        model.addAttribute("username", user.getUsername());

        // 검색/정렬 로직은 Service로 위임
        List<Members> memberList = personalDataService.searchMembers(req);
        model.addAttribute("memberList", memberList);

        // 셀 리더 목록은 동일하게 항상 세팅
        List<Members> cellLeaderList = personalDataService.getMembersCellLeader();
        model.addAttribute("cellLeaderList", cellLeaderList);

        return "personalDataPage";
    }

    /**
     * 멤버 중복 여부 확인
     * - 있다면 true, 없다면 false
     */
    @PostMapping("/admin/checkMember")
    @ResponseBody
    public boolean checkMember(@RequestBody CheckMemberRequest req) {

        String name = req.getMemberName();
        String birth = req.getMemberBirth();

        LocalDate birthDate = null;
        if (birth != null && !birth.isBlank() && !"null".equalsIgnoreCase(birth))
            birthDate = LocalDate.parse(birth);

        return personalDataService.checkMember(name, birthDate);
    }

    /**
     * 멤버 신규 생성
     */
    @PostMapping("/admin/createMember")
    @ResponseBody
    public boolean createMember(@RequestBody CreateMemberRequest req) {

        // 필수값 체크/엔티티 생성/저장은 Service에 위임
        return personalDataService.createMember(req);
    }
}
