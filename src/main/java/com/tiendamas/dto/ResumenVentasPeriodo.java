package com.tiendamas.dto;

public class ResumenVentasPeriodo {

    private final String etiqueta;
    private final double ventasTiendaFisica;
    private final double ventasOnline;

    public ResumenVentasPeriodo(String etiqueta, double ventasTiendaFisica, double ventasOnline) {
        this.etiqueta = etiqueta;
        this.ventasTiendaFisica = ventasTiendaFisica;
        this.ventasOnline = ventasOnline;
    }

    public String getEtiqueta() { return etiqueta; }
    public double getVentasTiendaFisica() { return ventasTiendaFisica; }
    public double getVentasOnline() { return ventasOnline; }

    public double getTotal() {
        return ventasTiendaFisica + ventasOnline;
    }
}
