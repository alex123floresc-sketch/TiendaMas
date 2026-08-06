package com.tiendamas.web;

import com.tiendamas.entity.VarianteProducto;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@SessionScope
public class Carrito {

    private final Map<Long, CarritoItem> items = new LinkedHashMap<>();

    public void agregar(VarianteProducto variante, int cantidad) {
        int stockDisponible = variante.getStock() != null ? variante.getStock() : 0;
        CarritoItem existente = items.get(variante.getId());
        if (existente != null) {
            int nuevaCantidad = Math.min(existente.getCantidad() + cantidad, Math.max(stockDisponible, 1));
            existente.setCantidad(nuevaCantidad);
        } else {
            CarritoItem item = new CarritoItem(variante, Math.min(cantidad, Math.max(stockDisponible, 1)));
            items.put(variante.getId(), item);
        }
    }

    public void actualizarCantidad(Long varianteId, int cantidad) {
        if (cantidad <= 0) {
            items.remove(varianteId);
            return;
        }
        CarritoItem item = items.get(varianteId);
        if (item != null) {
            item.setCantidad(Math.min(cantidad, Math.max(item.getStockDisponible(), 1)));
        }
    }

    public void quitar(Long varianteId) {
        items.remove(varianteId);
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
