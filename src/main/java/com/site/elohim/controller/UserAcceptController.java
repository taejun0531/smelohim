package com.site.elohim.controller;

import com.site.elohim.dto.UserAcceptRequest;
import com.site.elohim.dto.UserDeleteRequest;
import com.site.elohim.dto.LeaderIdCheckRequest;
import com.site.elohim.model.Members;
import com.site.elohim.model.Users;
import com.site.elohim.service.UserAcceptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class UserAcceptController {

    private final UserAcceptService userAcceptService;

    /**
     * 사용자 승인 화면
     */
    @GetMapping("/admin/userAcceptPage")
    public String userAcceptPage(Model model) {

        List<Users> awaitList      = userAcceptService.findAllUserByRole("AWAIT");
        List<Users> cellLeaderList = userAcceptService.findAllUserByRole("USER");
        List<Users> adminList      = userAcceptService.findAllUserByRole("ADMIN");

        model.addAttribute("awaitList", awaitList);
        model.addAttribute("cellLeaderList", cellLeaderList);
        model.addAttribute("adminList", adminList);

        // userAcceptPage.html
        return "userAcceptPage";
    }

    /**
     * 셀 리더로 연결할 memberId 가 이미 다른 유저에 매핑되어 있는지 확인
     * - 비어 있으면 true
     * - 이미 사용 중이면 false
     */
    @PostMapping("/admin/isEmptyLeaderId")
    @ResponseBody
    public boolean isEmptyLeaderId(@RequestBody LeaderIdCheckRequest request) {
        return userAcceptService.isLeaderIdEmpty(request.getLeaderId());
    }

    /**
     * 대기 유저 승인 / 권한 변경 / memberId 연결
     */
    @PostMapping("/admin/accept_user")
    @ResponseBody
    public boolean acceptUser(@RequestBody UserAcceptRequest request) {
        return userAcceptService.updateUser(request);
    }

    /**
     * 셀 리더로 등록된 멤버 목록 조회
     */
    @PostMapping("/admin/getCellLeaderInfo")
    @ResponseBody
    public List<Members> getCellLeaderInfo() {
        return userAcceptService.getMembersCellLeader();
    }

    /**
     * 유저 삭제
     */
    @PostMapping("/admin/delete_user")
    @ResponseBody
    public boolean deleteUser(@RequestBody UserDeleteRequest request) {
        return userAcceptService.deleteUser(request.getDeleteId());
    }
}
