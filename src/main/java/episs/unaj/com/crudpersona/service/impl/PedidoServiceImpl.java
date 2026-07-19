package episs.unaj.com.crudpersona.service.impl;

import episs.unaj.com.crudpersona.dto.DetalleForm;
import episs.unaj.com.crudpersona.dto.PedidoForm;
import episs.unaj.com.crudpersona.entity.DetallePedido;
import episs.unaj.com.crudpersona.entity.Pedido;
import episs.unaj.com.crudpersona.entity.Producto;
import episs.unaj.com.crudpersona.repository.PedidoRepository;
import episs.unaj.com.crudpersona.service.PedidoService;
import episs.unaj.com.crudpersona.service.PersonaService;
import episs.unaj.com.crudpersona.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PedidoServiceImpl implements PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private PersonaService personaService;

    @Autowired
    private ProductoService productoService;

    @Override
    public List<Pedido> obtenerTodos() {
        return pedidoRepository.findAll();
    }

    @Override
    public Pedido obtenerPorId(Long id) {
        return pedidoRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public Pedido crearPedido(PedidoForm form) {
        Pedido pedido = new Pedido();
        pedido.setPersona(personaService.obtenerPorId(form.getPersonaId()));

        double total = 0.0;
        for (DetalleForm df : form.getDetalles()) {
            if (df == null || df.getCantidad() == null || df.getCantidad() <= 0
                    || df.getProductoId() == null) {
                continue;
            }
            Producto producto = productoService.obtenerPorId(df.getProductoId());
            if (producto == null) continue;

            DetallePedido detalle = new DetallePedido();
            detalle.setProducto(producto);
            detalle.setCantidad(df.getCantidad());
            detalle.setPrecioUnitario(producto.getPrecio());
            detalle.setSubtotal(producto.getPrecio() * df.getCantidad());

            pedido.agregarDetalle(detalle);
            total += detalle.getSubtotal();
        }

        pedido.setTotal(total);
        return pedidoRepository.save(pedido);
    }

    @Override
    public void eliminar(Long id) {
        pedidoRepository.deleteById(id);
    }
}

