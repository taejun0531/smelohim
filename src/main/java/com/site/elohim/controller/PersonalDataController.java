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
                                       @RequestBody PersonalDataSearchRequest req,
                                       Model model) {

        model.addAttribute("username", user.getUsername());

        String category = req.getFindCategory();
        List<Members> memberList;  // 기본 빈 리스트 대신 바로 switch에서 할당

        switch (category) {
            case "name" -> {
                String name = req.getFindName();
                memberList = personalDataService.getMembersByName(name);
            }
            case "birth" -> {
                String birthYear = req.getFindBirth_year();
                String birthMonth = req.getFindBirth_month();
                memberList = personalDataService.getMembersByBirth(birthYear, birthMonth);
            }
            case "allCellLeader" -> {
                memberList = personalDataService.getMembersCellLeader();
            }
            case "cellLeader" -> {
                String cellLeader = req.getFindCellLeader();
                Long cellKey = (cellLeader != null && !cellLeader.isBlank()) ? Long.parseLong(cellLeader) : null;
                if (cellKey != null)
                    memberList = personalDataService.getMembersByCellKey(cellKey);
                else
                    memberList = List.of();
            }
            case "baptism" -> {
                String baptism = req.getFindBaptism();
                memberList = personalDataService.getMembersByBaptism(baptism);
            }
            case "nurture" -> {
                String nurtureYear = req.getFindNurture_year();
                String nurtureSemester = req.getFindNurture_semester();

                if (nurtureYear == null || nurtureYear.isBlank())
                    memberList = personalDataService.getMembersByNurtureSemester(nurtureSemester);
                else if (nurtureSemester == null || nurtureSemester.isBlank())
                    memberList = personalDataService.getMembersByNurtureYear(nurtureYear);
                else
                    memberList = personalDataService.getMembersByNurtureYearAndNurtureSemester(nurtureYear, nurtureSemester);
            }
            case "growth" -> {
                String growthYear = req.getFindGrowth_year();
                String growthSemester = req.getFindGrowth_semester();

                if (growthYear == null || growthYear.isBlank())
                    memberList = personalDataService.getMembersByGrowthSemester(growthSemester);
                else if (growthSemester == null || growthSemester.isBlank())
                    memberList = personalDataService.getMembersByGrowthYear(growthYear);
                else
                    memberList = personalDataService.getMembersByGrowthYearAndGrowthSemester(growthYear, growthSemester);
            }
            case "nameSort" -> {
                Long sortId = parseLongOrNull(req.getSortId());
                memberList = personalDataService.getMembersNameSort(sortId);
            }
            case "birthSort" -> {
                Long sortId = parseLongOrNull(req.getSortId());
                memberList = personalDataService.getMembersBirthSort(sortId);
            }
            default -> {
                // "none" 카테고리면 전체 조회
                memberList = personalDataService.getAllMember();
            }
        }

        model.addAttribute("memberList", memberList);

        List<Members> cellLeaderList = personalDataService.getMembersCellLeader();
        model.addAttribute("cellLeaderList", cellLeaderList);

        return "personalDataPage";
    }

    private Long parseLongOrNull(String value) {
        if (value == null || value.isBlank())
            return null;

        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
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

        // 필수값 이름만 체크
        if (req.getMemberName() == null || req.getMemberName().isBlank())
            return false;

        Members member = Members.builder()
                .memberName(req.getMemberName())
                .memberBirth(req.getMemberBirth())
                .memberPhoneNumber(req.getMemberPhoneNumber())
                .baptismStatus(req.getMemberBaptism())
                .nurtureYear(req.getNurtureYear())
                .nurtureSemester(req.getNurtureSemester())
                .growthYear(req.getGrowthYear())
                .growthSemester(req.getGrowthSemester())
                .cellKey(req.getCellKey())
                .cellName(req.getCellName())
                .cellLeaderStatus(false)
                .build();

        return personalDataService.createMember(member);
    }
}
