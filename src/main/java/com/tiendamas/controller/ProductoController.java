package com.tiendamas.controller;

import com.tiendamas.dto.ProductoForm;
import com.tiendamas.dto.VarianteForm;
import com.tiendamas.entity.Categoria;
import com.tiendamas.entity.Producto;
import com.tiendamas.entity.Talla;
import com.tiendamas.entity.VarianteProducto;
import com.tiendamas.service.CategoriaService;
import com.tiendamas.service.ProductoService;
import com.tiendamas.util.ImagenStorage;
import jakarta.validation.Valid;
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
        ProductoForm form = new ProductoForm();
        model.addAttribute("productoForm", form);
        model.addAttribute("categorias", categoriaService.obtenerTodas());
        model.addAttribute("tallas", Talla.values());
        model.addAttribute("titulo", "Nuevo Producto");
        return "productos/form";
    }

    @PostMapping
    public String guardar(@Valid @ModelAttribute("productoForm") ProductoForm form,
                          @RequestParam(value = "imagen", required = false) MultipartFile imagen) {
        Categoria categoria = categoriaService.obtenerPorId(form.getCategoriaId());
        String imagenUrl = (imagen != null && !imagen.isEmpty()) ? imagenStorage.guardar(imagen) : null;
        productoService.guardarConVariantes(form, categoria, imagenUrl);
        return "redirect:/productos";
    }

    @GetMapping("/{id}/editar")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        Producto producto = productoService.obtenerPorId(id);
        if (producto == null) {
            return "redirect:/productos";
        }
        ProductoForm form = new ProductoForm();
        form.setId(producto.getId());
        form.setNombre(producto.getNombre());
        form.setDescripcion(producto.getDescripcion());
        form.setPrecio(producto.getPrecio());
        form.setMarca(producto.getMarca());
        form.setCategoriaId(producto.getCategoria() != null ? producto.getCategoria().getId() : null);
        for (VarianteProducto v : producto.getVariantes()) {
            VarianteForm vf = new VarianteForm();
            vf.setId(v.getId());
            vf.setTalla(v.getTalla());
            vf.setColor(v.getColor());
            vf.setColorHex(v.getColorHex());
            vf.setStock(v.getStock());
            vf.setCodigoBarras(v.getCodigoBarras());
            form.getVariantes().add(vf);
        }

        model.addAttribute("productoForm", form);
        model.addAttribute("producto", producto);
        model.addAttribute("categorias", categoriaService.obtenerTodas());
        model.addAttribute("tallas", Talla.values());
        model.addAttribute("titulo", "Editar Producto");
        return "productos/form";
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long id,
                             @Valid @ModelAttribute("productoForm") ProductoForm form,
                             @RequestParam(value = "imagen", required = false) MultipartFile imagen) {
        Categoria categoria = categoriaService.obtenerPorId(form.getCategoriaId());
        Producto existente = productoService.obtenerPorId(id);
        String imagenUrl;
        if (imagen != null && !imagen.isEmpty()) {
            imagenStorage.eliminar(existente.getImagenUrl());
            imagenUrl = imagenStorage.guardar(imagen);
        } else {
            imagenUrl = existente.getImagenUrl();
        }
        try {
            productoService.actualizarConVariantes(id, form, categoria, imagenUrl);
        } catch (DataIntegrityViolationException e) {
            return "redirect:/productos/" + id + "/editar?error=varianteConVentas";
        }
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
