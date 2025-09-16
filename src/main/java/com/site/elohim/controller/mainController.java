package com.site.elohim.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;


@Controller
public class mainController {

    @GetMapping("/")
    public ModelAndView home(Model model) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!(principal instanceof UserDetails))
            return new ModelAndView("/loginPage");

        UserDetails user = (UserDetails) principal;
        String name = user.getUsername();
        String authorities = user.getAuthorities().toArray()[0].toString();

        ModelAndView mnv;
        if(authorities.contains("ROLE_ADMIN")) { 
            mnv = new ModelAndView("/mainPage_admin");
            mnv.addObject("username", name);
        }else if(authorities.contains("ROLE_USER")) {
            mnv = new ModelAndView("/mainPage_user");
            mnv.addObject("username", name);
        }else if(authorities.contains("ROLE_AWAIT")) {
            mnv = new ModelAndView("/mainPage_await");
            mnv.addObject("username", name);
        }else {
            System.out.println(authorities + " mainController() 오류");
            mnv = new ModelAndView("/loginPage");
        }

        return mnv;
    }

}
