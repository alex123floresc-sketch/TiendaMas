package episs.unaj.com.crudpersona.controller;

import episs.unaj.com.crudpersona.entity.Sueldo;
import episs.unaj.com.crudpersona.entity.Usuario;
import episs.unaj.com.crudpersona.service.SueldoService;
import episs.unaj.com.crudpersona.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/sueldos")
public class SueldoController {

    @Autowired
    private SueldoService sueldoService;

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("sueldos", sueldoService.obtenerTodos());
        model.addAttribute("titulo", "Sueldos");
        return "sueldos/index";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioCrear(Model model) {
        model.addAttribute("sueldo", new Sueldo());
        model.addAttribute("empleados", usuarioService.obtenerEmpleados());
        model.addAttribute("titulo", "Nuevo Sueldo");
        return "sueldos/form";
    }

    @PostMapping
    public String guardar(@RequestParam Long usuarioId, @ModelAttribute Sueldo sueldo) {
        Usuario empleado = usuarioService.obtenerEmpleados().stream()
                .filter(u -> u.getId().equals(usuarioId))
                .findFirst()
                .orElse(null);
        if (empleado == null) {
            return "redirect:/sueldos/nuevo?error=empleadoInvalido";
        }
        sueldo.setUsuario(empleado);
        sueldoService.guardar(sueldo);
        return "redirect:/sueldos";
    }

    @PostMapping("/{id}/pagar")
    public String pagar(@PathVariable Long id) {
        sueldoService.marcarPagado(id);
        return "redirect:/sueldos";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id) {
        sueldoService.eliminar(id);
        return "redirect:/sueldos";
    }
}
