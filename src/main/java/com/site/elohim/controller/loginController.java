package com.site.elohim.controller;

import com.site.elohim.model.Users;
import com.site.elohim.service.UsersService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.Optional;

@Controller
public class loginController {
    @Autowired
    private UsersService usersService;

    @GetMapping("/loginpage")
    public String loginPage() {

        return "views/loginPage";
    }

    @PostMapping("/process/login")
    public ModelAndView processLogin(String login_id, String login_password, HttpSession session, Model model) {

       Optional<Users> user = usersService.selectUsers(login_id, login_password);
       ModelAndView mnv;
       if(!user.isEmpty()){
           if(user.get().getUserRole().equals("AWAIT"))
               mnv = new ModelAndView("views/mainPage_await");
           else
               mnv = new ModelAndView("mainPage_login_user");
           mnv.addObject("users", user.get());
       }else {
           mnv = new ModelAndView("views/loginPage");
       }

       System.out.println("loginController" + mnv);

       return mnv;
    }

}
