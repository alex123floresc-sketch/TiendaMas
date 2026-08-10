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

    void registrarUso(Cupon cupon);
}
