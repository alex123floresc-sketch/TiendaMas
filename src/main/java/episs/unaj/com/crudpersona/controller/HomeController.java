package episs.unaj.com.crudpersona.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("titulo", "Inicio");
        return "home";
    }

    @GetMapping("/login")
    public String login() {
        return "login"; // Esto buscará el archivo login.html en templates
    }
}