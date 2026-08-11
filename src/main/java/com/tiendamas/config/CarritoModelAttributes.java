package com.tiendamas.config;

import com.tiendamas.controller.PosController;
import com.tiendamas.controller.TiendaController;
import com.tiendamas.entity.Categoria;
import com.tiendamas.service.CategoriaService;
import com.tiendamas.service.ContenidoService;
import com.tiendamas.web.Carrito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;
import java.util.Map;

@ControllerAdvice(assignableTypes = {TiendaController.class, PosController.class})
public class CarritoModelAttributes {

    @Autowired
    private Carrito carrito;

    @Autowired
    private CategoriaService categoriaService;

    @Autowired
    private ContenidoService contenidoService;

    @ModelAttribute("carritoCount")
    public int carritoCount() {
        return carrito.getCantidadTotal();
    }

    @ModelAttribute("categoriasMenu")
    public List<Categoria> categoriasMenu() {
        return categoriaService.obtenerPrincipales();
    }

    @ModelAttribute("contenido")
    public Map<String, String> contenido() {
        return contenidoService.obtenerTodo();
    }
}
