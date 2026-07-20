package episs.unaj.com.crudpersona.service;

import episs.unaj.com.crudpersona.entity.Producto;
import episs.unaj.com.crudpersona.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    private static final int RELACIONADOS_LIMITE_DEFECTO = 4;

    public List<Producto> obtenerTodos() {
        return productoRepository.findAll();
    }

    public Producto obtenerPorId(Long id) {
        return productoRepository.findById(id).orElse(null);
    }

    public Producto obtenerPorCodigoBarras(String codigoBarras) {
        if (codigoBarras == null || codigoBarras.isBlank()) return null;
        return productoRepository.findByCodigoBarras(codigoBarras.trim()).orElse(null);
    }

    public Producto guardar(Producto producto) {
        return productoRepository.save(producto);
    }

    public Producto actualizar(Long id, Producto producto) {
        Producto p = productoRepository.findById(id).orElse(null);
        if (p != null) {
            p.setNombre(producto.getNombre());
            p.setDescripcion(producto.getDescripcion());
            p.setPrecio(producto.getPrecio());
            p.setStock(producto.getStock());
            p.setCategoria(producto.getCategoria());
            p.setCodigoBarras(producto.getCodigoBarras());
            p.setMarca(producto.getMarca());
            p.setUnidadMedida(producto.getUnidadMedida());
            return productoRepository.save(p);
        }
        return null;
    }

    /** Productos de la misma categoría (para "productos relacionados" en la tienda). */
    public List<Producto> obtenerRelacionados(Producto producto, int limite) {
        if (producto == null || producto.getCategoria() == null) {
            return List.of();
        }
        return productoRepository.findByCategoriaIdAndIdNot(producto.getCategoria().getId(), producto.getId()).stream()
                .sorted(Comparator.comparing((Producto p) -> p.getStock() != null && p.getStock() > 0).reversed())
                .limit(limite)
                .toList();
    }

    public List<Producto> obtenerRelacionados(Producto producto) {
        return obtenerRelacionados(producto, RELACIONADOS_LIMITE_DEFECTO);
    }

    public void eliminar(Long id) {
        productoRepository.deleteById(id);
    }
}
