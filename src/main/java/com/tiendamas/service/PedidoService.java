package com.tiendamas.service;

import com.tiendamas.dto.ItemVenta;
import com.tiendamas.dto.PedidoForm;
import com.tiendamas.entity.CanalVenta;
import com.tiendamas.entity.EstadoPedido;
import com.tiendamas.entity.MetodoPago;
import com.tiendamas.entity.Pedido;
import com.tiendamas.entity.Producto;
import com.tiendamas.entity.TipoEntrega;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface PedidoService {
    List<Pedido> obtenerTodos();
    List<Pedido> obtenerPorCanal(CanalVenta canal);
    List<Pedido> obtenerPorPersona(Long personaId);
    Pedido obtenerPorId(Long id);

    List<Producto> obtenerMasVendidos(int limite);

    Map<Long, Integer> obtenerUnidadesVendidasDesde(LocalDateTime desde);

    Pedido crearPedido(PedidoForm form, String creadoPor);

    Pedido crearVenta(Long personaId, List<ItemVenta> items, CanalVenta canal,
                       MetodoPago metodoPago, String vendedorUsername,
                       TipoEntrega tipoEntrega, String direccionEntrega);

    void actualizarEstado(Long pedidoId, EstadoPedido nuevoEstado);

    void eliminar(Long id);
}
