package com.site.elohim.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/loginPage")
    public String loginPage() {

        // 로그인 페이지 url은 사용하지 않고
        // home URL에서 로그인 페이지 보여줄 수 있도록 redirect함.
        return "redirect:/";
    }

}
