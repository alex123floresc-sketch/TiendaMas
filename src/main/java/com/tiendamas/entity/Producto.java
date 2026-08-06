package com.tiendamas.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "producto")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String descripcion;
    private Double precio;

    private String marca;

    private String imagenUrl;

    @ManyToOne
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VarianteProducto> variantes = new ArrayList<>();

    public Producto() {}

    public Producto(String nombre, String descripcion, Double precio, Categoria categoria) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.categoria = categoria;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Double getPrecio() { return precio; }
    public void setPrecio(Double precio) { this.precio = precio; }

    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }

    public List<VarianteProducto> getVariantes() { return variantes; }
    public void setVariantes(List<VarianteProducto> variantes) { this.variantes = variantes; }

    public void agregarVariante(VarianteProducto variante) {
        variante.setProducto(this);
        this.variantes.add(variante);
    }

    @Transient
    public Integer getStock() {
        if (variantes == null) return 0;
        return variantes.stream().mapToInt(v -> v.getStock() != null ? v.getStock() : 0).sum();
    }

    @Transient
    public boolean tieneStock() {
        return getStock() > 0;
    }

    @Transient
    public List<String> getTallasDisponibles() {
        return variantes == null ? List.of() : variantes.stream()
                .filter(VarianteProducto::tieneStock)
                .map(v -> v.getTalla() != null ? v.getTalla().getEtiqueta() : "")
                .distinct()
                .toList();
    }

}
