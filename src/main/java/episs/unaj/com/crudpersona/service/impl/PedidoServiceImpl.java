package episs.unaj.com.crudpersona.service.impl;

import episs.unaj.com.crudpersona.dto.DetalleForm;
import episs.unaj.com.crudpersona.dto.ItemVenta;
import episs.unaj.com.crudpersona.dto.PedidoForm;
import episs.unaj.com.crudpersona.entity.CanalVenta;
import episs.unaj.com.crudpersona.entity.DetallePedido;
import episs.unaj.com.crudpersona.entity.MetodoPago;
import episs.unaj.com.crudpersona.entity.Pedido;
import episs.unaj.com.crudpersona.entity.Persona;
import episs.unaj.com.crudpersona.entity.Producto;
import episs.unaj.com.crudpersona.entity.TipoComprobante;
import episs.unaj.com.crudpersona.repository.PedidoRepository;
import episs.unaj.com.crudpersona.service.PedidoService;
import episs.unaj.com.crudpersona.service.PersonaService;
import episs.unaj.com.crudpersona.service.ProductoService;
import episs.unaj.com.crudpersona.service.SerieCorrelativoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class PedidoServiceImpl implements PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private PersonaService personaService;

    @Autowired
    private ProductoService productoService;

    @Autowired
    private SerieCorrelativoService serieCorrelativoService;

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
            items.add(new ItemVenta(df.getProductoId(), df.getCantidad()));
        }
        return crearVenta(form.getPersonaId(), items, CanalVenta.TIENDA_FISICA, form.getMetodoPago(), creadoPor);
    }

    @Override
    @Transactional
    public Pedido crearVenta(Long personaId, List<ItemVenta> items, CanalVenta canal,
                              MetodoPago metodoPago, String vendedorUsername) {
        Persona persona = personaService.obtenerPorId(personaId);
        if (persona == null) {
            throw new IllegalArgumentException("Cliente no encontrado");
        }

        Pedido pedido = new Pedido();
        pedido.setPersona(persona);
        pedido.setCanal(canal);
        pedido.setMetodoPago(metodoPago);
        pedido.setVendedorUsername(vendedorUsername);

        TipoComprobante tipoComprobante = persona.getTipoComprobanteSugerido();
        pedido.setTipoComprobante(tipoComprobante);
        pedido.setSerie(tipoComprobante.getSerie());
        pedido.setNumero(serieCorrelativoService.siguienteNumero(tipoComprobante));

        double total = 0.0;
        if (items != null) {
            for (ItemVenta iv : items) {
                if (iv == null || iv.getCantidad() == null || iv.getCantidad() <= 0
                        || iv.getProductoId() == null) {
                    continue;
                }
                Producto producto = productoService.obtenerPorId(iv.getProductoId());
                if (producto == null) continue;

                DetallePedido detalle = new DetallePedido();
                detalle.setProducto(producto);
                detalle.setCantidad(iv.getCantidad());
                detalle.setPrecioUnitario(producto.getPrecio());
                detalle.setSubtotal(producto.getPrecio() * iv.getCantidad());

                pedido.agregarDetalle(detalle);
                total += detalle.getSubtotal();
            }
        }

        if (pedido.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("El carrito está vacío");
        }

        pedido.setTotal(total);
        return pedidoRepository.save(pedido);
    }

    @Override
    public void eliminar(Long id) {
        pedidoRepository.deleteById(id);
    }
}
