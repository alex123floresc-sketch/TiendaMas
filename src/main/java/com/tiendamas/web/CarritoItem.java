package com.tiendamas.web;

import com.tiendamas.entity.Producto;

public class CarritoItem {

    private final Long productoId;
    private final String nombre;
    private final double precioUnitario;
    private int cantidad;

    public CarritoItem(Producto producto, int cantidad) {
        this.productoId = producto.getId();
        this.nombre = producto.getNombre();
        this.precioUnitario = producto.getPrecio();
        this.cantidad = cantidad;
    }

    public Long getProductoId() { return productoId; }
    public String getNombre() { return nombre; }
    public double getPrecioUnitario() { return precioUnitario; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public double getSubtotal() { return precioUnitario * cantidad; }
}
