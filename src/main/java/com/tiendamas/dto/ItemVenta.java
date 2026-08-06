package com.tiendamas.dto;

public class ItemVenta {

    private final Long varianteId;
    private final Integer cantidad;

    public ItemVenta(Long varianteId, Integer cantidad) {
        this.varianteId = varianteId;
        this.cantidad = cantidad;
    }

    public Long getVarianteId() { return varianteId; }
    public Integer getCantidad() { return cantidad; }
}
