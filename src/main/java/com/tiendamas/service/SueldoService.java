package com.tiendamas.service;

import com.tiendamas.entity.Sueldo;

import java.util.List;

public interface SueldoService {
    List<Sueldo> obtenerTodos();
    Sueldo obtenerPorId(Long id);
    Sueldo guardar(Sueldo sueldo);
    void marcarPagado(Long id);
    void eliminar(Long id);
}
