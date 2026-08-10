package com.tiendamas.service.impl;

import com.tiendamas.dto.DetalleForm;
import com.tiendamas.dto.ItemVenta;
import com.tiendamas.dto.PedidoForm;
import com.tiendamas.dto.ResultadoCupon;
import com.tiendamas.entity.CanalVenta;
import com.tiendamas.entity.Cupon;
import com.tiendamas.entity.DetallePedido;
import com.tiendamas.entity.EstadoPedido;
import com.tiendamas.entity.MetodoPago;
import com.tiendamas.entity.Pedido;
import com.tiendamas.entity.Persona;
import com.tiendamas.entity.Producto;
import com.tiendamas.entity.TipoComprobante;
import com.tiendamas.entity.TipoEntrega;
import com.tiendamas.entity.VarianteProducto;
import com.tiendamas.repository.DetallePedidoRepository;
import com.tiendamas.repository.PedidoRepository;
import com.tiendamas.service.EmailService;
import com.tiendamas.service.PedidoService;
import com.tiendamas.service.PersonaService;
import com.tiendamas.service.ProductoService;
import com.tiendamas.service.SerieCorrelativoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PedidoServiceImpl implements PedidoService {

    private static final Logger log = LoggerFactory.getLogger(PedidoServiceImpl.class);

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private DetallePedidoRepository detallePedidoRepository;

    @Autowired
    private PersonaService personaService;

    @Autowired
    private ProductoService productoService;

    @Autowired
    private SerieCorrelativoService serieCorrelativoService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ComprobantePdfService comprobantePdfService;

    @Autowired
    private EnvioService envioService;

    @Autowired
    private com.tiendamas.service.CuponService cuponService;

    @Autowired
    private com.tiendamas.service.FidelidadService fidelidadService;

    @Override
    public List<Pedido> obtenerTodos() {
        return pedidoRepository.findAll();
    }

    @Override
    public List<Pedido> obtenerPorCanal(CanalVenta canal) {
        return pedidoRepository.findByCanalOrderByFechaDesc(canal);
    }

    @Override
    public List<Pedido> obtenerPorPersona(Long personaId) {
        return pedidoRepository.findByPersonaIdOrderByFechaDesc(personaId);
    }

    @Override
    public Pedido obtenerPorId(Long id) {
        return pedidoRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public Pedido crearPedido(PedidoForm form, String creadoPor) {
        List<ItemVenta> items = new ArrayList<>();
        for (DetalleForm df : form.getDetalles()) {
            if (df == null) continue;
            items.add(new ItemVenta(df.getVarianteId(), df.getCantidad()));
        }
        return crearVenta(form.getPersonaId(), items, CanalVenta.TIENDA_FISICA, form.getMetodoPago(), creadoPor,
                TipoEntrega.RETIRO_TIENDA, null);
    }

    @Override
    @Transactional
    public Pedido crearVenta(Long personaId, List<ItemVenta> items, CanalVenta canal,
                              MetodoPago metodoPago, String vendedorUsername,
                              TipoEntrega tipoEntrega, String direccionEntrega) {
        return crearVenta(personaId, items, canal, metodoPago, vendedorUsername, tipoEntrega, direccionEntrega, null);
    }

    @Override
    @Transactional
    public Pedido crearVenta(Long personaId, List<ItemVenta> items, CanalVenta canal,
                              MetodoPago metodoPago, String vendedorUsername,
                              TipoEntrega tipoEntrega, String direccionEntrega, String codigoCupon) {
        Persona persona = personaService.obtenerPorId(personaId);
        if (persona == null) {
            throw new IllegalArgumentException("Cliente no encontrado");
        }

        Pedido pedido = new Pedido();
        pedido.setPersona(persona);
        pedido.setCanal(canal);
        pedido.setMetodoPago(metodoPago);
        pedido.setVendedorUsername(vendedorUsername);
        pedido.setTipoEntrega(tipoEntrega != null ? tipoEntrega : TipoEntrega.RETIRO_TIENDA);
        pedido.setDireccionEntrega(pedido.getTipoEntrega() == TipoEntrega.DOMICILIO ? direccionEntrega : null);
        // Una venta en tienda física se entrega en el acto; una venta online queda pendiente hasta su despacho/retiro.
        pedido.setEstado(canal == CanalVenta.TIENDA_FISICA ? EstadoPedido.ENTREGADO : EstadoPedido.PENDIENTE);

        TipoComprobante tipoComprobante = persona.getTipoComprobanteSugerido();
        pedido.setTipoComprobante(tipoComprobante);
        pedido.setSerie(tipoComprobante.getSerie());
        pedido.setNumero(serieCorrelativoService.siguienteNumero(tipoComprobante));

        double total = 0.0;
        if (items != null) {
            for (ItemVenta iv : items) {
                if (iv == null || iv.getCantidad() == null || iv.getCantidad() <= 0
                        || iv.getVarianteId() == null) {
                    continue;
                }
                VarianteProducto variante = productoService.obtenerVariante(iv.getVarianteId());
                if (variante == null) continue;
                Producto producto = variante.getProducto();

                int stockDisponible = variante.getStock() != null ? variante.getStock() : 0;
                if (iv.getCantidad() > stockDisponible) {
                    throw new IllegalArgumentException("Stock insuficiente para " + producto.getNombre()
                            + " (" + variante.getEtiqueta() + "): quedan " + stockDisponible + " unidades");
                }

                DetallePedido detalle = new DetallePedido();
                detalle.setProducto(producto);
                detalle.setVariante(variante);
                detalle.setTalla(variante.getTalla() != null ? variante.getTalla().getEtiqueta() : null);
                detalle.setColor(variante.getColor());
                detalle.setCantidad(iv.getCantidad());
                detalle.setPrecioUnitario(producto.getPrecio());
                detalle.setSubtotal(producto.getPrecio() * iv.getCantidad());

                pedido.agregarDetalle(detalle);
                total += detalle.getSubtotal();

                variante.setStock(stockDisponible - iv.getCantidad());
                productoService.guardarVariante(variante);
            }
        }

        if (pedido.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("El carrito está vacío");
        }

        Cupon cupon = null;
        double descuento = 0.0;
        if (codigoCupon != null && !codigoCupon.isBlank()) {
            ResultadoCupon resultado = cuponService.validar(codigoCupon, total, persona.getId());
            if (!resultado.isValido()) {
                throw new IllegalArgumentException(resultado.getError());
            }
            cupon = resultado.getCupon();
            descuento = cupon.calcularDescuento(total);
        }

        double costoEnvio = envioService.calcularCosto(total, pedido.getTipoEntrega());
        pedido.setCostoEnvio(costoEnvio);
        pedido.setCupon(cupon);
        pedido.setDescuento(descuento);
        pedido.setTotal(total - descuento + costoEnvio);
        Pedido guardado = pedidoRepository.save(pedido);

        if (cupon != null) {
            cuponService.registrarUso(cupon);
        }
        fidelidadService.registrarCompra(persona, guardado.getSubtotal());

        byte[] pdfComprobante = null;
        try {
            pdfComprobante = comprobantePdfService.generarPdf(guardado);
        } catch (Exception e) {
            log.warn("No se pudo generar el PDF del comprobante para el pedido {}, se enviara el correo sin adjunto: {}",
                    guardado.getId(), e.getMessage());
        }
        emailService.enviarConfirmacionPedido(guardado, pdfComprobante);
        return guardado;
    }

    @Override
    public List<Producto> obtenerMasVendidos(int limite) {
        Map<Long, Integer> unidadesPorProductoId = new LinkedHashMap<>();
        Map<Long, Producto> productoPorId = new LinkedHashMap<>();

        for (Pedido pedido : pedidoRepository.findAll()) {
            for (DetallePedido detalle : pedido.getDetalles()) {
                Producto producto = detalle.getProducto();
                if (producto == null) continue;
                int cantidad = detalle.getCantidad() != null ? detalle.getCantidad() : 0;
                unidadesPorProductoId.merge(producto.getId(), cantidad, Integer::sum);
                productoPorId.putIfAbsent(producto.getId(), producto);
            }
        }

        return unidadesPorProductoId.entrySet().stream()
                .sorted((a, b) -> b.getValue() - a.getValue())
                .limit(limite)
                .map(entry -> productoPorId.get(entry.getKey()))
                .toList();
    }

    @Override
    public Map<Long, Integer> obtenerUnidadesVendidasDesde(LocalDateTime desde) {
        Map<Long, Integer> unidadesPorVarianteId = new HashMap<>();
        for (Object[] fila : detallePedidoRepository.sumarCantidadPorVarianteDesde(desde)) {
            Long varianteId = (Long) fila[0];
            Number unidades = (Number) fila[1];
            unidadesPorVarianteId.put(varianteId, unidades.intValue());
        }
        return unidadesPorVarianteId;
    }

    @Override
    @Transactional
    public void actualizarEstado(Long pedidoId, EstadoPedido nuevoEstado) {
        Pedido pedido = pedidoRepository.findById(pedidoId).orElse(null);
        if (pedido == null) {
            throw new IllegalArgumentException("Pedido no encontrado");
        }
        pedido.setEstado(nuevoEstado);
        Pedido guardado = pedidoRepository.save(pedido);
        emailService.enviarCambioEstadoPedido(guardado);
    }

    @Override
    public void eliminar(Long id) {
        pedidoRepository.deleteById(id);
    }
}
