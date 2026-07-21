package com.tiendamas.repository;

import com.tiendamas.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    Optional<Producto> findByCodigoBarras(String codigoBarras);
    List<Producto> findByCategoriaIdAndIdNot(Long categoriaId, Long id);
}
