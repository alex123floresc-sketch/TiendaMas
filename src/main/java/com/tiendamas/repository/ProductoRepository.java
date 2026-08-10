package com.tiendamas.repository;

import com.tiendamas.entity.Producto;
import com.tiendamas.entity.Talla;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    List<Producto> findByCategoriaIdAndIdNot(Long categoriaId, Long id);

    List<Producto> findTop12ByOrderByIdDesc();

    @Query("SELECT DISTINCT p FROM Producto p LEFT JOIN p.variantes v WHERE " +
           "(:categoriaId IS NULL OR p.categoria.id = :categoriaId " +
           "     OR (p.categoria.categoriaPadre IS NOT NULL AND p.categoria.categoriaPadre.id = :categoriaId)) AND " +
           "(:q IS NULL OR LOWER(p.nombre) LIKE CONCAT('%', :q, '%') " +
           "OR LOWER(COALESCE(p.marca, '')) LIKE CONCAT('%', :q, '%') " +
           "OR LOWER(COALESCE(v.codigoBarras, '')) LIKE CONCAT('%', :q, '%')) AND " +
           "(:talla IS NULL OR v.talla = :talla) AND " +
           "(:precioMin IS NULL OR p.precio >= :precioMin) AND " +
           "(:precioMax IS NULL OR p.precio <= :precioMax) " +
           "ORDER BY p.nombre ASC")
    List<Producto> buscar(@Param("q") String q, @Param("categoriaId") Long categoriaId,
                           @Param("talla") Talla talla, @Param("precioMin") Double precioMin,
                           @Param("precioMax") Double precioMax);
}
