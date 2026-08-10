package com.tiendamas.dto;

import com.tiendamas.entity.Persona;

public class ClienteResumen {

    private final Persona persona;
    private final double total;
    private final int pedidos;

    public ClienteResumen(Persona persona, double total, int pedidos) {
        this.persona = persona;
        this.total = total;
        this.pedidos = pedidos;
    }

    public Persona getPersona() { return persona; }
    public double getTotal() { return total; }
    public int getPedidos() { return pedidos; }
}
