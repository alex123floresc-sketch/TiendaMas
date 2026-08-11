package com.tiendamas.entity;

public enum TamanoTarjetaCategoria {
    NORMAL("Normal (cuadrada)"),
    ANCHO("Ancha — para fotos horizontales (ej. zapatillas, accesorios)"),
    ALTO("Alta — para fotos verticales (ej. camisas, vestidos)"),
    COMPACTO("Compacta — más chica que el resto");

    private final String etiqueta;

    TamanoTarjetaCategoria(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
