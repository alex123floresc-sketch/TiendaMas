package com.tiendamas.entity;

public enum TipoSolicitudDevolucion {
    CAMBIO("Cambio por otra talla/color"),
    DEVOLUCION("Devolución con reembolso");

    private final String etiqueta;

    TipoSolicitudDevolucion(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() { return etiqueta; }
}
