package com.tiendamas.dto;

/** Fila liviana para el autocompletado del buscador de la tienda. */
public record SugerenciaProducto(Long id, String nombre, String imagenUrl, Double precio,
                                  String categoria, String url) {
}
