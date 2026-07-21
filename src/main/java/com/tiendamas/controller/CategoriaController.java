package com.tiendamas.controller;

import com.tiendamas.entity.Categoria;
import com.tiendamas.service.CategoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
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
private CategoriaService categoriaService;

// 1. LISTAR
@GetMapping
public String listar(Model model) {
List<Categoria> lista = categoriaService.obtenerTodas();
if (lista == null) {
lista = new java.util.ArrayList<>();
}
model.addAttribute("categorias", lista);
model.addAttribute("titulo", "Categorías");
return "categorias/index";
}

// 2. FORMULARIO NUEVO
@GetMapping("/nuevo")
public String mostrarFormularioCrear(Model model) {
model.addAttribute("categoria", new Categoria());
model.addAttribute("titulo", "Nueva Categoría");
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
model.addAttribute("titulo", "Editar Categoría");
return "categorias/form";
}
return "redirect:/categorias";
}

// 5. ELIMINAR (POST para evitar borrados accidentales vía GET/CSRF)
@PostMapping("/{id}/eliminar")
public String eliminar(@PathVariable("id") Long id) {
try {
categoriaService.eliminar(id);
} catch (DataIntegrityViolationException e) {
return "redirect:/categorias?error=conRelaciones";
}
return "redirect:/categorias";
}
}
