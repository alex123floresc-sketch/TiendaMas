package com.tiendamas.dto;

/** Un valor del período actual junto al mismo indicador del período anterior de
 *  igual duración, para mostrar la variación porcentual en los reportes. */
public class ComparacionKpi {

    private final double valorActual;
    private final double valorAnterior;

    public ComparacionKpi(double valorActual, double valorAnterior) {
        this.valorActual = valorActual;
        this.valorAnterior = valorAnterior;
    }

    public double getValorActual() { return valorActual; }
    public double getValorAnterior() { return valorAnterior; }

    /** Null cuando no hay base de comparación (período anterior en cero y actual también en cero). */
    public Double getVariacionPorcentaje() {
        if (valorAnterior == 0) {
            return valorActual == 0 ? null : 100.0;
        }
        return ((valorActual - valorAnterior) / valorAnterior) * 100.0;
    }

    public boolean isSubio() {
        Double v = getVariacionPorcentaje();
        return v != null && v > 0.05;
    }

    public boolean isBajo() {
        Double v = getVariacionPorcentaje();
        return v != null && v < -0.05;
    }

    public boolean isSinBase() {
        return getVariacionPorcentaje() == null;
    }
}
