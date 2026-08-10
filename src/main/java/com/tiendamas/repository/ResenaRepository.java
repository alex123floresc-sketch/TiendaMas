package com.tiendamas.repository;

import com.tiendamas.entity.Resena;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ResenaRepository extends JpaRepository<Resena, Long> {
    List<Resena> findByProductoIdOrderByFechaDesc(Long productoId);
    boolean existsByProductoIdAndPersonaId(Long productoId, Long personaId);

    @Query("select avg(r.calificacion) from Resena r where r.producto.id = :productoId")
    Double promedioCalificacion(@Param("productoId") Long productoId);
}
