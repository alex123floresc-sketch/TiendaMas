package episs.unaj.com.crudpersona.dto;

public class ItemVenta {

    private final Long productoId;
    private final Integer cantidad;

    public ItemVenta(Long productoId, Integer cantidad) {
        this.productoId = productoId;
        this.cantidad = cantidad;
    }

    public Long getProductoId() { return productoId; }
    public Integer getCantidad() { return cantidad; }
}
