package com.tiendamas.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index(Model model, Authentication authentication) {
        if (tieneRol(authentication, "ROLE_VENDEDOR")) {
            return "redirect:/pos";
        }
        if (tieneRol(authentication, "ROLE_CLIENTE")) {
            return "redirect:/tienda";
        }
        model.addAttribute("titulo", "Inicio");
        return "home";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    private boolean tieneRol(Authentication authentication, String rol) {
        if (authentication == null) return false;
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if (authority.getAuthority().equals(rol)) {
                return true;
            }
        }
        return false;
    }
}
