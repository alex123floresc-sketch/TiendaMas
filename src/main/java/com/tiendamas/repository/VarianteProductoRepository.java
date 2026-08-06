package com.tiendamas.repository;

import com.tiendamas.entity.VarianteProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VarianteProductoRepository extends JpaRepository<VarianteProducto, Long> {
    Optional<VarianteProducto> findByCodigoBarras(String codigoBarras);
    List<VarianteProducto> findByProductoId(Long productoId);
}
