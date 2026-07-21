package com.tiendamas.entity;

public enum MetodoPago {
    EFECTIVO("Efectivo"),
    TARJETA("Tarjeta de crédito/débito"),
    YAPE_PLIN("Yape / Plin"),
    TRANSFERENCIA("Transferencia bancaria");

    private final String etiqueta;

    MetodoPago(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
