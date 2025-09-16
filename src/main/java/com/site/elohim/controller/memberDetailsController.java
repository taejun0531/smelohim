package com.site.elohim.controller;

import com.site.elohim.model.Members;
import com.site.elohim.service.memberDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Optional;

@Controller
public class memberDetailsController {

    private final memberDetailsService service;

    public memberDetailsController(memberDetailsService service) { this.service = service; }

    @GetMapping("/user/memberDetailsPage")
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

}
