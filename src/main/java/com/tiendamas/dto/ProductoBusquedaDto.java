package com.tiendamas.dto;

import com.tiendamas.entity.Producto;

public class ProductoBusquedaDto {

    private final Long id;
    private final String nombre;
    private final String marca;
    private final Double precio;
    private final Integer stock;
    private final String imagenUrl;
    private final String codigoBarras;
    private final Long categoriaId;
    private final String categoriaNombre;

    public ProductoBusquedaDto(Producto p) {
        this.id = p.getId();
        this.nombre = p.getNombre();
        this.marca = p.getMarca();
        this.precio = p.getPrecio();
        this.stock = p.getStock();
        this.imagenUrl = p.getImagenUrl();
        this.codigoBarras = p.getCodigoBarras();
        this.categoriaId = p.getCategoria() != null ? p.getCategoria().getId() : null;
        this.categoriaNombre = p.getCategoria() != null ? p.getCategoria().getNombre() : null;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getMarca() { return marca; }
    public Double getPrecio() { return precio; }
    public Integer getStock() { return stock; }
    public String getImagenUrl() { return imagenUrl; }
    public String getCodigoBarras() { return codigoBarras; }
    public Long getCategoriaId() { return categoriaId; }
    public String getCategoriaNombre() { return categoriaNombre; }
}
