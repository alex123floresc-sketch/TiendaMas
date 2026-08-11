package com.tiendamas.service;

import java.util.Map;

public interface ContenidoService {

    Map<String, String> obtenerTodo();

    void guardar(Map<String, String> valores);
}
