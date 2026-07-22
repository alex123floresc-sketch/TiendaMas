package com.tiendamas.dto;

import com.tiendamas.entity.Categoria;

public class CategoriaResumenDto {

    private final Long id;
    private final String nombre;

    public CategoriaResumenDto(Categoria c) {
        this.id = c.getId();
        this.nombre = c.getNombre();
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
}
