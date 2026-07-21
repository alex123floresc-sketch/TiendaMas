package com.tiendamas.service;

import com.tiendamas.dto.ItemVenta;
import com.tiendamas.dto.PedidoForm;
import com.tiendamas.entity.CanalVenta;
import com.tiendamas.entity.MetodoPago;
import com.tiendamas.entity.Pedido;
import com.tiendamas.entity.Producto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface PedidoService {
    List<Pedido> obtenerTodos();
    List<Pedido> obtenerPorCanal(CanalVenta canal);
    List<Pedido> obtenerPorPersona(Long personaId);
    Pedido obtenerPorId(Long id);

    /** Productos más vendidos (por unidades), fuente única reutilizada por reportes y por las recomendaciones de la tienda. */
    List<Producto> obtenerMasVendidos(int limite);

    /** Unidades vendidas por producto desde una fecha dada (para calcular velocidad de venta / reabastecimiento). */
    Map<Long, Integer> obtenerUnidadesVendidasDesde(LocalDateTime desde);

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
