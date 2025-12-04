package com.site.elohim.controller;

import com.site.elohim.dto.UpdateMemberRequest;
import com.site.elohim.dto.DeleteMemberRequest;
import com.site.elohim.model.Members;
import com.site.elohim.service.MemberDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MemberDetailsController {

    private final MemberDetailsService memberDetailsService;

    /**
     * 멤버 상세 페이지
     * ex) /admin/memberDetailsPage?memberId=3
     */
    @GetMapping("/admin/memberDetailsPage")
    public String memberDetailsPage(@RequestParam Long memberId, Model model) {

        Members member = memberDetailsService.findByMemberId(memberId).orElse(null);
        model.addAttribute("member", member);

        List<Members> cellLeaderList = memberDetailsService.getMembersCellLeader();
        model.addAttribute("cellLeaderList", cellLeaderList);

        return "memberDetailsPage";
    }

    /**
     * 멤버 정보 수정
     */
    @PostMapping("/admin/updateMember")
    @ResponseBody
    public boolean updateMember(@RequestBody UpdateMemberRequest req) {
        return memberDetailsService.updateMember(req);
    }

    /**
     * 멤버 삭제
     * 요청 JSON: { "deleteMemberId": 3 }
     */
    @PostMapping("/admin/deleteMember")
    @ResponseBody
    public boolean deleteMember(@RequestBody DeleteMemberRequest request) {
        return memberDetailsService.deleteMember(request.getDeleteMemberId());
    }
}
