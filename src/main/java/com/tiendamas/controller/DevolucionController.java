package com.tiendamas.controller;

import com.tiendamas.entity.EstadoSolicitudDevolucion;
import com.tiendamas.entity.SolicitudDevolucion;
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

import java.util.List;

@Controller
@RequestMapping("/devoluciones")
public class DevolucionController {

    @Autowired
    private SolicitudDevolucionService solicitudDevolucionService;

    @GetMapping
    public String listar(Model model) {
        List<SolicitudDevolucion> solicitudes = solicitudDevolucionService.obtenerTodas();
        model.addAttribute("solicitudes", solicitudes);
        model.addAttribute("solicitudesPendientes", solicitudes.stream()
                .filter(s -> s.getEstado() == EstadoSolicitudDevolucion.PENDIENTE)
                .toList());
        model.addAttribute("totalPendientes", contar(solicitudes, EstadoSolicitudDevolucion.PENDIENTE));
        model.addAttribute("totalResueltas", contar(solicitudes, EstadoSolicitudDevolucion.APROBADA)
                + contar(solicitudes, EstadoSolicitudDevolucion.COMPLETADA));
        model.addAttribute("totalRechazadas", contar(solicitudes, EstadoSolicitudDevolucion.RECHAZADA));
        model.addAttribute("titulo", "Cambios y devoluciones");
        return "devoluciones/index";
    }

    private long contar(List<SolicitudDevolucion> solicitudes, EstadoSolicitudDevolucion estado) {
        return solicitudes.stream().filter(s -> s.getEstado() == estado).count();
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
