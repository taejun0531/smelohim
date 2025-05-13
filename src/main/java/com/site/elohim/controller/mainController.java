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
        System.out.println("home() : " + principal.toString());
        if (!(principal instanceof UserDetails))
            return new ModelAndView("views/loginPage");
        UserDetails userDetails = (UserDetails) principal;
        String name = userDetails.getUsername();
        String authorities = userDetails.getAuthorities().toArray()[0].toString();

        ModelAndView mnv;
        if(authorities.contains("ROLE_ADMIN")) {
            mnv = new ModelAndView("views/mainPage_admin");
            mnv.addObject("username", name);
            mnv.addObject("authorities", authorities);
        }else if(authorities.contains("ROLE_USER")) {
            mnv = new ModelAndView("views/mainPage_user");
            mnv.addObject("username", name);
            mnv.addObject("authorities", authorities);
        }else if(authorities.contains("ROLE_AWAIT")) {
            mnv = new ModelAndView("views/mainPage_await");
            mnv.addObject("username", name);
            mnv.addObject("authorities", authorities);
        }else {
            mnv = new ModelAndView("views/mainPage_intro");
        }

        System.out.println("home()");

        return mnv;
    }

    @GetMapping("/admin/administratorPage")
    public ModelAndView administratorPage(Model model) {
        ModelAndView mnv = new ModelAndView("views/administratorPage");
        return mnv;
    }

    @GetMapping("/user/attendancePage")
    public ModelAndView attendancePage(Model model) {
        ModelAndView mnv = new ModelAndView("views/attendancePage");
        return mnv;
    }

    @GetMapping("/parsonalDataPage")
    public ModelAndView parsonalDataPage(Model model) {
        ModelAndView mnv = new ModelAndView("views/parsonalDataPage");
        return mnv;
    }

}
