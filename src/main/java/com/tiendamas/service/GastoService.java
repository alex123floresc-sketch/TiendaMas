package com.tiendamas.service;

import com.tiendamas.entity.Gasto;

import java.util.List;

public interface GastoService {
    List<Gasto> obtenerTodos();
    Gasto obtenerPorId(Long id);
    Gasto guardar(Gasto gasto);
    Gasto duplicar(Long id);
    void eliminar(Long id);
}
