package episs.unaj.com.crudpersona.controller;

import episs.unaj.com.crudpersona.entity.Categoria;
import episs.unaj.com.crudpersona.service.CategoriaService; // Cambia esto según tu estructura de servicios
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/categorias")
public class CategoriaController {

    @Autowired
    private CategoriaService categoriaService; // Ajusta al nombre de tu servicio

    // 1. LISTAR
    @GetMapping
    public String listar(Model model) {
        List<Categoria> lista = categoriaService.obtenerTodas(); // Ajusta al nombre de tu método
        if (lista == null) {
            lista = new java.util.ArrayList<>();
        }
        model.addAttribute("categorias", lista); // ¡Clave!: Debe llamarse "categorias" en plural
        return "categorias/index";
    }

    // 2. FORMULARIO NUEVO
    @GetMapping("/nuevo")
    public String mostrarFormularioCrear(Model model) {
        model.addAttribute("categoria", new Categoria()); // ¡Clave!: Debe llamarse "categoria" en singular
        return "categorias/form";
    }

    // 3. GUARDAR
    @PostMapping
    public String guardar(Categoria categoria) {
        categoriaService.guardar(categoria);
        return "redirect:/categorias";
    }

    // 4. FORMULARIO EDITAR
    @GetMapping("/{id}/editar")
    public String mostrarFormularioEditar(@PathVariable("id") Long id, Model model) {
        Categoria categoria = categoriaService.obtenerPorId(id);
        if (categoria != null) {
            model.addAttribute("categoria", categoria);
            return "categorias/form";
        }
        return "redirect:/categorias";
    }

    // 5. ELIMINAR
    @GetMapping("/{id}/eliminar")
    public String eliminar(@PathVariable("id") Long id) {
        categoriaService.eliminar(id);
        return "redirect:/categorias";
    }
}