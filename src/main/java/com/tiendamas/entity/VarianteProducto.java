package com.tiendamas.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "variante_producto",
        uniqueConstraints = @UniqueConstraint(columnNames = {"producto_id", "talla", "color"}))
public class VarianteProducto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Enumerated(EnumType.STRING)
    private Talla talla;

    private String color;

    private String colorHex;

    private Integer stock = 0;

    @Column(unique = true)
    private String codigoBarras;

    public VarianteProducto() {}

    public VarianteProducto(Producto producto, Talla talla, String color, String colorHex, Integer stock, String codigoBarras) {
        this.producto = producto;
        this.talla = talla;
        this.color = color;
        this.colorHex = colorHex;
        this.stock = stock;
        this.codigoBarras = codigoBarras;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }

    public Talla getTalla() { return talla; }
    public void setTalla(Talla talla) { this.talla = talla; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getColorHex() { return colorHex; }
    public void setColorHex(String colorHex) { this.colorHex = colorHex; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public String getCodigoBarras() { return codigoBarras; }
    public void setCodigoBarras(String codigoBarras) { this.codigoBarras = codigoBarras; }

    @Transient
    public boolean tieneStock() {
        return stock != null && stock > 0;
    }

    @Transient
    public String getEtiqueta() {
        String tallaTexto = talla != null ? talla.getEtiqueta() : "";
        if (color == null || color.isBlank()) return tallaTexto;
        return tallaTexto + " · " + color;
    }

    @Transient
    public String getEtiquetaCompleta() {
        String nombreProducto = producto != null ? producto.getNombre() : "";
        return nombreProducto + " — " + getEtiqueta();
    }
}
