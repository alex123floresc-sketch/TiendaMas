package com.tiendamas.entity;

public enum TipoDescuentoCupon {
    PORCENTAJE("% de descuento"),
    MONTO_FIJO("S/ de descuento");

    private final String etiqueta;

    TipoDescuentoCupon(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() { return etiqueta; }
}
