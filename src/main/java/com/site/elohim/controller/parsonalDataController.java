package com.site.elohim.controller;

import com.site.elohim.model.Members;
import com.site.elohim.service.parsonalDataService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;

@Controller
public class parsonalDataController {

    private final parsonalDataService service;

    public parsonalDataController(parsonalDataService service) {
        this.service = service;
    }

    @GetMapping("/admin/parsonalDataPage")
    public ModelAndView parsonalDataPage(Model model) {
        ModelAndView mnv = new ModelAndView("/parsonalDataPage");
        UserDetails user = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        mnv.addObject("username", user.getUsername());

        List<Members> memberList = service.getAllMember();
        mnv.addObject("memberList", memberList);

        List<Members> cellLeaderList = service.getMembersCellLeader();
        mnv.addObject("cellLeaderList", cellLeaderList);

        return mnv;
    }

    @PostMapping("/admin/parsonalDataPage")
    @ResponseBody
    public ModelAndView findParsonalDataPage(@RequestBody Map<String, String> data) {
        ModelAndView mv = new ModelAndView("/parsonalDataPage");
        UserDetails user = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        mv.addObject("username", user.getUsername());

        // 검색한 내용 List로 담아서 보내기
        String category = data.get("findCategory");

        switch (category) {
            case "name" -> {
                String name = data.get("findName");
                mv.addObject("memberList", service.getMembersByName(name));
                System.out.println(service.getMembersByName(name));
            }
            case "birth" -> {
                String birth = data.get("findBirth_year") + '-' + data.get("findBirth_month");
                mv.addObject("memberList", service.getMembersByBirth(birth));
            }
            case "cellLeader" -> {
                String cellLeader = data.get("findCellLeader");
                mv.addObject("memberList", service.getMembersByCellKey(Long.parseLong(cellLeader)));
            }
            case "baptism" -> {
                String baptism = data.get("findBaptism");
                mv.addObject("memberList", service.getMembersByBaptism(baptism));
            }
            case "nurture" -> {
                String nurture_year = data.get("findNurture_year");
                String nurture_semester = data.get("findNurture_semester");
                System.out.println(nurture_year);
                System.out.println(nurture_semester);

                if (nurture_year.isEmpty()) {
                    System.out.println("nurture_year is empty");
                    mv.addObject("memberList", service.getMembersByNurtureSemester(nurture_semester));
                } else if (nurture_semester.isEmpty()) {
                    System.out.println("nurture_semester is empty");
                    mv.addObject("memberList", service.getMembersByNurtureYear(nurture_year));
                } else {
                    System.out.println("nurture_year and nurture_semester are not empty");
                    mv.addObject("memberList", service.getMembersByNurtureYearAndNurtureSemester(nurture_year, nurture_semester));
                }
            }
            case "growth" -> {
                String growth_year = data.get("findGrowth_year");
                String growth_semester = data.get("findGrowth_semester");

                if (growth_year.isEmpty()) {
                    mv.addObject("memberList", service.getMembersByGrowthSemester(growth_semester));
                } else if (growth_semester.isEmpty()) {
                    mv.addObject("memberList", service.getMembersByGrowthYear(growth_year));
                } else {
                    mv.addObject("memberList", service.getMembersByGrowthYearAndGrowthSemester(growth_year, growth_semester));
                }
            }
            case "nameSort" -> {
                Long sortId = Long.parseLong(data.get("sortId"));
                mv.addObject("memberList", service.getMembersNameSort(sortId));
            }
            case "birthSort" -> {
                Long sortId = Long.parseLong(data.get("sortId"));
                mv.addObject("memberList", service.getMembersBirthSort(sortId));
            }
        }

        List<Members> cellLeaderList = service.getMembersCellLeader();
        mv.addObject("cellLeaderList", cellLeaderList);

        return mv;
    }

    @PostMapping("/admin/deleteMember")
    @ResponseBody
    public boolean deleteMember(@RequestBody Map<String, String> data) {
        Long memberId = Long.parseLong(data.get("deleteMemberId"));

        return service.deleteMemberById(memberId);
    }

    @PostMapping("/admin/checkMember")
    @ResponseBody
    public boolean checkMember(@RequestBody Map<String, String> data) {
        String name = data.get("memberName");
        String birth = data.get("memberBirth");
        // 있다면 true 없다면 false
        return service.checkMember(name, birth);
    }

    @PostMapping("/admin/createMember")
    @ResponseBody
    public boolean createMember(@RequestBody Map<String, String> data) {
        Members member = Members.builder()
                .memberName(data.get("memberName"))
                .memberBirth(data.get("memberBirth"))
                .memberPhoneNumber(data.get("memberPhoneNumber"))
                .memberAddress(data.get("memberAddress"))
                .baptismStatus(data.get("memberBaptism"))
                .nurtureYear(data.get("memberNurtureYear"))
                .nurtureSemester(data.get("memberNurtureSemester"))
                .growthYear(data.get("memberGrowthYear"))
                .growthSemester(data.get("memberGrowthSemester"))
                .memberMemo(data.get("memberMemo"))
                .cellLeaderStatus(Boolean.parseBoolean(data.get("memberCellLeader")))
                .build();

        return service.createMember(member);
    }

}
