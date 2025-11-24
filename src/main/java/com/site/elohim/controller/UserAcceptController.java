package com.site.elohim.controller;

import com.site.elohim.model.Members;
import com.site.elohim.model.Users;
import com.site.elohim.service.UserAcceptService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;

@Controller
public class UserAcceptController {

    private final UserAcceptService service;

    public UserAcceptController(UserAcceptService service) {
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

    @PostMapping("/admin/isEmptyLeaderId")
    @ResponseBody
    public boolean isEmptyLeaderId(@RequestBody Map<String, String> data) {
        Long leaderId = Long.parseLong(data.get("leaderId"));
        return service.existsByLeaderId(leaderId); // 이미 있는 경우 false, 비어 있는 경우 true
    }

    @PostMapping("/admin/accept_user")
    @ResponseBody
    public boolean acceptUser(@RequestBody Map<String, String> data) {
        String id = data.get("id");
        String userRole = data.get("userRole");
        String leaderId = data.get("leaderId");

        return service.updateUser(id, userRole, leaderId);
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
