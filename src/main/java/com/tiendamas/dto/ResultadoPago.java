package com.tiendamas.dto;

/** Resultado de intentar cobrar con la pasarela de pago: o trae el id del cargo, o el motivo del fallo. */
public class ResultadoPago {

    private final boolean exitoso;
    private final String chargeId;
    private final String mensaje;

    private ResultadoPago(boolean exitoso, String chargeId, String mensaje) {
        this.exitoso = exitoso;
        this.chargeId = chargeId;
        this.mensaje = mensaje;
    }

    public static ResultadoPago exito(String chargeId) {
        return new ResultadoPago(true, chargeId, null);
    }

    public static ResultadoPago fallo(String mensaje) {
        return new ResultadoPago(false, null, mensaje);
    }

    public boolean isExitoso() { return exitoso; }
    public String getChargeId() { return chargeId; }
    public String getMensaje() { return mensaje; }
}
