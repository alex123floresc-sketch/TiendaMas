package com.tiendamas.service;

import com.tiendamas.entity.Resena;

import java.util.List;

public interface ResenaService {
    List<Resena> obtenerPorProducto(Long productoId);
    double promedioPorProducto(Long productoId);

    /** El cliente compró este producto y todavía no dejó una reseña sobre él. */
    boolean puedeResenar(Long productoId, Long personaId);

    boolean yaReseno(Long productoId, Long personaId);

    Resena crear(Long productoId, Long personaId, Integer calificacion, String comentario);
}
