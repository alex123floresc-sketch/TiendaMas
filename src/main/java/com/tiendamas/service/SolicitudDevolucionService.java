package com.tiendamas.service;

import com.tiendamas.entity.EstadoSolicitudDevolucion;
import com.tiendamas.entity.SolicitudDevolucion;
import com.tiendamas.entity.TipoSolicitudDevolucion;

import java.util.List;

public interface SolicitudDevolucionService {
    List<SolicitudDevolucion> obtenerTodas();
    List<SolicitudDevolucion> obtenerPorPersona(Long personaId);
    List<SolicitudDevolucion> obtenerPorPedido(Long pedidoId);
    SolicitudDevolucion obtenerPorId(Long id);

    /** Ítem entregado, dentro de la ventana de cambios, del cliente dueño del pedido, y sin una solicitud previa. */
    boolean puedeSolicitar(Long detallePedidoId, Long personaId);

    SolicitudDevolucion crear(Long detallePedidoId, Long personaId, TipoSolicitudDevolucion tipo, String motivo);

    SolicitudDevolucion resolver(Long solicitudId, EstadoSolicitudDevolucion nuevoEstado, String respuestaAdmin);
}
