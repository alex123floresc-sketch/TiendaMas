package episs.unaj.com.crudpersona.controller;

import episs.unaj.com.crudpersona.entity.DetallePedido;
import episs.unaj.com.crudpersona.entity.Pedido;
import episs.unaj.com.crudpersona.entity.Producto;
import episs.unaj.com.crudpersona.service.PedidoService;
import episs.unaj.com.crudpersona.service.PersonaService;
import episs.unaj.com.crudpersona.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/reportes")
public class ReporteController {

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private ProductoService productoService;

    @Autowired
    private PersonaService personaService;

    private static final int STOCK_BAJO_UMBRAL = 5;
    private static final int TOP_PRODUCTOS_LIMITE = 5;

    @GetMapping
    public String reportes(Model model) {
        List<Pedido> pedidos = pedidoService.obtenerTodos();
        List<Producto> productos = productoService.obtenerTodos();

        double totalVentas = 0.0;
        Map<String, Integer> unidadesPorProducto = new LinkedHashMap<>();
        Map<String, Double> ventasPorCategoria = new LinkedHashMap<>();

        for (Pedido pedido : pedidos) {
            totalVentas += pedido.getTotal() != null ? pedido.getTotal() : 0.0;

            for (DetallePedido detalle : pedido.getDetalles()) {
                Producto producto = detalle.getProducto();
                if (producto == null) continue;

                int cantidad = detalle.getCantidad() != null ? detalle.getCantidad() : 0;
                unidadesPorProducto.merge(producto.getNombre(), cantidad, Integer::sum);

                String categoriaNombre = producto.getCategoria() != null
                        ? producto.getCategoria().getNombre() : "Sin categoría";
                double subtotal = detalle.getSubtotal() != null ? detalle.getSubtotal() : 0.0;
                ventasPorCategoria.merge(categoriaNombre, subtotal, Double::sum);
            }
        }

        List<Map.Entry<String, Integer>> topProductos = unidadesPorProducto.entrySet().stream()
                .sorted((a, b) -> b.getValue() - a.getValue())
                .limit(TOP_PRODUCTOS_LIMITE)
                .toList();

        List<Producto> bajoStock = productos.stream()
                .filter(p -> p.getStock() != null && p.getStock() <= STOCK_BAJO_UMBRAL)
                .sorted(Comparator.comparing(Producto::getStock))
                .toList();

        model.addAttribute("titulo", "Reportes");
        model.addAttribute("totalVentas", totalVentas);
        model.addAttribute("totalPedidos", pedidos.size());
        model.addAttribute("totalClientes", personaService.obtenerTodas().size());
        model.addAttribute("totalProductos", productos.size());
        model.addAttribute("topProductos", topProductos);
        model.addAttribute("ventasPorCategoria", ventasPorCategoria);
        model.addAttribute("bajoStock", bajoStock);
        return "reportes/index";
    }
}
