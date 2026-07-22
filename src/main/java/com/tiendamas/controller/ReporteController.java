package com.tiendamas.controller;

import com.tiendamas.dto.RecomendacionReabastecimiento;
import com.tiendamas.dto.ResumenMensual;
import com.tiendamas.dto.ResumenVentasPeriodo;
import com.tiendamas.entity.CanalVenta;
import com.tiendamas.entity.DetallePedido;
import com.tiendamas.entity.Gasto;
import com.tiendamas.entity.Pedido;
import com.tiendamas.entity.Producto;
import com.tiendamas.entity.Sueldo;
import com.tiendamas.entity.Usuario;
import com.tiendamas.service.GastoService;
import com.tiendamas.service.PedidoService;
import com.tiendamas.service.PersonaService;
import com.tiendamas.service.ProductoService;
import com.tiendamas.service.SueldoService;
import com.tiendamas.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/reportes")
public class ReporteController {

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private ProductoService productoService;

    @Autowired
    private PersonaService personaService;

    @Autowired
    private GastoService gastoService;

    @Autowired
    private SueldoService sueldoService;

    @Autowired
    private UsuarioService usuarioService;

    private static final int STOCK_BAJO_UMBRAL = 5;
    private static final int TOP_PRODUCTOS_LIMITE = 5;
    private static final int MESES_RESUMEN = 6;
    private static final int SEMANAS_RESUMEN = 8;
    private static final int VENTANA_REABASTECIMIENTO_DIAS = 30;
    private static final double DIAS_RESTANTES_URGENTE = 7;
    private static final double DIAS_RESTANTES_MODERADO = 14;
    private static final String[] MESES_ABREV = {
            "Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"
    };

