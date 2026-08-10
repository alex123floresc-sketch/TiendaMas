package com.tiendamas.service.impl;

import com.tiendamas.entity.TipoEntrega;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Calcula el costo de envío a domicilio: fijo, gratis a partir de un monto de compra (ver application.yml). */
@Service
public class EnvioService {

    @Value("${app.envio.costo}")
    private double costo;

    @Value("${app.envio.montoGratis}")
    private double montoGratis;

    public double calcularCosto(double subtotal, TipoEntrega tipoEntrega) {
        if (tipoEntrega != TipoEntrega.DOMICILIO) {
            return 0.0;
        }
        return subtotal >= montoGratis ? 0.0 : costo;
    }

    public double getCosto() { return costo; }
    public double getMontoGratis() { return montoGratis; }
}
