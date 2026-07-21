package com.tiendamas.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "serie_correlativo")
public class SerieCorrelativo {

    @Id
    @Enumerated(EnumType.STRING)
    private TipoComprobante tipoComprobante;

    private int ultimoNumero = 0;

    public SerieCorrelativo() {}

    public SerieCorrelativo(TipoComprobante tipoComprobante) {
        this.tipoComprobante = tipoComprobante;
    }

    public TipoComprobante getTipoComprobante() { return tipoComprobante; }
    public void setTipoComprobante(TipoComprobante tipoComprobante) { this.tipoComprobante = tipoComprobante; }

    public int getUltimoNumero() { return ultimoNumero; }
    public void setUltimoNumero(int ultimoNumero) { this.ultimoNumero = ultimoNumero; }
}
