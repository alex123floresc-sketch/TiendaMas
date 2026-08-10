package com.tiendamas.service;

import com.tiendamas.dto.ResultadoCupon;
import com.tiendamas.entity.Cupon;

import java.util.List;

public interface CuponService {
    List<Cupon> obtenerTodos();
    Cupon obtenerPorId(Long id);
    Cupon guardar(Cupon cupon);
    void eliminar(Long id);

    /** Valida código, vigencia, tope de usos y monto mínimo contra el subtotal indicado. */
    ResultadoCupon validar(String codigo, double subtotal);

    /** Igual que validar, pero además comprueba que los cupones personales (de puntos de fidelidad) le pertenezcan a personaId. */
    ResultadoCupon validar(String codigo, double subtotal, Long personaId);

    void registrarUso(Cupon cupon);
}
