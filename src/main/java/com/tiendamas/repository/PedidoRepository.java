package com.tiendamas.repository;

import com.tiendamas.entity.CanalVenta;
import com.tiendamas.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByCanalOrderByFechaDesc(CanalVenta canal);
    List<Pedido> findByPersonaIdOrderByFechaDesc(Long personaId);
}
