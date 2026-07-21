package com.tiendamas.dto;

import com.tiendamas.entity.Producto;

public class RecomendacionReabastecimiento {

    private final Producto producto;
    private final int unidadesVendidas30d;
    private final Double diasRestantes;
    private final String nivel;

    public RecomendacionReabastecimiento(Producto producto, int unidadesVendidas30d, Double diasRestantes, String nivel) {
        this.producto = producto;
        this.unidadesVendidas30d = unidadesVendidas30d;
        this.diasRestantes = diasRestantes;
        this.nivel = nivel;
    }

    public Producto getProducto() { return producto; }
    public int getUnidadesVendidas30d() { return unidadesVendidas30d; }
    public Double getDiasRestantes() { return diasRestantes; }
    public String getNivel() { return nivel; }
}
