package com.tiendamas.dto;

import com.tiendamas.entity.Talla;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public class VarianteForm {

    private Long id;

    @NotNull(message = "Selecciona una talla")
    private Talla talla;

    private String color;
    private String colorHex;

    @NotNull(message = "Indica el stock")
    @PositiveOrZero(message = "El stock no puede ser negativo")
    private Integer stock;

    private String codigoBarras;

    public VarianteForm() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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
}
