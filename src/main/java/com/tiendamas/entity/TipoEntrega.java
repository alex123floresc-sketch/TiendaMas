package com.tiendamas.entity;

public enum TipoEntrega {
    RETIRO_TIENDA("Retiro en tienda"),
    DOMICILIO("Envío a domicilio");

    private final String etiqueta;

    TipoEntrega(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
