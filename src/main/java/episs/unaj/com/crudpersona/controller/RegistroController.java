package episs.unaj.com.crudpersona.controller;

import episs.unaj.com.crudpersona.dto.RegistroForm;
import episs.unaj.com.crudpersona.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class RegistroController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/registro")
    public String mostrarFormulario(Model model) {
        model.addAttribute("registroForm", new RegistroForm());
        return "registro";
    }

    @PostMapping("/registro")
    public String registrar(@ModelAttribute RegistroForm registroForm, Model model) {
        if (registroForm.getUsername() == null || registroForm.getUsername().isBlank()
                || usuarioService.existeUsername(registroForm.getUsername())) {
            model.addAttribute("error", "Ese nombre de usuario ya está en uso o no es válido.");
            return "registro";
        }
        usuarioService.registrarCliente(registroForm);
        return "redirect:/login?registrado=true";
    }
}
