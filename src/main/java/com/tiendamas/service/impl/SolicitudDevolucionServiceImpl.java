package com.tiendamas.service.impl;

import com.tiendamas.entity.DetallePedido;
import com.tiendamas.entity.EstadoPedido;
import com.tiendamas.entity.EstadoSolicitudDevolucion;
import com.tiendamas.entity.Pedido;
import com.tiendamas.entity.SolicitudDevolucion;
import com.tiendamas.entity.TipoSolicitudDevolucion;
import com.tiendamas.repository.DetallePedidoRepository;
import com.tiendamas.repository.SolicitudDevolucionRepository;
import com.tiendamas.service.SolicitudDevolucionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SolicitudDevolucionServiceImpl implements SolicitudDevolucionService {

    private static final int DIAS_VENTANA_CAMBIO = 15;

    @Autowired
    private SolicitudDevolucionRepository solicitudDevolucionRepository;

    @Autowired
    private DetallePedidoRepository detallePedidoRepository;

    @Override
    public List<SolicitudDevolucion> obtenerTodas() {
        return solicitudDevolucionRepository.findAllByOrderByFechaDesc();
    }

    @Override
    public List<SolicitudDevolucion> obtenerPorPersona(Long personaId) {
        return solicitudDevolucionRepository.findByPedidoPersonaIdOrderByFechaDesc(personaId);
    }

    @Override
    public List<SolicitudDevolucion> obtenerPorPedido(Long pedidoId) {
        return solicitudDevolucionRepository.findByPedidoIdOrderByFechaDesc(pedidoId);
    }

    @Override
    public SolicitudDevolucion obtenerPorId(Long id) {
        return id == null ? null : solicitudDevolucionRepository.findById(id).orElse(null);
    }

    @Override
    public boolean puedeSolicitar(Long detallePedidoId, Long personaId) {
        return validarSolicitud(detallePedidoId, personaId) == null;
    }

    /** Devuelve el mensaje de error si no es elegible, o null si puede solicitar. */
    private String validarSolicitud(Long detallePedidoId, Long personaId) {
        if (detallePedidoId == null || personaId == null) {
            return "Ítem no encontrado";
        }
        DetallePedido detalle = detallePedidoRepository.findById(detallePedidoId).orElse(null);
        if (detalle == null || detalle.getPedido() == null) {
            return "Ítem no encontrado";
        }
        Pedido pedido = detalle.getPedido();
        if (pedido.getPersona() == null || !pedido.getPersona().getId().equals(personaId)) {
            return "Este pedido no te pertenece";
        }
        if (pedido.getEstado() != EstadoPedido.ENTREGADO) {
            return "Solo se pueden solicitar cambios o devoluciones de pedidos ya entregados";
        }
        if (pedido.getFecha() == null || pedido.getFecha().plusDays(DIAS_VENTANA_CAMBIO).isBefore(LocalDateTime.now())) {
            return "Ya pasaron los " + DIAS_VENTANA_CAMBIO + " días del plazo para cambios y devoluciones";
        }
        if (solicitudDevolucionRepository.existsByDetallePedidoId(detallePedidoId)) {
            return "Ya existe una solicitud para este producto";
        }
        return null;
    }

    @Override
    @Transactional
    public SolicitudDevolucion crear(Long detallePedidoId, Long personaId, TipoSolicitudDevolucion tipo, String motivo) {
        String error = validarSolicitud(detallePedidoId, personaId);
        if (error != null) {
            throw new IllegalArgumentException(error);
        }
        if (tipo == null) {
            throw new IllegalArgumentException("Indica si querés un cambio o una devolución");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("Contanos el motivo de tu solicitud");
        }

        DetallePedido detalle = detallePedidoRepository.findById(detallePedidoId).orElseThrow();
        SolicitudDevolucion solicitud = new SolicitudDevolucion();
        solicitud.setPedido(detalle.getPedido());
        solicitud.setDetallePedido(detalle);
        solicitud.setTipo(tipo);
        solicitud.setMotivo(motivo.trim());
        return solicitudDevolucionRepository.save(solicitud);
    }

    @Override
    @Transactional
    public SolicitudDevolucion resolver(Long solicitudId, EstadoSolicitudDevolucion nuevoEstado, String respuestaAdmin) {
        SolicitudDevolucion solicitud = obtenerPorId(solicitudId);
        if (solicitud == null) {
            throw new IllegalArgumentException("Solicitud no encontrada");
        }
        if (nuevoEstado == null) {
            throw new IllegalArgumentException("Indica el nuevo estado de la solicitud");
        }
        solicitud.setEstado(nuevoEstado);
        solicitud.setRespuestaAdmin(respuestaAdmin != null && !respuestaAdmin.isBlank() ? respuestaAdmin.trim() : null);
        solicitud.setFechaResolucion(LocalDateTime.now());
        return solicitudDevolucionRepository.save(solicitud);
    }
}
