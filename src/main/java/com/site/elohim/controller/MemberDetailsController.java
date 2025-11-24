package com.site.elohim.controller;

import com.site.elohim.dto.UpdateMemberRequest;
import com.site.elohim.model.Members;
import com.site.elohim.service.MemberDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class MemberDetailsController {

    private final MemberDetailsService service;

    public MemberDetailsController(MemberDetailsService service) { this.service = service; }

    @GetMapping("/admin/memberDetailsPage")
    public ModelAndView memberDetailsPage(@RequestParam Long memberId) {
        ModelAndView mnv = new ModelAndView("memberDetailsPage");

        Optional<Members> member = service.findByMemberId(memberId);
        if (member.isPresent()) {
            mnv.addObject("member", member.get());
        }

        List<Members> cellLeaderList = service.getMembersCellLeader();
        mnv.addObject("cellLeaderList", cellLeaderList);

        return mnv;
    }

    @PostMapping("/admin/updateMember")
    @ResponseBody
    public boolean updateMember(@RequestBody UpdateMemberRequest req) {
        return service.updateMember(req);
    }

    @PostMapping("/admin/deleteMember")
    @ResponseBody
    public boolean deleteMember(@RequestBody Map<String, String> data) {
        return service.deleteMember(Long.parseLong(data.get("deleteMemberId")));
    }

}
