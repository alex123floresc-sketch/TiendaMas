package episs.unaj.com.crudpersona.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index() {
        // Redirige automáticamente al listado de personas nada más entrar
        return "redirect:/personas";
    }
    // Agrega este método a tu controlador existente
    @GetMapping("/login")
    public String login() {
        return "login"; // Esto buscará el archivo login.html en templates
    }
}