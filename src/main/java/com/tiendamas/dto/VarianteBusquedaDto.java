package com.tiendamas.dto;

import com.tiendamas.entity.VarianteProducto;

public class VarianteBusquedaDto {

    private final Long id;
    private final String talla;
    private final String color;
    private final String colorHex;
    private final Integer stock;
    private final String codigoBarras;

    public VarianteBusquedaDto(VarianteProducto v) {
        this.id = v.getId();
        this.talla = v.getTalla() != null ? v.getTalla().getEtiqueta() : null;
        this.color = v.getColor();
        this.colorHex = v.getColorHex();
        this.stock = v.getStock();
        this.codigoBarras = v.getCodigoBarras();
    }

    public Long getId() { return id; }
    public String getTalla() { return talla; }
    public String getColor() { return color; }
    public String getColorHex() { return colorHex; }
    public Integer getStock() { return stock; }
    public String getCodigoBarras() { return codigoBarras; }
}
