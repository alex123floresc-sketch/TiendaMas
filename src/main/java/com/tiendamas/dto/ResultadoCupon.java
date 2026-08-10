package com.tiendamas.dto;

import com.tiendamas.entity.Cupon;

/** Resultado de validar un código de cupón: o trae el cupón usable, o el motivo por el que no aplica. */
public class ResultadoCupon {

    private final Cupon cupon;
    private final String error;

    private ResultadoCupon(Cupon cupon, String error) {
        this.cupon = cupon;
        this.error = error;
    }

    public static ResultadoCupon valido(Cupon cupon) {
        return new ResultadoCupon(cupon, null);
    }

    public static ResultadoCupon invalido(String error) {
        return new ResultadoCupon(null, error);
    }

    public boolean isValido() { return cupon != null; }
    public Cupon getCupon() { return cupon; }
    public String getError() { return error; }
}
