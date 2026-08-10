package com.tiendamas.web;

import com.tiendamas.entity.VarianteProducto;

public class CarritoItem {

    private final Long varianteId;
    private final Long productoId;
    private final String nombre;
    private final String talla;
    private final String color;
    private final String colorHex;
    private final String imagenUrl;
    private final double precioUnitario;
    private final int stockDisponible;
    private int cantidad;

    public CarritoItem(VarianteProducto variante, int cantidad) {
        this.varianteId = variante.getId();
        this.productoId = variante.getProducto().getId();
        this.nombre = variante.getProducto().getNombre();
        this.talla = variante.getTalla() != null ? variante.getTalla().getEtiqueta() : null;
        this.color = variante.getColor();
        this.colorHex = variante.getColorHex();
        this.imagenUrl = variante.getProducto().getImagenPrincipal();
        this.precioUnitario = variante.getProducto().getPrecio();
        this.stockDisponible = variante.getStock() != null ? variante.getStock() : 0;
        this.cantidad = cantidad;
    }

    public Long getVarianteId() { return varianteId; }
    public Long getProductoId() { return productoId; }
    public String getNombre() { return nombre; }
    public String getTalla() { return talla; }
    public String getColor() { return color; }
    public String getColorHex() { return colorHex; }
    public String getImagenUrl() { return imagenUrl; }
    public double getPrecioUnitario() { return precioUnitario; }
    public int getStockDisponible() { return stockDisponible; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public double getSubtotal() { return precioUnitario * cantidad; }
}
