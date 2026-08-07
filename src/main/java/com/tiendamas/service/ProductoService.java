package com.tiendamas.service;

import com.tiendamas.dto.ProductoForm;
import com.tiendamas.dto.VarianteForm;
import com.tiendamas.entity.Categoria;
import com.tiendamas.entity.ImagenProducto;
import com.tiendamas.entity.Producto;
import com.tiendamas.entity.VarianteProducto;
import com.tiendamas.repository.ProductoRepository;
import com.tiendamas.repository.VarianteProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private VarianteProductoRepository varianteProductoRepository;

    private static final int RELACIONADOS_LIMITE_DEFECTO = 4;

    public List<Producto> obtenerTodos() {
        return productoRepository.findAll();
    }

    public Producto obtenerPorId(Long id) {
        return productoRepository.findById(id).orElse(null);
    }

    public VarianteProducto obtenerVariante(Long varianteId) {
        return varianteId == null ? null : varianteProductoRepository.findById(varianteId).orElse(null);
    }

    public VarianteProducto obtenerVariantePorCodigoBarras(String codigoBarras) {
        if (codigoBarras == null || codigoBarras.isBlank()) return null;
        return varianteProductoRepository.findByCodigoBarras(codigoBarras.trim()).orElse(null);
    }

    public VarianteProducto guardarVariante(VarianteProducto variante) {
        return varianteProductoRepository.save(variante);
    }

    public List<Producto> buscar(String query, Long categoriaId) {
        String q = (query == null || query.isBlank()) ? null : query.trim().toLowerCase();
        return productoRepository.buscar(q, categoriaId);
    }

    public Producto guardarConVariantes(ProductoForm form, Categoria categoria, String imagenUrl) {
        Producto producto = new Producto(form.getNombre(), form.getDescripcion(), form.getPrecio(), categoria);
        producto.setMarca(form.getMarca());
        producto.setImagenUrl(imagenUrl);
        for (VarianteForm vf : form.getVariantes()) {
            if (vf == null || vf.getTalla() == null) continue;
            producto.agregarVariante(nuevaVariante(vf));
        }
        return productoRepository.save(producto);
    }

    public Producto actualizarConVariantes(Long id, ProductoForm form, Categoria categoria, String imagenUrl) {
        Producto producto = productoRepository.findById(id).orElse(null);
        if (producto == null) return null;

        producto.setNombre(form.getNombre());
        producto.setDescripcion(form.getDescripcion());
        producto.setPrecio(form.getPrecio());
        producto.setMarca(form.getMarca());
        producto.setCategoria(categoria);
        producto.setImagenUrl(imagenUrl);

        Map<Long, VarianteProducto> existentesPorId = producto.getVariantes().stream()
                .filter(v -> v.getId() != null)
                .collect(Collectors.toMap(VarianteProducto::getId, v -> v));

        Set<Long> idsConservados = new HashSet<>();
        for (VarianteForm vf : form.getVariantes()) {
            if (vf == null || vf.getTalla() == null) continue;
            VarianteProducto existente = vf.getId() != null ? existentesPorId.get(vf.getId()) : null;
            if (existente != null) {
                existente.setTalla(vf.getTalla());
                existente.setColor(vf.getColor());
                existente.setColorHex(vf.getColorHex());
                existente.setStock(vf.getStock() != null ? vf.getStock() : 0);
                existente.setCodigoBarras(blankToNull(vf.getCodigoBarras()));
                idsConservados.add(existente.getId());
            } else {
                producto.agregarVariante(nuevaVariante(vf));
            }
        }
        producto.getVariantes().removeIf(v -> v.getId() != null && !idsConservados.contains(v.getId()));

        return productoRepository.save(producto);
    }

    public void agregarImagenes(Long productoId, List<String> urls) {
        if (urls == null || urls.isEmpty()) return;
        Producto producto = productoRepository.findById(productoId).orElse(null);
        if (producto == null) return;

        int siguienteOrden = producto.getImagenes().size();
        for (String url : urls) {
            if (url == null || url.isBlank()) continue;
            producto.agregarImagen(new ImagenProducto(url, producto, siguienteOrden++));
        }
        productoRepository.save(producto);
    }

    /** Elimina una imagen de la galería y devuelve su URL (para borrar el archivo del disco), o null si no existía. */
    public String eliminarImagen(Long productoId, Long imagenId) {
        Producto producto = productoRepository.findById(productoId).orElse(null);
        if (producto == null) return null;

        String[] urlEliminada = new String[1];
        producto.getImagenes().removeIf(img -> {
            if (img.getId().equals(imagenId)) {
                urlEliminada[0] = img.getUrl();
                return true;
            }
            return false;
        });
        if (urlEliminada[0] != null) {
            productoRepository.save(producto);
        }
        return urlEliminada[0];
    }

    private VarianteProducto nuevaVariante(VarianteForm vf) {
        return new VarianteProducto(null, vf.getTalla(), vf.getColor(), vf.getColorHex(),
                vf.getStock() != null ? vf.getStock() : 0, blankToNull(vf.getCodigoBarras()));
    }

    private String blankToNull(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }

    public List<Producto> obtenerRelacionados(Producto producto, int limite) {
        if (producto == null || producto.getCategoria() == null) {
            return List.of();
        }
        return productoRepository.findByCategoriaIdAndIdNot(producto.getCategoria().getId(), producto.getId()).stream()
                .sorted(Comparator.comparing(Producto::tieneStock).reversed())
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
