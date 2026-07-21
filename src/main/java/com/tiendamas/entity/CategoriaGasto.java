package com.tiendamas.entity;

public enum CategoriaGasto {
    ALQUILER("Alquiler"),
    SERVICIOS("Servicios (luz, agua, internet)"),
    INSUMOS("Insumos y mercadería"),
    MARKETING("Marketing y publicidad"),
    OTROS("Otros");

    private final String etiqueta;

    CategoriaGasto(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
