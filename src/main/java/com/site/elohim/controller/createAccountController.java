package com.site.elohim.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class createAccountController {

    @GetMapping("/createaccountpage")
    public String createAccountPage() {

        return "views/createAccountPage";
    }

}
