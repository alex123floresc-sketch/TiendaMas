package com.tiendamas.entity;

public enum EstadoSolicitudDevolucion {
    PENDIENTE("Pendiente de revisión"),
    APROBADA("Aprobada"),
    RECHAZADA("Rechazada"),
    COMPLETADA("Completada");

    private final String etiqueta;

    EstadoSolicitudDevolucion(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() { return etiqueta; }
}
