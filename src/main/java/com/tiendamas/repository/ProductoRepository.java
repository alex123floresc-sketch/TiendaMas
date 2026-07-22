package com.tiendamas.repository;

import com.tiendamas.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    Optional<Producto> findByCodigoBarras(String codigoBarras);
    List<Producto> findByCategoriaIdAndIdNot(Long categoriaId, Long id);

    @Query("SELECT p FROM Producto p WHERE " +
           "(:categoriaId IS NULL OR p.categoria.id = :categoriaId) AND " +
           "(:q IS NULL OR LOWER(p.nombre) LIKE CONCAT('%', :q, '%') " +
           "OR LOWER(COALESCE(p.marca, '')) LIKE CONCAT('%', :q, '%') " +
           "OR LOWER(COALESCE(p.codigoBarras, '')) LIKE CONCAT('%', :q, '%')) " +
           "ORDER BY p.nombre ASC")
    List<Producto> buscar(@Param("q") String q, @Param("categoriaId") Long categoriaId);
}
