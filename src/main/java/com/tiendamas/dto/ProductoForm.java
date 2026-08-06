package com.tiendamas.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.util.AutoPopulatingList;

import java.util.List;

public class ProductoForm {

    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    private String descripcion;

    @NotNull(message = "El precio es obligatorio")
    @Positive(message = "El precio debe ser mayor a cero")
    private Double precio;

    private String marca;

    @NotNull(message = "Selecciona una categoría")
    private Long categoriaId;

    @Valid
    private List<VarianteForm> variantes = new AutoPopulatingList<>(VarianteForm.class);

    public ProductoForm() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Double getPrecio() { return precio; }
    public void setPrecio(Double precio) { this.precio = precio; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public Long getCategoriaId() { return categoriaId; }
    public void setCategoriaId(Long categoriaId) { this.categoriaId = categoriaId; }

    public List<VarianteForm> getVariantes() { return variantes; }
    public void setVariantes(List<VarianteForm> variantes) { this.variantes = variantes; }
}
