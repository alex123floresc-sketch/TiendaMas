package com.tiendamas.controller;

import com.tiendamas.dto.ItemVenta;
import com.tiendamas.entity.CanalVenta;
import com.tiendamas.entity.MetodoPago;
import com.tiendamas.entity.Pedido;
import com.tiendamas.entity.Producto;
import com.tiendamas.service.PedidoService;
import com.tiendamas.service.PersonaService;
import com.tiendamas.service.ProductoService;
import com.tiendamas.web.Carrito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/pos")
public class PosController {

    @Autowired
    private Carrito carrito;

    @Autowired
    private ProductoService productoService;

    @Autowired
    private PersonaService personaService;

    @Autowired
    private PedidoService pedidoService;

    @GetMapping
    public String pos(Model model) {
        model.addAttribute("titulo", "Punto de Venta");
        model.addAttribute("carrito", carrito);
        model.addAttribute("personas", personaService.obtenerTodas());
        model.addAttribute("productos", productoService.obtenerTodos());
        return "pos/index";
    }

    @PostMapping("/carrito/agregar-por-codigo")
    public String agregarPorCodigo(@RequestParam String codigoBarras,
                                    @RequestParam(required = false, defaultValue = "1") Integer cantidad) {
        Producto producto = productoService.obtenerPorCodigoBarras(codigoBarras);
        if (producto == null) {
            return "redirect:/pos?error=codigoNoEncontrado";
        }
        carrito.agregar(producto, cantidad != null && cantidad > 0 ? cantidad : 1);
        return "redirect:/pos";
    }

    @PostMapping("/carrito/agregar")
    public String agregarManual(@RequestParam Long productoId,
                                 @RequestParam(required = false, defaultValue = "1") Integer cantidad) {
        Producto producto = productoService.obtenerPorId(productoId);
        if (producto == null) {
            return "redirect:/pos?error=productoNoEncontrado";
        }
        carrito.agregar(producto, cantidad != null && cantidad > 0 ? cantidad : 1);
        return "redirect:/pos";
    }

    @PostMapping("/carrito/cantidad")
    public String actualizarCantidad(@RequestParam Long productoId, @RequestParam Integer cantidad) {
        carrito.actualizarCantidad(productoId, cantidad);
        return "redirect:/pos";
    }

    @PostMapping("/carrito/quitar/{productoId}")
    public String quitar(@PathVariable Long productoId) {
        carrito.quitar(productoId);
        return "redirect:/pos";
    }

    @PostMapping("/carrito/vaciar")
    public String vaciar() {
        carrito.vaciar();
        return "redirect:/pos";
    }

    @PostMapping("/checkout")
    public String checkout(@RequestParam Long personaId, @RequestParam MetodoPago metodoPago, Principal principal) {
        if (carrito.isEmpty()) {
            return "redirect:/pos?error=carritoVacio";
        }
        List<ItemVenta> items = carrito.getItems().stream()
                .map(i -> new ItemVenta(i.getProductoId(), i.getCantidad()))
                .collect(Collectors.toList());

        Pedido pedido;
        try {
            pedido = pedidoService.crearVenta(personaId, items, CanalVenta.TIENDA_FISICA, metodoPago, principal.getName());
        } catch (IllegalArgumentException e) {
            return "redirect:/pos?error=clienteInvalido";
        }
        carrito.vaciar();
        return "redirect:/pedidos/" + pedido.getId();
    }

    @GetMapping("/ventas")
    public String ventas(Model model) {
        model.addAttribute("titulo", "Mis Ventas");
        model.addAttribute("pedidos", pedidoService.obtenerPorCanal(CanalVenta.TIENDA_FISICA));
        return "pos/ventas";
    }
}
