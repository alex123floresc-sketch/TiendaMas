package episs.unaj.com.crudpersona.controller;

import episs.unaj.com.crudpersona.entity.Categoria;
import episs.unaj.com.crudpersona.entity.Producto;
import episs.unaj.com.crudpersona.service.CategoriaService;
import episs.unaj.com.crudpersona.service.ProductoService;
import episs.unaj.com.crudpersona.util.ImagenStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private CategoriaService categoriaService;

    @Autowired
    private ImagenStorage imagenStorage;

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
    public String guardar(@ModelAttribute Producto producto, @RequestParam Long categoriaId,
                          @RequestParam(value = "imagen", required = false) MultipartFile imagen) {
        Categoria categoria = categoriaService.obtenerPorId(categoriaId);
        producto.setCategoria(categoria);
        if (imagen != null && !imagen.isEmpty()) {
            producto.setImagenUrl(imagenStorage.guardar(imagen));
        }
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
                             @RequestParam Long categoriaId,
                             @RequestParam(value = "imagen", required = false) MultipartFile imagen) {
        Categoria categoria = categoriaService.obtenerPorId(categoriaId);
        producto.setCategoria(categoria);
        if (imagen != null && !imagen.isEmpty()) {
            Producto existente = productoService.obtenerPorId(id);
            imagenStorage.eliminar(existente.getImagenUrl());
            producto.setImagenUrl(imagenStorage.guardar(imagen));
        } else {
            producto.setImagenUrl(productoService.obtenerPorId(id).getImagenUrl());
        }
        productoService.actualizar(id, producto);
        return "redirect:/productos";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id) {
        Producto producto = productoService.obtenerPorId(id);
        try {
            productoService.eliminar(id);
        } catch (DataIntegrityViolationException e) {
            return "redirect:/productos?error=conRelaciones";
        }
        imagenStorage.eliminar(producto.getImagenUrl());
        return "redirect:/productos";
    }
}
