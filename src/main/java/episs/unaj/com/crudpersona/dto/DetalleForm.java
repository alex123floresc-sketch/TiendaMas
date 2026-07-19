package episs.unaj.com.crudpersona.dto;

public class DetalleForm {

    private Long productoId;
    private Integer cantidad;

    public DetalleForm() {}

    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
}

