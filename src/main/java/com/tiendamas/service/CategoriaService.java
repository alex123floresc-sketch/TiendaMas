package com.tiendamas.service;

import com.tiendamas.entity.Categoria;
import com.tiendamas.repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    public List<Categoria> obtenerTodas() {
        return categoriaRepository.findAll();
    }

    /** Solo las categorías de primer nivel (sin padre), p. ej. Hombre, Mujer, Niños. */
    public List<Categoria> obtenerPrincipales() {
        return categoriaRepository.findByCategoriaPadreIsNullOrderByNombreAsc();
    }

    public Categoria obtenerPorId(Long id) {
        return categoriaRepository.findById(id).orElse(null);
    }

    public Categoria guardar(Categoria categoria) {
        return categoriaRepository.save(categoria);
    }

    /** Guarda la categoría asignándole (o quitándole) una categoría padre. */
    public Categoria guardar(Categoria categoria, Long categoriaPadreId) {
        if (categoriaPadreId == null) {
            categoria.setCategoriaPadre(null);
        } else {
            categoria.setCategoriaPadre(obtenerPorId(categoriaPadreId));
        }
        return categoriaRepository.save(categoria);
    }

    public void eliminar(Long id) {
        categoriaRepository.deleteById(id);
    }
}
