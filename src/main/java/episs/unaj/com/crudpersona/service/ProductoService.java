package episs.unaj.com.crudpersona.service;

import episs.unaj.com.crudpersona.entity.Producto;
import episs.unaj.com.crudpersona.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

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
            return productoRepository.save(p);
        }
        return null;
    }

    public void eliminar(Long id) {
        productoRepository.deleteById(id);
    }
}
