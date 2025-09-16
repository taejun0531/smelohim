package com.site.elohim.controller;

import com.site.elohim.model.Members;
import com.site.elohim.service.updateMemberService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;

@Controller
public class updateMemberController {

    private final updateMemberService service;

    public updateMemberController(updateMemberService service) {
        this.service = service;
    }

    @GetMapping("/admin/updateMemberPage")
    public ModelAndView updateMemberPage(@RequestParam Map<String, String> data) {
        ModelAndView mv = new ModelAndView("/updateMemberPage");
        Long memberId = Long.parseLong(data.get("memberId"));
        Members member = service.getMemberByMemberId(memberId);

        if(member == null)
            return mv;

        mv.addObject("name", member.getMemberName());
        mv.addObject("birth", member.getMemberBirth());
        mv.addObject("phoneNumber", member.getMemberPhoneNumber());
        mv.addObject("address", member.getMemberAddress());
        mv.addObject("baptismStatus", member.getBaptismStatus());
        mv.addObject("worshipStatus", member.getWorshipStatus());
        mv.addObject("cellStatus", member.getCellStatus());
        mv.addObject("nurtureYear", member.getNurtureYear());
        mv.addObject("nurtureSemester", member.getNurtureSemester());
        mv.addObject("growthYear", member.getGrowthYear());
        mv.addObject("growthSemester", member.getGrowthSemester());
        mv.addObject("memo", member.getMemberMemo());
        mv.addObject("cellLeaderStatus", member.isCellLeaderStatus());
        mv.addObject("cellKey", member.getCellKey());
        mv.addObject("cellName", member.getCellName());

        return mv;
    }
}
