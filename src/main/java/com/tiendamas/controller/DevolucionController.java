package com.tiendamas.controller;

import com.tiendamas.entity.EstadoSolicitudDevolucion;
import com.tiendamas.service.SolicitudDevolucionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/devoluciones")
public class DevolucionController {

    @Autowired
    private SolicitudDevolucionService solicitudDevolucionService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("solicitudes", solicitudDevolucionService.obtenerTodas());
        model.addAttribute("titulo", "Cambios y devoluciones");
        return "devoluciones/index";
    }

    @PostMapping("/{id}/resolver")
    public String resolver(@PathVariable Long id, @RequestParam EstadoSolicitudDevolucion estado,
                            @RequestParam(required = false) String respuesta,
                            RedirectAttributes redirectAttributes) {
        try {
            solicitudDevolucionService.resolver(id, estado, respuesta);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/devoluciones";
    }
}
