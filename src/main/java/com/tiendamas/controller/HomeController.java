package com.tiendamas.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    // El panel con menú lateral (home.html) es solo el "inicio" del ADMIN.
    // Si un vendedor o cliente llega aquí (p. ej. escribiendo la URL a mano),
    // lo mandamos a su propia pantalla en vez de mostrarle el menú administrativo.
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
        return "login"; // Esto buscará el archivo login.html en templates
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
