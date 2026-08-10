package com.tiendamas.controller;

import com.tiendamas.dto.ClienteResumen;
import com.tiendamas.dto.RecomendacionReabastecimiento;
import com.tiendamas.dto.ResumenMensual;
import com.tiendamas.dto.ResumenVentasPeriodo;
import com.tiendamas.entity.CanalVenta;
import com.tiendamas.entity.DetallePedido;
import com.tiendamas.entity.Gasto;
import com.tiendamas.entity.Pedido;
import com.tiendamas.entity.Persona;
import com.tiendamas.entity.Producto;
import com.tiendamas.entity.Sueldo;
import com.tiendamas.entity.Usuario;
import com.tiendamas.entity.VarianteProducto;
import com.tiendamas.service.GastoService;
import com.tiendamas.service.PedidoService;
import com.tiendamas.service.PersonaService;
import com.tiendamas.service.ProductoService;
import com.tiendamas.service.SueldoService;
import com.tiendamas.service.UsuarioService;
import com.tiendamas.service.impl.ReporteChartService;
import com.tiendamas.service.impl.ReportePdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    @Autowired
    private ReportePdfService reportePdfService;

    @Autowired
    private ReporteChartService reporteChartService;

    private static final int STOCK_BAJO_UMBRAL = 5;
    private static final int TOP_PRODUCTOS_LIMITE = 8;
    private static final int TOP_CLIENTES_LIMITE = 8;
    private static final int MESES_RESUMEN = 6;
    private static final int SEMANAS_RESUMEN = 8;
    private static final int VENTANA_REABASTECIMIENTO_DIAS = 30;
    private static final double DIAS_RESTANTES_URGENTE = 7;
    private static final double DIAS_RESTANTES_MODERADO = 14;
    private static final String[] MESES_ABREV = {
            "Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"
    };

    @GetMapping
    public String reportes(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                            Model model) {
        model.addAllAttributes(calcularReporte(desde, hasta));
        model.addAttribute("titulo", "Reportes");
        model.addAttribute("desde", desde);
        model.addAttribute("hasta", hasta);
        return "reportes/index";
    }

    @GetMapping("/pdf")
    @SuppressWarnings("unchecked")
    public ResponseEntity<byte[]> exportarPdf(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        Map<String, Object> datos = calcularReporte(desde, hasta);
        datos.put("desde", desde);
        datos.put("hasta", hasta);
        datos.put("generadoEl", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

        // Los gráficos de la pantalla se dibujan con Chart.js (JavaScript), que el generador de PDF
        // no ejecuta; acá se renderizan de nuevo como imágenes estáticas para que salgan en el PDF.
        datos.put("chartCanalBase64", reporteChartService.graficoTorta(
                (Map<String, Double>) datos.get("ventasPorCanal"), "Ventas por canal"));
        datos.put("chartMetodoPagoBase64", reporteChartService.graficoTorta(
                (Map<String, Double>) datos.get("ventasPorMetodoPago"), "Ventas por método de pago"));
        datos.put("chartVendedorBase64", reporteChartService.graficoTorta(
                (Map<String, Double>) datos.get("ventasPorVendedor"), "Ventas por vendedor"));
        datos.put("chartSemanalBase64", reporteChartService.graficoVentasPorCanal(
                (List<ResumenVentasPeriodo>) datos.get("resumenSemanal"), "Ventas por semana (últimas 8 semanas)"));
        datos.put("chartMensualVentasBase64", reporteChartService.graficoVentasPorCanal(
                (List<ResumenVentasPeriodo>) datos.get("resumenMensualVentas"), "Ventas por mes (últimos 6 meses)"));
        datos.put("chartFinancieroBase64", reporteChartService.graficoFinanciero(
                (List<ResumenMensual>) datos.get("resumenMensual")));

        byte[] pdf = reportePdfService.generarPdf(datos);
        String nombreArchivo = "Reporte-TiendaMas-" + LocalDate.now() + ".pdf";
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(nombreArchivo).build().toString())
                .body(pdf);
    }

    /**
     * Calcula todos los datos del reporte. Las secciones "totales" (ventas, top productos, por
     * categoría/canal/método/vendedor/cliente, estado y entrega) respetan el rango desde/hasta si
     * se indica. Las tendencias (resumen mensual financiero, semanal y mensual por canal) y las
     * recomendaciones de reabastecimiento son siempre sobre el histórico/últimos 30 días, sin
     * importar el filtro, porque muestran una evolución en el tiempo.
     */
    private Map<String, Object> calcularReporte(LocalDate desde, LocalDate hasta) {
        List<Pedido> todosPedidos = pedidoService.obtenerTodos();
        List<Producto> productos = productoService.obtenerTodos();
        List<Pedido> pedidos = filtrarPorFecha(todosPedidos, desde, hasta);

        double totalVentas = 0.0;
        int totalUnidades = 0;
        Map<String, Integer> unidadesPorProducto = new LinkedHashMap<>();
        Map<String, Double> ventasPorCategoria = new LinkedHashMap<>();
        Map<String, Double> ventasPorCanal = new LinkedHashMap<>();
        Map<String, Double> ventasPorMetodoPago = new LinkedHashMap<>();
        Map<String, Double> ventasPorVendedor = new LinkedHashMap<>();
        Map<String, Integer> pedidosPorEstado = new LinkedHashMap<>();
        Map<String, Integer> pedidosPorEntrega = new LinkedHashMap<>();
        Map<Persona, Double> gastoPorCliente = new LinkedHashMap<>();
        Map<Persona, Integer> pedidosPorCliente = new LinkedHashMap<>();

        Map<String, Usuario> usuariosPorUsername = usuarioService.obtenerTodos().stream()
                .collect(Collectors.toMap(Usuario::getUsername, u -> u, (a, b) -> a));

        for (Pedido pedido : pedidos) {
            double totalPedido = pedido.getTotal() != null ? pedido.getTotal() : 0.0;
            totalVentas += totalPedido;

            String canalNombre = pedido.getCanal() != null ? pedido.getCanal().getEtiqueta() : "Sin canal";
            ventasPorCanal.merge(canalNombre, totalPedido, Double::sum);

            String metodoPagoNombre = pedido.getMetodoPago() != null ? pedido.getMetodoPago().getEtiqueta() : "Sin especificar";
            ventasPorMetodoPago.merge(metodoPagoNombre, totalPedido, Double::sum);

            String estadoNombre = pedido.getEstado() != null ? pedido.getEstado().getEtiqueta() : "Sin estado";
            pedidosPorEstado.merge(estadoNombre, 1, Integer::sum);

            String entregaNombre = pedido.getTipoEntrega() != null ? pedido.getTipoEntrega().getEtiqueta() : "Sin especificar";
            pedidosPorEntrega.merge(entregaNombre, 1, Integer::sum);

            String vendedorUsername = pedido.getVendedorUsername();
            String vendedorNombre;
            if (vendedorUsername == null || vendedorUsername.isBlank()) {
                vendedorNombre = "Venta en línea (autoservicio)";
            } else {
                Usuario vendedor = usuariosPorUsername.get(vendedorUsername);
                vendedorNombre = vendedor != null ? (vendedor.getNombre() + " " + vendedor.getApellido()) : vendedorUsername;
            }
            ventasPorVendedor.merge(vendedorNombre, totalPedido, Double::sum);

            if (pedido.getPersona() != null) {
                gastoPorCliente.merge(pedido.getPersona(), totalPedido, Double::sum);
                pedidosPorCliente.merge(pedido.getPersona(), 1, Integer::sum);
            }

            for (DetallePedido detalle : pedido.getDetalles()) {
                Producto producto = detalle.getProducto();
                if (producto == null) continue;

                int cantidad = detalle.getCantidad() != null ? detalle.getCantidad() : 0;
                unidadesPorProducto.merge(producto.getNombre(), cantidad, Integer::sum);
                totalUnidades += cantidad;

                String categoriaNombre = producto.getCategoria() != null
                        ? producto.getCategoria().getNombre() : "Sin categoría";
                double subtotal = detalle.getSubtotal() != null ? detalle.getSubtotal() : 0.0;
                ventasPorCategoria.merge(categoriaNombre, subtotal, Double::sum);
            }
        }

        double ticketPromedio = pedidos.isEmpty() ? 0.0 : totalVentas / pedidos.size();

        List<Map.Entry<String, Integer>> masVendidos = unidadesPorProducto.entrySet().stream()
                .sorted((a, b) -> b.getValue() - a.getValue())
                .limit(TOP_PRODUCTOS_LIMITE)
                .toList();

        List<ClienteResumen> topClientes = gastoPorCliente.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(TOP_CLIENTES_LIMITE)
                .map(e -> new ClienteResumen(e.getKey(), e.getValue(), pedidosPorCliente.getOrDefault(e.getKey(), 0)))
                .toList();

        Map<Long, Integer> unidadesUltimos30Dias = pedidoService.obtenerUnidadesVendidasDesde(
                LocalDateTime.now().minusDays(VENTANA_REABASTECIMIENTO_DIAS));
        List<VarianteProducto> variantes = productos.stream()
                .flatMap(p -> p.getVariantes().stream())
                .toList();
        List<RecomendacionReabastecimiento> recomendacionesReabastecimiento =
                calcularReabastecimiento(variantes, unidadesUltimos30Dias);

        List<ResumenMensual> resumenMensual = calcularResumenMensual(todosPedidos, gastoService.obtenerTodos(), sueldoService.obtenerTodos());
        double maxResumenMensual = resumenMensual.stream()
                .flatMap(r -> Stream.of(r.getVentas(), r.getGastos(), r.getSueldos()))
                .max(Double::compare)
                .orElse(1.0);
        if (maxResumenMensual <= 0) {
            maxResumenMensual = 1.0;
        }

        List<ResumenVentasPeriodo> resumenSemanal = calcularResumenSemanal(todosPedidos);
        List<ResumenVentasPeriodo> resumenMensualVentas = calcularResumenMensualVentas(todosPedidos);

        Map<String, Object> datos = new LinkedHashMap<>();
        datos.put("totalVentas", totalVentas);
        datos.put("totalPedidos", pedidos.size());
        datos.put("totalClientes", personaService.obtenerTodas().size());
        datos.put("totalProductos", productos.size());
        datos.put("totalUnidadesVendidas", totalUnidades);
        datos.put("ticketPromedio", ticketPromedio);
        datos.put("masVendidos", masVendidos);
        datos.put("ventasPorCategoria", ventasPorCategoria);
        datos.put("ventasPorCanal", ventasPorCanal);
        datos.put("ventasPorMetodoPago", ventasPorMetodoPago);
        datos.put("ventasPorVendedor", ventasPorVendedor);
        datos.put("pedidosPorEstado", pedidosPorEstado);
        datos.put("pedidosPorEntrega", pedidosPorEntrega);
        datos.put("topClientes", topClientes);
        datos.put("recomendacionesReabastecimiento", recomendacionesReabastecimiento);
        datos.put("resumenMensual", resumenMensual);
        datos.put("maxResumenMensual", maxResumenMensual);
        datos.put("resumenSemanal", resumenSemanal);
        datos.put("resumenMensualVentas", resumenMensualVentas);
        return datos;
    }

    private List<Pedido> filtrarPorFecha(List<Pedido> pedidos, LocalDate desde, LocalDate hasta) {
        if (desde == null && hasta == null) {
            return pedidos;
        }
        return pedidos.stream()
                .filter(p -> p.getFecha() != null)
                .filter(p -> desde == null || !p.getFecha().toLocalDate().isBefore(desde))
                .filter(p -> hasta == null || !p.getFecha().toLocalDate().isAfter(hasta))
                .toList();
    }

    private List<RecomendacionReabastecimiento> calcularReabastecimiento(List<VarianteProducto> variantes, Map<Long, Integer> unidadesUltimos30Dias) {
        List<RecomendacionReabastecimiento> resultado = new ArrayList<>();

        for (VarianteProducto variante : variantes) {
            int stock = variante.getStock() != null ? variante.getStock() : 0;
            int vendidas = unidadesUltimos30Dias.getOrDefault(variante.getId(), 0);
            double velocidadDiaria = vendidas / (double) VENTANA_REABASTECIMIENTO_DIAS;
            Double diasRestantes = velocidadDiaria > 0 ? stock / velocidadDiaria : null;

            boolean urgente = stock <= STOCK_BAJO_UMBRAL || (diasRestantes != null && diasRestantes <= DIAS_RESTANTES_URGENTE);
            boolean moderado = !urgente && diasRestantes != null && diasRestantes <= DIAS_RESTANTES_MODERADO;

            if (!urgente && !moderado) continue;

            resultado.add(new RecomendacionReabastecimiento(variante, vendidas, diasRestantes, urgente ? "URGENTE" : "MODERADO"));
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
