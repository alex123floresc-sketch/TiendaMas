package episs.unaj.com.crudpersona.config;

import episs.unaj.com.crudpersona.controller.PosController;
import episs.unaj.com.crudpersona.controller.TiendaController;
import episs.unaj.com.crudpersona.web.Carrito;
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
