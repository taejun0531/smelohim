package com.site.elohim.controller;

import com.site.elohim.model.Members;
import com.site.elohim.model.Users;
import com.site.elohim.service.userAcceptService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class userAcceptController {

    private final userAcceptService service;

    public userAcceptController(userAcceptService service) {
        this.service = service;
    }

    @GetMapping("/admin/userAcceptPage")
    public ModelAndView userAcceptPage(Model model) {
        ModelAndView mnv = new ModelAndView("/userAcceptPage");

        List<Users> awaitList = service.findAllUserByRole("AWAIT");
        List<Users> cellLeaderList = service.findAllUserByRole("USER");
        List<Users> adminList = service.findAllUserByRole("ADMIN");

        mnv.addObject("awaitList", awaitList);
        mnv.addObject("cellLeaderList", cellLeaderList);
        mnv.addObject("adminList", adminList);

        return mnv;
    }

    @PostMapping("/admin/accept_user")
    @ResponseBody
    public boolean acceptUser(@RequestBody Map<String, String> data) {
        String userKey = data.get("chage_userKey");
        String userRole = data.get("chage_userRole");

        return service.updateUser(userKey, userRole);
    }

    @PostMapping("/admin/getCellLeaderInfo")
    @ResponseBody
    public List<Members> getCellLeaderInfo() {

        return service.getMembersCellLeader();
    }

    @PostMapping("/admin/delete_user")
    @ResponseBody
    public boolean deleteUser(@RequestBody Map<String, String> data) {
        Long deleteId = Long.parseLong(data.get("deleteId"));

        return service.deleteUser(deleteId);
    }

}
