package com.tiendamas.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "contenido_sitio")
public class ContenidoSitio {

    @Id
    @Column(length = 60)
    private String clave;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String valor;

    public ContenidoSitio() {
    }

    public ContenidoSitio(String clave, String valor) {
        this.clave = clave;
        this.valor = valor;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }
}
