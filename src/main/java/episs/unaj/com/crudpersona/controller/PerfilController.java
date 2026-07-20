package episs.unaj.com.crudpersona.controller;

import episs.unaj.com.crudpersona.dto.CambioPasswordForm;
import episs.unaj.com.crudpersona.entity.RolUsuario;
import episs.unaj.com.crudpersona.entity.Usuario;
import episs.unaj.com.crudpersona.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

/**
 * Perfil de cuenta para ADMIN y VENDEDOR (nombre/apellido + cambio de contraseña).
 * Un CLIENTE tiene un perfil más completo en /tienda/perfil (datos de facturación),
 * así que aquí simplemente lo redirigimos allá.
 */
@Controller
@RequestMapping("/perfil")
public class PerfilController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public String perfil(Model model, Principal principal) {
        Usuario usuario = usuarioService.buscarPorUsername(principal.getName())
                .orElseThrow(() -> new IllegalStateException("Usuario autenticado no encontrado"));

        if (usuario.getRol() == RolUsuario.CLIENTE) {
            return "redirect:/tienda/perfil";
        }

        model.addAttribute("usuario", usuario);
        model.addAttribute("cambioPasswordForm", new CambioPasswordForm());
        model.addAttribute("titulo", "Mi Perfil");
        return usuario.getRol() == RolUsuario.ADMIN ? "perfil/admin" : "perfil/vendedor";
    }

    @PostMapping("/datos")
    public String actualizarDatos(@RequestParam String nombre, @RequestParam String apellido, Principal principal) {
        usuarioService.actualizarPerfil(principal.getName(), nombre, apellido);
        return "redirect:/perfil?actualizado=true";
    }

    @PostMapping("/password")
    public String cambiarPassword(@ModelAttribute CambioPasswordForm form, Principal principal) {
        // Un CLIENTE llega aquí desde /tienda/perfil, así que lo devolvemos ahí
        // (si volviera a /perfil, este mismo controlador lo redirigiría de nuevo,
        // perdiendo el mensaje de confirmación en el camino).
        Usuario usuario = usuarioService.buscarPorUsername(principal.getName()).orElse(null);
        String volverA = (usuario != null && usuario.getRol() == RolUsuario.CLIENTE) ? "/tienda/perfil" : "/perfil";

        if (form.getPasswordNueva() == null || !form.getPasswordNueva().equals(form.getConfirmarPassword())) {
            return "redirect:" + volverA + "?error=noCoincide";
        }
        boolean actualizado = usuarioService.cambiarPassword(principal.getName(), form.getPasswordActual(), form.getPasswordNueva());
        return actualizado
                ? "redirect:" + volverA + "?passwordActualizada=true"
                : "redirect:" + volverA + "?error=passwordIncorrecta";
    }
}
