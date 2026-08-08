package com.tiendamas.controller;

import com.tiendamas.dto.PedidoForm;
import com.tiendamas.entity.EstadoPedido;
import com.tiendamas.entity.Pedido;
import com.tiendamas.entity.RolUsuario;
import com.tiendamas.entity.Usuario;
import com.tiendamas.entity.VarianteProducto;
import com.tiendamas.service.EmailService;
import com.tiendamas.service.PedidoService;
import com.tiendamas.service.PersonaService;
import com.tiendamas.service.ProductoService;
import com.tiendamas.service.UsuarioService;
import com.tiendamas.service.impl.ComprobantePdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

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

    @Autowired
    private ComprobantePdfService comprobantePdfService;

    @Autowired
    private EmailService emailService;

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
        List<VarianteProducto> variantes = productoService.obtenerTodos().stream()
                .flatMap(p -> p.getVariantes().stream())
                .toList();
        model.addAttribute("variantes", variantes);
        model.addAttribute("titulo", "Nueva Venta");
        return "pedidos/form";
    }

    @PostMapping
    public String guardar(@ModelAttribute PedidoForm pedidoForm, Principal principal) {
        try {
            pedidoService.crearPedido(pedidoForm, principal.getName());
        } catch (IllegalArgumentException e) {
            return "redirect:/pedidos/nuevo?error=datosInvalidos";
        }
        return "redirect:/pedidos";
    }

    @GetMapping("/{id}")
    public String ver(@PathVariable Long id, Model model, Principal principal) {
        Pedido pedido = pedidoService.obtenerPorId(id);
        if (pedido == null) {
            return "redirect:/pedidos";
        }

        Usuario usuario = verificarAcceso(pedido, principal);

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

    @GetMapping("/{id}/descargar")
    public ResponseEntity<byte[]> descargar(@PathVariable Long id, Principal principal) {
        Pedido pedido = pedidoService.obtenerPorId(id);
        if (pedido == null) {
            return ResponseEntity.notFound().build();
        }
        verificarAcceso(pedido, principal);

        byte[] pdf = comprobantePdfService.generarPdf(pedido);
        String nombreArchivo = "Boleta-" + pedido.getNumeroCompleto() + ".pdf";
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(nombreArchivo).build().toString())
                .body(pdf);
    }

    @PostMapping("/{id}/enviar-correo")
    public String enviarPorCorreo(@PathVariable Long id, Principal principal) {
        Pedido pedido = pedidoService.obtenerPorId(id);
        if (pedido == null) {
            return "redirect:/pedidos";
        }
        verificarAcceso(pedido, principal);

        byte[] pdf = comprobantePdfService.generarPdf(pedido);
        boolean enviado = emailService.enviarComprobantePorCorreo(pedido, pdf);
        return "redirect:/pedidos/" + id + "?correo=" + (enviado ? "enviado" : "error");
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

    /** Valida que el usuario autenticado pueda ver este pedido; los CLIENTE solo pueden ver los propios. */
    private Usuario verificarAcceso(Pedido pedido, Principal principal) {
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
        return usuario;
    }
}
