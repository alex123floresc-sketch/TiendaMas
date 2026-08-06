package com.tiendamas.dto;

public class DetalleForm {

    private Long varianteId;
    private Integer cantidad;

    public DetalleForm() {}

    public Long getVarianteId() { return varianteId; }
    public void setVarianteId(Long varianteId) { this.varianteId = varianteId; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
}
