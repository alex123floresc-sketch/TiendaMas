package com.tiendamas.controller;

import com.tiendamas.dto.CambioPasswordForm;
import com.tiendamas.dto.ItemVenta;
import com.tiendamas.entity.CanalVenta;
import com.tiendamas.entity.MetodoPago;
import com.tiendamas.entity.Pedido;
import com.tiendamas.entity.Persona;
import com.tiendamas.entity.Producto;
import com.tiendamas.entity.Usuario;
import com.tiendamas.service.CategoriaService;
import com.tiendamas.service.PedidoService;
import com.tiendamas.service.PersonaService;
import com.tiendamas.service.ProductoService;
import com.tiendamas.service.UsuarioService;
import com.tiendamas.web.Carrito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/tienda")
public class TiendaController {

    @Autowired
    private Carrito carrito;

    @Autowired
    private ProductoService productoService;

    @Autowired
    private CategoriaService categoriaService;

    @Autowired
    private PersonaService personaService;

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public String index(@RequestParam(required = false) Long categoriaId, Model model) {
        List<Producto> productos = productoService.obtenerTodos();
        if (categoriaId != null) {
            productos = productos.stream()
                    .filter(p -> p.getCategoria() != null && categoriaId.equals(p.getCategoria().getId()))
                    .collect(Collectors.toList());
        }
        model.addAttribute("productos", productos);
        model.addAttribute("categorias", categoriaService.obtenerTodas());
        model.addAttribute("categoriaSeleccionada", categoriaId);
        model.addAttribute("titulo", "Tienda");
        return "tienda/index";
    }

    @GetMapping("/productos/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        Producto producto = productoService.obtenerPorId(id);
        if (producto == null) {
            return "redirect:/tienda";
        }
        model.addAttribute("producto", producto);
        model.addAttribute("relacionados", productoService.obtenerRelacionados(producto));
        model.addAttribute("titulo", producto.getNombre());
        return "tienda/detalle";
    }

    @PostMapping("/carrito/agregar")
    public String agregar(@RequestParam Long productoId, @RequestParam(required = false, defaultValue = "1") Integer cantidad) {
        Producto producto = productoService.obtenerPorId(productoId);
        if (producto != null) {
            carrito.agregar(producto, cantidad != null && cantidad > 0 ? cantidad : 1);
        }
        return "redirect:/tienda/carrito";
    }

    @GetMapping("/carrito")
    public String verCarrito(Model model) {
        List<Long> productosEnCarrito = carrito.getItems().stream()
                .map(item -> item.getProductoId())
                .toList();
        List<Producto> recomendados = pedidoService.obtenerMasVendidos(8).stream()
                .filter(p -> !productosEnCarrito.contains(p.getId()))
                .limit(4)
                .toList();

        model.addAttribute("carrito", carrito);
        model.addAttribute("recomendados", recomendados);
        model.addAttribute("titulo", "Mi Carrito");
        return "tienda/carrito";
    }

    @PostMapping("/carrito/cantidad")
    public String actualizarCantidad(@RequestParam Long productoId, @RequestParam Integer cantidad) {
        carrito.actualizarCantidad(productoId, cantidad);
        return "redirect:/tienda/carrito";
    }

    @PostMapping("/carrito/quitar/{productoId}")
    public String quitar(@PathVariable Long productoId) {
        carrito.quitar(productoId);
        return "redirect:/tienda/carrito";
    }

    @PostMapping("/carrito/vaciar")
    public String vaciarCarrito() {
        carrito.vaciar();
        return "redirect:/tienda/carrito";
    }

    @GetMapping("/checkout")
    public String checkoutForm(Model model, Principal principal) {
        if (carrito.isEmpty()) {
            return "redirect:/tienda/carrito";
        }
        Persona persona = personaDelUsuario(principal);
        if (persona == null || persona.getTipoDocumento() == null
                || persona.getNumeroDocumento() == null || persona.getNumeroDocumento().isBlank()) {
            return "redirect:/tienda/perfil?volver=checkout";
        }
        model.addAttribute("carrito", carrito);
        model.addAttribute("persona", persona);
        model.addAttribute("titulo", "Finalizar Compra");
        return "tienda/checkout";
    }

    @PostMapping("/checkout")
    public String checkout(@RequestParam MetodoPago metodoPago, Principal principal, Model model) {
        if (carrito.isEmpty()) {
            return "redirect:/tienda/carrito";
        }
        Persona persona = personaDelUsuario(principal);
        if (persona == null) {
            return "redirect:/tienda/perfil?volver=checkout";
        }

        List<ItemVenta> items = carrito.getItems().stream()
                .map(i -> new ItemVenta(i.getProductoId(), i.getCantidad()))
                .collect(Collectors.toList());

        Pedido pedido = pedidoService.crearVenta(persona.getId(), items, CanalVenta.ONLINE, metodoPago, null);
        carrito.vaciar();
        return "redirect:/pedidos/" + pedido.getId();
    }

    @GetMapping("/perfil")
    public String perfil(Model model, Principal principal) {
        Persona persona = personaDelUsuario(principal);
        model.addAttribute("persona", persona != null ? persona : new Persona());
        model.addAttribute("cambioPasswordForm", new CambioPasswordForm());
        model.addAttribute("titulo", "Mi Perfil");
        return "tienda/perfil";
    }

    @PostMapping("/perfil")
    public String guardarPerfil(@ModelAttribute Persona persona, Principal principal) {
        Usuario usuario = usuarioService.buscarPorUsername(principal.getName()).orElse(null);
        if (usuario == null || usuario.getPersona() == null) {
            return "redirect:/tienda";
        }
        // El id siempre se toma de la cuenta autenticada, nunca del formulario:
        // evita que un cliente edite la ficha de otra persona.
        persona.setId(usuario.getPersona().getId());
        personaService.guardar(persona);
        return "redirect:/tienda/checkout";
    }

    @GetMapping("/pedidos")
    public String misPedidos(Model model, Principal principal) {
        Persona persona = personaDelUsuario(principal);
        List<Pedido> pedidos = persona != null ? pedidoService.obtenerPorPersona(persona.getId()) : List.of();
        model.addAttribute("pedidos", pedidos);
        model.addAttribute("titulo", "Mis Pedidos");
        return "tienda/mis-pedidos";
    }

    private Persona personaDelUsuario(Principal principal) {
        if (principal == null) return null;
        Usuario usuario = usuarioService.buscarPorUsername(principal.getName()).orElse(null);
        return usuario != null ? usuario.getPersona() : null;
    }
}
