package episs.unaj.com.crudpersona.controller;

import episs.unaj.com.crudpersona.entity.Gasto;
import episs.unaj.com.crudpersona.service.GastoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/gastos")
public class GastoController {

    @Autowired
    private GastoService gastoService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("gastos", gastoService.obtenerTodos());
        model.addAttribute("titulo", "Gastos");
        return "gastos/index";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioCrear(Model model) {
        model.addAttribute("gasto", new Gasto());
        model.addAttribute("titulo", "Nuevo Gasto");
        return "gastos/form";
    }

    @PostMapping
    public String guardar(Gasto gasto) {
        gastoService.guardar(gasto);
        return "redirect:/gastos";
    }

    @GetMapping("/{id}/editar")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        Gasto gasto = gastoService.obtenerPorId(id);
        if (gasto == null) {
            return "redirect:/gastos";
        }
        model.addAttribute("gasto", gasto);
        model.addAttribute("titulo", "Editar Gasto");
        return "gastos/form";
    }

    @PostMapping("/{id}/duplicar")
    public String duplicar(@PathVariable Long id) {
        gastoService.duplicar(id);
        return "redirect:/gastos";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id) {
        gastoService.eliminar(id);
        return "redirect:/gastos";
    }
}
