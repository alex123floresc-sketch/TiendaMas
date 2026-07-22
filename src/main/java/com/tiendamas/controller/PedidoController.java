package com.tiendamas.controller;

import com.tiendamas.dto.PedidoForm;
import com.tiendamas.entity.EstadoPedido;
import com.tiendamas.entity.Pedido;
import com.tiendamas.entity.RolUsuario;
import com.tiendamas.entity.Usuario;
import com.tiendamas.service.PedidoService;
import com.tiendamas.service.PersonaService;
import com.tiendamas.service.ProductoService;
import com.tiendamas.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequestMapping("/pedidos")
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private PersonaService personaService;

    @Autowired
    private ProductoService productoService;

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("pedidos", pedidoService.obtenerTodos());
        model.addAttribute("titulo", "Ventas");
        return "pedidos/index";
    }

    @GetMapping("/nuevo")
    public String mostrarFormulario(Model model) {
        model.addAttribute("pedidoForm", new PedidoForm());
        model.addAttribute("personas", personaService.obtenerTodas());
        model.addAttribute("productos", productoService.obtenerTodos());
        model.addAttribute("titulo", "Nueva Venta");
        return "pedidos/form";
    }

    @PostMapping
    public String guardar(@ModelAttribute PedidoForm pedidoForm, Principal principal) {
        pedidoService.crearPedido(pedidoForm, principal.getName());
        return "redirect:/pedidos";
    }

    @GetMapping("/{id}")
    public String ver(@PathVariable Long id, Model model, Principal principal) {
        Pedido pedido = pedidoService.obtenerPorId(id);
        if (pedido == null) {
            return "redirect:/pedidos";
        }

        Usuario usuario = usuarioService.buscarPorUsername(principal.getName()).orElse(null);
        boolean esCliente = usuario != null && usuario.getRol() == RolUsuario.CLIENTE;
        if (esCliente) {
            Long personaDelUsuario = usuario.getPersona() != null ? usuario.getPersona().getId() : null;
            boolean esDueno = personaDelUsuario != null && pedido.getPersona() != null
                    && pedido.getPersona().getId().equals(personaDelUsuario);
            if (!esDueno) {
                throw new AccessDeniedException("No tienes acceso a este comprobante");
            }
        }

        String volverUrl = "/pedidos";
        if (usuario != null) {
            if (usuario.getRol() == RolUsuario.CLIENTE) {
                volverUrl = "/tienda/pedidos";
            } else if (usuario.getRol() == RolUsuario.VENDEDOR) {
                volverUrl = "/pos/ventas";
            }
        }

        model.addAttribute("pedido", pedido);
        model.addAttribute("titulo", "Detalle de Pedido");
        model.addAttribute("volverUrl", volverUrl);
        return "pedidos/ver";
    }

    @PostMapping("/{id}/estado")
    public String actualizarEstado(@PathVariable Long id, @RequestParam EstadoPedido estado) {
        pedidoService.actualizarEstado(id, estado);
        return "redirect:/pedidos";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id) {
        try {
            pedidoService.eliminar(id);
        } catch (DataIntegrityViolationException e) {
            return "redirect:/pedidos?error=conRelaciones";
        }
        return "redirect:/pedidos";
    }
}
