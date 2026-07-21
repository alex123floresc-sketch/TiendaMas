package com.tiendamas.config;

import com.tiendamas.controller.PosController;
import com.tiendamas.controller.TiendaController;
import com.tiendamas.web.Carrito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(assignableTypes = {TiendaController.class, PosController.class})
public class CarritoModelAttributes {

    @Autowired
    private Carrito carrito;

    @ModelAttribute("carritoCount")
    public int carritoCount() {
        return carrito.getCantidadTotal();
    }
}
