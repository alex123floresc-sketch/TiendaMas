package com.tiendamas.service;

import com.tiendamas.entity.Cupon;
import com.tiendamas.entity.Persona;

public interface FidelidadService {

    /** Suma los puntos que corresponden por una compra (1 punto por cada S/ 10 del subtotal pagado). */
    void registrarCompra(Persona persona, double subtotalPagado);

    int obtenerPuntos(Long personaId);

    /** Cuántos soles de descuento representan esos puntos si se canjean (10 puntos = S/ 1), sin canjearlos todavía. */
    double valorEnSoles(int puntos);

    int getMinimoParaCanjear();

    /** Canjea los puntos indicados por un cupón de descuento personal, válido solo para esa persona. */
    Cupon canjear(Long personaId, int puntos);
}
