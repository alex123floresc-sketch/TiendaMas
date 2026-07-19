package episs.unaj.com.crudpersona.service;

import episs.unaj.com.crudpersona.dto.ItemVenta;
import episs.unaj.com.crudpersona.dto.PedidoForm;
import episs.unaj.com.crudpersona.entity.CanalVenta;
import episs.unaj.com.crudpersona.entity.MetodoPago;
import episs.unaj.com.crudpersona.entity.Pedido;

import java.util.List;

public interface PedidoService {
    List<Pedido> obtenerTodos();
    List<Pedido> obtenerPorCanal(CanalVenta canal);
    List<Pedido> obtenerPorPersona(Long personaId);
    Pedido obtenerPorId(Long id);

    /** Alta manual desde el panel de administrador. */
    Pedido crearPedido(PedidoForm form, String creadoPor);

    /**
     * Lógica de venta compartida por el panel de admin, el POS del vendedor
     * y el checkout de la tienda en línea: calcula totales, determina
     * boleta/factura según el cliente y asigna el número correlativo.
     */
    Pedido crearVenta(Long personaId, List<ItemVenta> items, CanalVenta canal,
                       MetodoPago metodoPago, String vendedorUsername);

    void eliminar(Long id);
}
