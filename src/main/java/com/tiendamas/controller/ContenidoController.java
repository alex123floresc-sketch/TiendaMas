package com.tiendamas.controller;

import com.tiendamas.service.ContenidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
@RequestMapping("/contenido")
public class ContenidoController {

    @Autowired
    private ContenidoService contenidoService;

    @GetMapping
    public String editar(Model model) {
        model.addAttribute("contenido", contenidoService.obtenerTodo());
        model.addAttribute("titulo", "Contenido del sitio");
        return "contenido/form";
    }

    @PostMapping
    public String guardar(@RequestParam Map<String, String> valores) {
        contenidoService.guardar(valores);
        return "redirect:/contenido?guardado=true";
    }
}