    @GetMapping
    public String reportes(Model model) {
        List<Pedido> pedidos = pedidoService.obtenerTodos();
        List<Producto> productos = productoService.obtenerTodos();

        double totalVentas = 0.0;
        Map<String, Integer> unidadesPorProducto = new LinkedHashMap<>();
        Map<String, Double> ventasPorCategoria = new LinkedHashMap<>();
        Map<String, Double> ventasPorCanal = new LinkedHashMap<>();
        Map<String, Double> ventasPorMetodoPago = new LinkedHashMap<>();
        Map<String, Double> ventasPorVendedor = new LinkedHashMap<>();

        Map<String, Usuario> usuariosPorUsername = usuarioService.obtenerTodos().stream()
                .collect(Collectors.toMap(Usuario::getUsername, u -> u, (a, b) -> a));

        for (Pedido pedido : pedidos) {
            double totalPedido = pedido.getTotal() != null ? pedido.getTotal() : 0.0;
            totalVentas += totalPedido;

            String canalNombre = pedido.getCanal() != null ? pedido.getCanal().getEtiqueta() : "Sin canal";
            ventasPorCanal.merge(canalNombre, totalPedido, Double::sum);

            String metodoPagoNombre = pedido.getMetodoPago() != null ? pedido.getMetodoPago().getEtiqueta() : "Sin especificar";
            ventasPorMetodoPago.merge(metodoPagoNombre, totalPedido, Double::sum);

            String vendedorUsername = pedido.getVendedorUsername();
            String vendedorNombre;
            if (vendedorUsername == null || vendedorUsername.isBlank()) {
                vendedorNombre = "Venta en línea (autoservicio)";
            } else {
                Usuario vendedor = usuariosPorUsername.get(vendedorUsername);
                vendedorNombre = vendedor != null ? (vendedor.getNombre() + " " + vendedor.getApellido()) : vendedorUsername;
            }
            ventasPorVendedor.merge(vendedorNombre, totalPedido, Double::sum);

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

        List<Producto> masVendidos = pedidoService.obtenerMasVendidos(TOP_PRODUCTOS_LIMITE);

        Map<Long, Integer> unidadesUltimos30Dias = pedidoService.obtenerUnidadesVendidasDesde(
                LocalDateTime.now().minusDays(VENTANA_REABASTECIMIENTO_DIAS));
        List<RecomendacionReabastecimiento> recomendacionesReabastecimiento =
                calcularReabastecimiento(productos, unidadesUltimos30Dias);

        List<ResumenMensual> resumenMensual = calcularResumenMensual(pedidos, gastoService.obtenerTodos(), sueldoService.obtenerTodos());
        double maxResumenMensual = resumenMensual.stream()
                .flatMap(r -> java.util.stream.Stream.of(r.getVentas(), r.getGastos(), r.getSueldos()))
                .max(Double::compare)
                .orElse(1.0);
        if (maxResumenMensual <= 0) {
            maxResumenMensual = 1.0;
        }

        List<ResumenVentasPeriodo> resumenSemanal = calcularResumenSemanal(pedidos);
        List<ResumenVentasPeriodo> resumenMensualVentas = calcularResumenMensualVentas(pedidos);

        model.addAttribute("titulo", "Reportes");
        model.addAttribute("totalVentas", totalVentas);
        model.addAttribute("totalPedidos", pedidos.size());
        model.addAttribute("totalClientes", personaService.obtenerTodas().size());
        model.addAttribute("totalProductos", productos.size());
        model.addAttribute("masVendidos", masVendidos);
        model.addAttribute("unidadesPorProducto", unidadesPorProducto);
        model.addAttribute("ventasPorCategoria", ventasPorCategoria);
        model.addAttribute("ventasPorCanal", ventasPorCanal);
        model.addAttribute("ventasPorMetodoPago", ventasPorMetodoPago);
        model.addAttribute("ventasPorVendedor", ventasPorVendedor);
        model.addAttribute("recomendacionesReabastecimiento", recomendacionesReabastecimiento);
        model.addAttribute("resumenMensual", resumenMensual);
        model.addAttribute("maxResumenMensual", maxResumenMensual);
        model.addAttribute("resumenSemanal", resumenSemanal);
        model.addAttribute("resumenMensualVentas", resumenMensualVentas);
        return "reportes/index";
    }

    private List<RecomendacionReabastecimiento> calcularReabastecimiento(List<Producto> productos, Map<Long, Integer> unidadesUltimos30Dias) {
        List<RecomendacionReabastecimiento> resultado = new ArrayList<>();

        for (Producto producto : productos) {
            int stock = producto.getStock() != null ? producto.getStock() : 0;
            int vendidas = unidadesUltimos30Dias.getOrDefault(producto.getId(), 0);
            double velocidadDiaria = vendidas / (double) VENTANA_REABASTECIMIENTO_DIAS;
            Double diasRestantes = velocidadDiaria > 0 ? stock / velocidadDiaria : null;

            boolean urgente = stock <= STOCK_BAJO_UMBRAL || (diasRestantes != null && diasRestantes <= DIAS_RESTANTES_URGENTE);
            boolean moderado = !urgente && diasRestantes != null && diasRestantes <= DIAS_RESTANTES_MODERADO;

            if (!urgente && !moderado) continue;

            resultado.add(new RecomendacionReabastecimiento(producto, vendidas, diasRestantes, urgente ? "URGENTE" : "MODERADO"));
        }

        resultado.sort((a, b) -> {
            double da = a.getDiasRestantes() != null ? a.getDiasRestantes() : Double.MAX_VALUE;
            double db = b.getDiasRestantes() != null ? b.getDiasRestantes() : Double.MAX_VALUE;
            return Double.compare(da, db);
        });
        return resultado;
    }

    private List<ResumenMensual> calcularResumenMensual(List<Pedido> pedidos, List<Gasto> gastos, List<Sueldo> sueldos) {
        List<ResumenMensual> resultado = new ArrayList<>();
        YearMonth mesActual = YearMonth.now();

        for (int i = MESES_RESUMEN - 1; i >= 0; i--) {
            YearMonth mes = mesActual.minusMonths(i);

            double ventasMes = pedidos.stream()
                    .filter(p -> p.getFecha() != null && YearMonth.from(p.getFecha()).equals(mes))
                    .mapToDouble(p -> p.getTotal() != null ? p.getTotal() : 0.0)
                    .sum();

            double gastosMes = gastos.stream()
                    .filter(g -> g.getFecha() != null && YearMonth.from(g.getFecha()).equals(mes))
                    .mapToDouble(g -> g.getMonto() != null ? g.getMonto() : 0.0)
                    .sum();

            double sueldosMes = sueldos.stream()
                    .filter(s -> s.getFechaPago() != null && YearMonth.from(s.getFechaPago()).equals(mes))
                    .mapToDouble(s -> s.getMonto() != null ? s.getMonto() : 0.0)
                    .sum();

            String etiqueta = MESES_ABREV[mes.getMonthValue() - 1] + " " + mes.getYear();
            resultado.add(new ResumenMensual(etiqueta, ventasMes, gastosMes, sueldosMes));
        }
        return resultado;
    }

    private List<ResumenVentasPeriodo> calcularResumenSemanal(List<Pedido> pedidos) {
        List<ResumenVentasPeriodo> resultado = new ArrayList<>();
        LocalDate lunesActual = LocalDate.now().with(DayOfWeek.MONDAY);

        for (int i = SEMANAS_RESUMEN - 1; i >= 0; i--) {
            LocalDate lunes = lunesActual.minusWeeks(i);
            LocalDate domingo = lunes.plusDays(6);

            double ventasFisica = sumaVentasEnRango(pedidos, CanalVenta.TIENDA_FISICA, lunes, domingo);
            double ventasOnline = sumaVentasEnRango(pedidos, CanalVenta.ONLINE, lunes, domingo);

            String etiqueta = lunes.getDayOfMonth() + " " + MESES_ABREV[lunes.getMonthValue() - 1];
            resultado.add(new ResumenVentasPeriodo(etiqueta, ventasFisica, ventasOnline));
        }
        return resultado;
    }

    private List<ResumenVentasPeriodo> calcularResumenMensualVentas(List<Pedido> pedidos) {
        List<ResumenVentasPeriodo> resultado = new ArrayList<>();
        YearMonth mesActual = YearMonth.now();

        for (int i = MESES_RESUMEN - 1; i >= 0; i--) {
            YearMonth mes = mesActual.minusMonths(i);

            double ventasFisica = pedidos.stream()
                    .filter(p -> p.getCanal() == CanalVenta.TIENDA_FISICA
                            && p.getFecha() != null && YearMonth.from(p.getFecha()).equals(mes))
                    .mapToDouble(p -> p.getTotal() != null ? p.getTotal() : 0.0)
                    .sum();

            double ventasOnline = pedidos.stream()
                    .filter(p -> p.getCanal() == CanalVenta.ONLINE
                            && p.getFecha() != null && YearMonth.from(p.getFecha()).equals(mes))
                    .mapToDouble(p -> p.getTotal() != null ? p.getTotal() : 0.0)
                    .sum();

            String etiqueta = MESES_ABREV[mes.getMonthValue() - 1] + " " + mes.getYear();
            resultado.add(new ResumenVentasPeriodo(etiqueta, ventasFisica, ventasOnline));
        }
        return resultado;
    }

    private double sumaVentasEnRango(List<Pedido> pedidos, CanalVenta canal, LocalDate desde, LocalDate hasta) {
        return pedidos.stream()
                .filter(p -> p.getCanal() == canal && p.getFecha() != null)
                .filter(p -> {
                    LocalDate fecha = p.getFecha().toLocalDate();
                    return !fecha.isBefore(desde) && !fecha.isAfter(hasta);
                })
                .mapToDouble(p -> p.getTotal() != null ? p.getTotal() : 0.0)
                .sum();
    }
}
