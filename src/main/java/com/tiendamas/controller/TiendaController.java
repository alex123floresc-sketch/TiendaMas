package com.tiendamas.controller;

import com.tiendamas.dto.CambioPasswordForm;
import com.tiendamas.dto.ItemVenta;
import com.tiendamas.dto.SugerenciaProducto;
import com.tiendamas.entity.CanalVenta;
import com.tiendamas.entity.Categoria;
import com.tiendamas.entity.MetodoPago;
import com.tiendamas.entity.Pedido;
import com.tiendamas.entity.Persona;
import com.tiendamas.entity.Producto;
import com.tiendamas.entity.Suscriptor;
import com.tiendamas.entity.Talla;
import com.tiendamas.entity.TipoEntrega;
import com.tiendamas.entity.Usuario;
import com.tiendamas.entity.VarianteProducto;
import com.tiendamas.repository.SuscriptorRepository;
import com.tiendamas.service.CategoriaService;
import com.tiendamas.service.PedidoService;
import com.tiendamas.service.PersonaService;
import com.tiendamas.service.ProductoService;
import com.tiendamas.service.UsuarioService;
import com.tiendamas.web.Carrito;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.security.Principal;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SuscriptorRepository suscriptorRepository;

    @GetMapping
    public String index(@RequestParam(required = false) String q,
                         @RequestParam(required = false) Long categoriaId,
                         @RequestParam(required = false) Talla talla,
                         @RequestParam(required = false) String precioRango,
                         @RequestParam(required = false, defaultValue = "relevancia") String orden, Model model) {
        Double precioMin = null;
        Double precioMax = null;
        if (precioRango != null) {
            switch (precioRango) {
                case "0-50" -> precioMax = 50.0;
                case "50-100" -> { precioMin = 50.0; precioMax = 100.0; }
                case "100-200" -> { precioMin = 100.0; precioMax = 200.0; }
                case "200+" -> precioMin = 200.0;
                default -> precioRango = null;
            }
        }
        List<Producto> productos = ordenar(productoService.buscar(q, categoriaId, talla, precioMin, precioMax), orden);

        model.addAttribute("productos", productos);
        model.addAttribute("categorias", categoriaService.obtenerPrincipales());
        model.addAttribute("categoriaSeleccionada", categoriaId);
        model.addAttribute("tallas", Talla.values());
        model.addAttribute("tallaSeleccionada", talla);
        model.addAttribute("precioRango", precioRango);
        model.addAttribute("q", q);
        model.addAttribute("orden", orden);
        if ((q == null || q.isBlank()) && categoriaId == null && talla == null && precioRango == null) {
            model.addAttribute("masVendidos", pedidoService.obtenerMasVendidos(10));
            model.addAttribute("recientes", productoService.obtenerRecientes());
            long totalMarcas = productos.stream()
                    .map(Producto::getMarca)
                    .filter(m -> m != null && !m.isBlank())
                    .distinct()
                    .count();
            model.addAttribute("totalMarcas", totalMarcas);

            Map<Categoria, List<Producto>> carruseles = new LinkedHashMap<>();
            Map<Long, String> imagenPorCategoria = new LinkedHashMap<>();
            for (Categoria cat : categoriaService.obtenerPrincipales()) {
                List<Producto> destacados = productoService.buscar(null, cat.getId(), null, null, null);
                if (!destacados.isEmpty()) {
                    carruseles.put(cat, destacados.stream().limit(10).toList());
                    destacados.stream()
                            .filter(p -> p.getImagenUrl() != null)
                            .findFirst()
                            .ifPresent(p -> imagenPorCategoria.put(cat.getId(), p.getImagenUrl()));
                }
            }
            model.addAttribute("carruseles", carruseles);
            model.addAttribute("imagenPorCategoria", imagenPorCategoria);
        }
        model.addAttribute("titulo", "Tienda");
        model.addAttribute("metaDescripcion", "Ropa y accesorios para hombre, mujer y niños. Envío a domicilio o recojo en tienda, pago 100% seguro.");
        return "tienda/index";
    }

    private List<Producto> ordenar(List<Producto> productos, String orden) {
        Comparator<Producto> comparador = switch (orden == null ? "" : orden) {
            case "precio_asc" -> Comparator.comparing(Producto::getPrecio, Comparator.nullsLast(Double::compareTo));
            case "precio_desc" -> Comparator.comparing(Producto::getPrecio, Comparator.nullsLast(Double::compareTo)).reversed();
            case "nombre_az" -> Comparator.comparing(Producto::getNombre, String.CASE_INSENSITIVE_ORDER);
            case "nombre_za" -> Comparator.comparing(Producto::getNombre, String.CASE_INSENSITIVE_ORDER).reversed();
            default -> null;
        };
        if (comparador == null) {
            return productos;
        }
        return productos.stream().sorted(comparador).collect(Collectors.toList());
    }

    @GetMapping("/buscar-sugerencias")
    @ResponseBody
    public List<SugerenciaProducto> buscarSugerencias(@RequestParam(required = false) String q) {
        if (q == null || q.isBlank() || q.trim().length() < 2) {
            return List.of();
        }
        return productoService.buscar(q, null, null, null, null).stream()
                .limit(6)
                .map(p -> new SugerenciaProducto(p.getId(), p.getNombre(), p.getImagenUrl(), p.getPrecio(),
                        p.getCategoria() != null ? p.getCategoria().getNombre() : null,
                        "/tienda/productos/" + p.getId()))
                .toList();
    }

    @PostMapping("/newsletter")
    public String suscribirNewsletter(@RequestParam String email, RedirectAttributes redirectAttributes) {
        String correo = email == null ? "" : email.trim();
        if (!correo.matches("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$")) {
            redirectAttributes.addFlashAttribute("newsletterError", "invalido");
        } else if (suscriptorRepository.existsByEmailIgnoreCase(correo)) {
            redirectAttributes.addFlashAttribute("newsletterError", "yaSuscrito");
        } else {
            suscriptorRepository.save(new Suscriptor(correo));
            redirectAttributes.addFlashAttribute("newsletterOk", true);
        }
        return "redirect:/tienda#newsletter";
    }

    @GetMapping("/favoritos")
    public String favoritos(Model model) {
        model.addAttribute("titulo", "Mis Favoritos");
        model.addAttribute("metaDescripcion", "Los productos que guardaste como favoritos en TiendaMas.");
        return "tienda/favoritos";
    }

    @GetMapping("/info")
    public String info(@RequestParam(required = false) String seccion, Model model) {
        model.addAttribute("seccion", seccion);
        model.addAttribute("titulo", "Información y ayuda");
        model.addAttribute("metaDescripcion", "Quiénes somos, envíos y entregas, cambios y devoluciones, términos y condiciones, y libro de reclamaciones de TiendaMas.");
        return "tienda/info";
    }

    @GetMapping("/productos/{id}")
    public String detalle(@PathVariable Long id, Model model, HttpServletRequest request) {
        Producto producto = productoService.obtenerPorId(id);
        if (producto == null) {
            return "redirect:/tienda";
        }
        String imagen = producto.getImagenUrl() != null ? producto.getImagenUrl()
                : (!producto.getImagenes().isEmpty() ? producto.getImagenes().get(0).getUrl() : null);
        model.addAttribute("producto", producto);
        model.addAttribute("relacionados", productoService.obtenerRelacionados(producto));
        model.addAttribute("titulo", producto.getNombre());
        model.addAttribute("metaDescripcion", descripcionCorta(producto.getDescripcion(), producto.getNombre()));
        model.addAttribute("ogImage", imagen);
        model.addAttribute("productoJsonLd", jsonLdProducto(producto, imagen, request));
        return "tienda/detalle";
    }

    private String descripcionCorta(String descripcion, String nombre) {
        String base = descripcion != null && !descripcion.isBlank() ? descripcion : "Comprá " + nombre + " en TiendaMas.";
        return base.length() > 160 ? base.substring(0, 157) + "..." : base;
    }

    private String jsonLdProducto(Producto producto, String imagen, HttpServletRequest request) {
        String baseUrl = request.getRequestURL().substring(0, request.getRequestURL().length() - request.getRequestURI().length());
        Map<String, Object> jsonLd = new LinkedHashMap<>();
        jsonLd.put("@context", "https://schema.org/");
        jsonLd.put("@type", "Product");
        jsonLd.put("name", producto.getNombre());
        jsonLd.put("description", descripcionCorta(producto.getDescripcion(), producto.getNombre()));
        jsonLd.put("sku", String.valueOf(producto.getId()));
        if (imagen != null) {
            jsonLd.put("image", List.of(baseUrl + imagen));
        }
        if (producto.getMarca() != null && !producto.getMarca().isEmpty()) {
            jsonLd.put("brand", Map.of("@type", "Brand", "name", producto.getMarca()));
        }
        Map<String, Object> offer = new LinkedHashMap<>();
        offer.put("@type", "Offer");
        offer.put("priceCurrency", "PEN");
        offer.put("price", producto.getPrecio());
        offer.put("availability", producto.tieneStock() ? "https://schema.org/InStock" : "https://schema.org/OutOfStock");
        offer.put("url", baseUrl + "/tienda/productos/" + producto.getId());
        jsonLd.put("offers", offer);
        try {
            return objectMapper.writeValueAsString(jsonLd);
        } catch (JacksonException e) {
            return null;
        }
    }

    @PostMapping("/carrito/agregar")
    public String agregar(@RequestParam Long varianteId, @RequestParam(required = false, defaultValue = "1") Integer cantidad) {
        VarianteProducto variante = productoService.obtenerVariante(varianteId);
        if (variante != null) {
            carrito.agregar(variante, cantidad != null && cantidad > 0 ? cantidad : 1);
        }
        return "redirect:/tienda/carrito?agregado=1";
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
    public String actualizarCantidad(@RequestParam Long varianteId, @RequestParam Integer cantidad) {
        carrito.actualizarCantidad(varianteId, cantidad);
        return "redirect:/tienda/carrito";
    }

    @PostMapping("/carrito/quitar/{varianteId}")
    public String quitar(@PathVariable Long varianteId) {
        carrito.quitar(varianteId);
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
    public String checkout(@RequestParam MetodoPago metodoPago,
                            @RequestParam TipoEntrega tipoEntrega,
                            @RequestParam(required = false) String direccionEntrega,
                            Principal principal, Model model) {
        if (carrito.isEmpty()) {
            return "redirect:/tienda/carrito";
        }
        Persona persona = personaDelUsuario(principal);
        if (persona == null) {
            return "redirect:/tienda/perfil?volver=checkout";
        }

        String direccion = tipoEntrega == TipoEntrega.DOMICILIO
                ? (direccionEntrega != null && !direccionEntrega.isBlank() ? direccionEntrega : persona.getDireccion())
                : null;

        List<ItemVenta> items = carrito.getItems().stream()
                .map(i -> new ItemVenta(i.getVarianteId(), i.getCantidad()))
                .collect(Collectors.toList());

        Pedido pedido;
        try {
            pedido = pedidoService.crearVenta(persona.getId(), items, CanalVenta.ONLINE, metodoPago, null,
                    tipoEntrega, direccion);
        } catch (IllegalArgumentException e) {
            return "redirect:/tienda/carrito?error=stockInsuficiente";
        }
        carrito.vaciar();
        return "redirect:/tienda/confirmacion/" + pedido.getId();
    }

    @GetMapping("/confirmacion/{id}")
    public String confirmacion(@PathVariable Long id, Model model, Principal principal) {
        Pedido pedido = pedidoService.obtenerPorId(id);
        Persona persona = personaDelUsuario(principal);
        boolean esDueno = pedido != null && persona != null && pedido.getPersona() != null
                && pedido.getPersona().getId().equals(persona.getId());
        if (!esDueno) {
            return "redirect:/tienda";
        }
        model.addAttribute("pedido", pedido);
        model.addAttribute("titulo", "Pedido confirmado");
        return "tienda/confirmacion";
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
