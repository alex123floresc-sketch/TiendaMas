package episs.unaj.com.crudpersona.web;

import episs.unaj.com.crudpersona.entity.Producto;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Carrito de compras en memoria de sesión. Lo comparten el punto de venta
 * (vendedor en tienda física) y la tienda en línea (cliente): misma lógica
 * de agregar/quitar/actualizar/total en un solo lugar.
 */
@Component
@SessionScope
public class Carrito {

    private final Map<Long, CarritoItem> items = new LinkedHashMap<>();

    public void agregar(Producto producto, int cantidad) {
        CarritoItem existente = items.get(producto.getId());
        if (existente != null) {
            existente.setCantidad(existente.getCantidad() + cantidad);
        } else {
            items.put(producto.getId(), new CarritoItem(producto, cantidad));
        }
    }

    public void actualizarCantidad(Long productoId, int cantidad) {
        if (cantidad <= 0) {
            items.remove(productoId);
            return;
        }
        CarritoItem item = items.get(productoId);
        if (item != null) {
            item.setCantidad(cantidad);
        }
    }

    public void quitar(Long productoId) {
        items.remove(productoId);
    }

    public void vaciar() {
        items.clear();
    }

    public Collection<CarritoItem> getItems() {
        return items.values();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public int getCantidadTotal() {
        return items.values().stream().mapToInt(CarritoItem::getCantidad).sum();
    }

    public double getTotal() {
        return items.values().stream().mapToDouble(CarritoItem::getSubtotal).sum();
    }
}
