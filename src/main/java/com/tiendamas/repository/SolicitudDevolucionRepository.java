package com.tiendamas.repository;

import com.tiendamas.entity.SolicitudDevolucion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SolicitudDevolucionRepository extends JpaRepository<SolicitudDevolucion, Long> {
    List<SolicitudDevolucion> findAllByOrderByFechaDesc();
    List<SolicitudDevolucion> findByPedidoPersonaIdOrderByFechaDesc(Long personaId);
    List<SolicitudDevolucion> findByPedidoIdOrderByFechaDesc(Long pedidoId);
    boolean existsByDetallePedidoId(Long detallePedidoId);
}
