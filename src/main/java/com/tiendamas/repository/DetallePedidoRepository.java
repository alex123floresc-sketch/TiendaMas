package com.tiendamas.repository;

import com.tiendamas.entity.DetallePedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Long> {

    @Query("SELECT d.variante.id AS varianteId, COALESCE(SUM(d.cantidad), 0) AS unidades " +
           "FROM DetallePedido d WHERE d.pedido.fecha >= :desde AND d.variante IS NOT NULL " +
           "GROUP BY d.variante.id")
    List<Object[]> sumarCantidadPorVarianteDesde(@Param("desde") LocalDateTime desde);
}
