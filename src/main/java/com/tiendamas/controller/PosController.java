package com.tiendamas.controller;

import com.tiendamas.dto.CategoriaResumenDto;
import com.tiendamas.dto.ItemVenta;
import com.tiendamas.dto.ProductoBusquedaDto;
import com.tiendamas.entity.CanalVenta;
import com.tiendamas.entity.MetodoPago;
import com.tiendamas.entity.Pedido;
import com.tiendamas.entity.TipoEntrega;
import com.tiendamas.entity.VarianteProducto;
import com.tiendamas.service.CategoriaService;
import com.tiendamas.service.PedidoService;
import com.tiendamas.service.PersonaService;
import com.tiendamas.service.ProductoService;
import com.tiendamas.web.Carrito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/pos")
public class PosController {

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

    @GetMapping
    public String pos(Model model) {
        model.addAttribute("titulo", "Punto de Venta");
        model.addAttribute("carrito", carrito);
        model.addAttribute("personas", personaService.obtenerTodas());
        model.addAttribute("clienteGenerico", personaService.obtenerOClienteGenerico());
        return "pos/index";
    }

    // ---------- API de búsqueda (buscador de productos y categorías) ----------

    @GetMapping("/api/productos")
    @ResponseBody
    public List<ProductoBusquedaDto> buscarProductos(@RequestParam(required = false) String q,
                                                       @RequestParam(required = false) Long categoriaId) {
        return productoService.buscar(q, categoriaId).stream()
                .map(ProductoBusquedaDto::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/api/categorias")
    @ResponseBody
    public List<CategoriaResumenDto> listarCategorias() {
        return categoriaService.obtenerTodas().stream()
                .map(CategoriaResumenDto::new)
                .collect(Collectors.toList());
    }

    // ---------- API del carrito (usada por el escáner y el buscador vía AJAX) ----------

    @PostMapping("/api/carrito/agregar-por-codigo")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> apiAgregarPorCodigo(@RequestParam String codigoBarras,
                                                                    @RequestParam(required = false, defaultValue = "1") Integer cantidad) {
        VarianteProducto variante = productoService.obtenerVariantePorCodigoBarras(codigoBarras);
        if (variante == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "error", "codigoNoEncontrado"));
        }
        carrito.agregar(variante, cantidad != null && cantidad > 0 ? cantidad : 1);
        return ResponseEntity.ok(respuestaExito(variante));
    }

    @PostMapping("/api/carrito/agregar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> apiAgregarManual(@RequestParam Long varianteId,
                                                                 @RequestParam(required = false, defaultValue = "1") Integer cantidad) {
        VarianteProducto variante = productoService.obtenerVariante(varianteId);
        if (variante == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "error", "productoNoEncontrado"));
        }
        carrito.agregar(variante, cantidad != null && cantidad > 0 ? cantidad : 1);
        return ResponseEntity.ok(respuestaExito(variante));
    }

    @PostMapping("/api/carrito/cantidad")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> apiActualizarCantidad(@RequestParam Long varianteId, @RequestParam Integer cantidad) {
        carrito.actualizarCantidad(varianteId, cantidad);
        return ResponseEntity.ok(snapshotCarrito());
    }

    @PostMapping("/api/carrito/quitar/{varianteId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> apiQuitar(@PathVariable Long varianteId) {
        carrito.quitar(varianteId);
        return ResponseEntity.ok(snapshotCarrito());
    }

    @PostMapping("/api/carrito/vaciar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> apiVaciar() {
        carrito.vaciar();
        return ResponseEntity.ok(snapshotCarrito());
    }

    // ---------- Endpoints clásicos (fallback sin JavaScript) ----------

    @PostMapping("/carrito/agregar-por-codigo")
    public String agregarPorCodigo(@RequestParam String codigoBarras,
                                    @RequestParam(required = false, defaultValue = "1") Integer cantidad) {
        VarianteProducto variante = productoService.obtenerVariantePorCodigoBarras(codigoBarras);
        if (variante == null) {
            return "redirect:/pos?error=codigoNoEncontrado";
        }
        carrito.agregar(variante, cantidad != null && cantidad > 0 ? cantidad : 1);
        return "redirect:/pos";
    }

    @PostMapping("/carrito/agregar")
    public String agregarManual(@RequestParam Long varianteId,
                                 @RequestParam(required = false, defaultValue = "1") Integer cantidad) {
        VarianteProducto variante = productoService.obtenerVariante(varianteId);
        if (variante == null) {
            return "redirect:/pos?error=productoNoEncontrado";
        }
        carrito.agregar(variante, cantidad != null && cantidad > 0 ? cantidad : 1);
        return "redirect:/pos";
    }

    @PostMapping("/carrito/cantidad")
    public String actualizarCantidad(@RequestParam Long varianteId, @RequestParam Integer cantidad) {
        carrito.actualizarCantidad(varianteId, cantidad);
        return "redirect:/pos";
    }

    @PostMapping("/carrito/quitar/{varianteId}")
    public String quitar(@PathVariable Long varianteId) {
        carrito.quitar(varianteId);
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
                .map(i -> new ItemVenta(i.getVarianteId(), i.getCantidad()))
                .collect(Collectors.toList());

        Pedido pedido;
        try {
            pedido = pedidoService.crearVenta(personaId, items, CanalVenta.TIENDA_FISICA, metodoPago, principal.getName(),
                    TipoEntrega.RETIRO_TIENDA, null);
        } catch (IllegalArgumentException e) {
            return "redirect:/pos?error=stockInsuficiente";
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

    private Map<String, Object> snapshotCarrito() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("success", true);
        data.put("items", carrito.getItems());
        data.put("total", carrito.getTotal());
        data.put("cantidadTotal", carrito.getCantidadTotal());
        return data;
    }

    private Map<String, Object> respuestaExito(VarianteProducto variante) {
        Map<String, Object> data = snapshotCarrito();
        data.put("productoAgregado", variante.getProducto().getNombre() + " (" + variante.getEtiqueta() + ")");
        data.put("stockDisponible", variante.getStock());
        return data;
    }
}
