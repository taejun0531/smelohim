package com.site.elohim.controller;

import com.site.elohim.model.Role;
import com.site.elohim.service.MainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MainController {

    private final MainService mainService;

    @GetMapping("/")
    public String home(@AuthenticationPrincipal UserDetails user, Model model) {

        // user.getUsername() => userId를 반환
        String username = mainService.getUsernameByUserId(user.getUsername());
        model.addAttribute("username", username);

        if (hasRole(user, Role.ADMIN.getValue()))
            return "mainPage_admin";
        if (hasRole(user, Role.USER.getValue()))
            return "mainPage_user";
        if (hasRole(user, Role.AWAIT.getValue()))
            return "mainPage_await";

        // 정의되지 않은 ROLE인 경우: 로그 남기고 로그인 페이지로
        log.warn("Unknown roles for user {}: {}", username, user.getAuthorities());
        return "redirect:/loginPage";
    }

    private boolean hasRole(UserDetails user, String roleValue) {
        return user.getAuthorities().stream()
                .anyMatch(a -> roleValue.equals(a.getAuthority()));
    }
}
