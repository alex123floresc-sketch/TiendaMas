package com.tiendamas.dto;

import com.tiendamas.entity.Producto;
import com.tiendamas.entity.VarianteProducto;

public class RecomendacionReabastecimiento {

    private final VarianteProducto variante;
    private final int unidadesVendidas30d;
    private final Double diasRestantes;
    private final String nivel;

    public RecomendacionReabastecimiento(VarianteProducto variante, int unidadesVendidas30d, Double diasRestantes, String nivel) {
        this.variante = variante;
        this.unidadesVendidas30d = unidadesVendidas30d;
        this.diasRestantes = diasRestantes;
        this.nivel = nivel;
    }

    public VarianteProducto getVariante() { return variante; }
    public Producto getProducto() { return variante.getProducto(); }
    public int getUnidadesVendidas30d() { return unidadesVendidas30d; }
    public Double getDiasRestantes() { return diasRestantes; }
    public String getNivel() { return nivel; }
}
