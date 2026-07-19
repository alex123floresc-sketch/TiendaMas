package episs.unaj.com.crudpersona.controller;

import episs.unaj.com.crudpersona.entity.Categoria;
import episs.unaj.com.crudpersona.entity.Producto;
import episs.unaj.com.crudpersona.service.CategoriaService;
import episs.unaj.com.crudpersona.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private CategoriaService categoriaService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("productos", productoService.obtenerTodos());
        model.addAttribute("titulo", "Productos");
        return "productos/index";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioCrear(Model model) {
        model.addAttribute("producto", new Producto());
        model.addAttribute("categorias", categoriaService.obtenerTodas());
        model.addAttribute("titulo", "Nuevo Producto");
        return "productos/form";
    }

    @PostMapping
    public String guardar(@ModelAttribute Producto producto, @RequestParam Long categoriaId) {
        Categoria categoria = categoriaService.obtenerPorId(categoriaId);
        producto.setCategoria(categoria);
        productoService.guardar(producto);
        return "redirect:/productos";
    }

    @GetMapping("/{id}/editar")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        model.addAttribute("producto", productoService.obtenerPorId(id));
        model.addAttribute("categorias", categoriaService.obtenerTodas());
        model.addAttribute("titulo", "Editar Producto");
        return "productos/form";
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long id,
                             @ModelAttribute Producto producto,
                             @RequestParam Long categoriaId) {
        Categoria categoria = categoriaService.obtenerPorId(categoriaId);
        producto.setCategoria(categoria);
        productoService.actualizar(id, producto);
        return "redirect:/productos";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id) {
        try {
            productoService.eliminar(id);
        } catch (DataIntegrityViolationException e) {
            return "redirect:/productos?error=conRelaciones";
        }
        return "redirect:/productos";
    }
}
