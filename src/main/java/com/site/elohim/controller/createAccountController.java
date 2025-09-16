package com.site.elohim.controller;

import com.site.elohim.model.createAccountRequest;
import com.site.elohim.service.createAccountService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

@Controller
public class createAccountController {

    private final com.site.elohim.service.createAccountService createAccountService;

    public createAccountController(createAccountService createAccountService) {
        this.createAccountService = createAccountService;
    }

    @GetMapping("/createAccountPage")
    public String createAccountPage() {

        return "/createAccountPage";
    }

    @PostMapping("/process/createAccount")
    public ModelAndView processCreateAccount(createAccountRequest request) {
        ModelAndView mv;

        if(createAccountService.createAccount(request)){
            mv = new ModelAndView("redirect:/");
        }else {
            mv = new ModelAndView("redirect:/createAccountPage");
            return mv;
        }

        return mv;
    }

    @PostMapping("/process/userIdCheck")
    @ResponseBody
    public boolean processUserIdCheck(@RequestBody Map<String, String> data) {
        String userId = data.get("userId");
        return createAccountService.userIdCheck(userId);
    }

    @PostMapping("/process/userNameCheck")
    @ResponseBody
    public boolean processUserNameCheck(@RequestBody Map<String, String> data) {
        String userName = data.get("userName");
        return createAccountService.userNameCheck(userName);
    }

}
