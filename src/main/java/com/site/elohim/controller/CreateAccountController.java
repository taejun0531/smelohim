package com.site.elohim.controller;

import com.site.elohim.dto.CreateAccountRequest;
import com.site.elohim.dto.UserIdCheckRequest;
import com.site.elohim.dto.UserNameCheckRequest;
import com.site.elohim.service.CreateAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class CreateAccountController {

    private final CreateAccountService createAccountService;

    /**
     * 회원가입 페이지
     */
    @GetMapping("/createAccountPage")
    public String createAccountPage() {
        // templates/createAccountPage.html 렌더링
        return "createAccountPage";
    }

    /**
     * 회원가입 처리
     */
    @PostMapping("/process/createAccount")
    @ResponseBody
    public boolean processCreateAccount(@RequestBody CreateAccountRequest request) {
        return createAccountService.createAccount(request);
    }

    /**
     * 아이디 중복 체크
     * true  = 사용 가능
     * false = 이미 존재
     */
    @PostMapping("/process/userIdCheck")
    @ResponseBody
    public boolean processUserIdCheck(@RequestBody UserIdCheckRequest request) {
        return createAccountService.userIdCheck(request.getUserId());
    }

    /**
     * 이름 중복 체크
     * true  = 사용 가능
     * false = 이미 존재
     */
    @PostMapping("/process/userNameCheck")
    @ResponseBody
    public boolean processUserNameCheck(@RequestBody UserNameCheckRequest request) {
        return createAccountService.userNameCheck(request.getUserName());
    }

}
