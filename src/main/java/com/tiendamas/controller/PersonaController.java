package com.tiendamas.controller;

import com.tiendamas.entity.Pedido;
import com.tiendamas.entity.Persona;
import com.tiendamas.service.PedidoService;
import com.tiendamas.service.PersonaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/personas")
public class PersonaController {

    @Autowired
    private PersonaService personaService;

    @Autowired
    private PedidoService pedidoService;

    @GetMapping
    public String listar(Model model) {
        List<Persona> lista = personaService.obtenerTodas();
        if (lista == null) {
            lista = new java.util.ArrayList<>();
        }

        Map<Long, Double> totalPorPersona = new HashMap<>();
        for (Pedido pedido : pedidoService.obtenerTodos()) {
            if (pedido.getPersona() == null) continue;
            double total = pedido.getTotal() != null ? pedido.getTotal() : 0.0;
            totalPorPersona.merge(pedido.getPersona().getId(), total, Double::sum);
        }

        model.addAttribute("personas", lista);
        model.addAttribute("totalPorPersona", totalPorPersona);
        model.addAttribute("titulo", "Clientes");
        return "personas/index";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioCrear(Model model) {
        model.addAttribute("persona", new Persona());
        model.addAttribute("titulo", "Nuevo Cliente");
        return "personas/form";
    }

    @PostMapping
    public String guardar(Persona persona) {
        personaService.guardar(persona);
        return "redirect:/personas";
    }

    @GetMapping("/{id}")
    public String ver(@PathVariable("id") Long id, Model model) {
        Persona persona = personaService.obtenerPorId(id);
        if (persona == null) {
            return "redirect:/personas";
        }

        List<Pedido> pedidos = pedidoService.obtenerPorPersona(id);
        double totalGastado = pedidos.stream().mapToDouble(p -> p.getTotal() != null ? p.getTotal() : 0.0).sum();

        model.addAttribute("persona", persona);
        model.addAttribute("titulo", "Detalle de Cliente");
        model.addAttribute("cantidadPedidos", pedidos.size());
        model.addAttribute("totalGastado", totalGastado);
        model.addAttribute("ultimaCompra", pedidos.isEmpty() ? null : pedidos.get(0));
        return "personas/ver";
    }

    @GetMapping("/{id}/editar")
    public String mostrarFormularioEditar(@PathVariable("id") Long id, Model model) {
        Persona persona = personaService.obtenerPorId(id);
        if (persona != null) {
            model.addAttribute("persona", persona);
            model.addAttribute("titulo", "Editar Cliente");
            return "personas/form";
        }
        return "redirect:/personas";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable("id") Long id) {
        try {
            personaService.eliminar(id);
        } catch (DataIntegrityViolationException e) {
            return "redirect:/personas?error=conRelaciones";
        }
        return "redirect:/personas";
    }
}
