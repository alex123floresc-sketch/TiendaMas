package com.tiendamas.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import java.time.LocalDate;

@Entity
@Table(name = "cupon")
public class Cupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String codigo;

    @Enumerated(EnumType.STRING)
    private TipoDescuentoCupon tipoDescuento;

    private Double valor;

    private Boolean activo = true;

    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    /** Monto mínimo de compra (subtotal) para poder usar el cupón. Null = sin mínimo. */
    private Double montoMinimo;

    /** Cantidad máxima de veces que se puede usar en total. Null = sin límite. */
    private Integer usoMaximo;

    private Integer usosRealizados = 0;

    /** Si no es null, el cupón es personal (por ejemplo, canjeado con puntos de fidelidad) y solo esa persona puede usarlo. */
    @ManyToOne
    @JoinColumn(name = "persona_asignada_id")
    private Persona personaAsignada;

    public Cupon() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo == null ? null : codigo.trim().toUpperCase(); }

    public TipoDescuentoCupon getTipoDescuento() { return tipoDescuento; }
    public void setTipoDescuento(TipoDescuentoCupon tipoDescuento) { this.tipoDescuento = tipoDescuento; }

    public Double getValor() { return valor; }
    public void setValor(Double valor) { this.valor = valor; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }

    public Double getMontoMinimo() { return montoMinimo; }
    public void setMontoMinimo(Double montoMinimo) { this.montoMinimo = montoMinimo; }

    public Integer getUsoMaximo() { return usoMaximo; }
    public void setUsoMaximo(Integer usoMaximo) { this.usoMaximo = usoMaximo; }

    public Integer getUsosRealizados() { return usosRealizados; }
    public void setUsosRealizados(Integer usosRealizados) { this.usosRealizados = usosRealizados; }

    public Persona getPersonaAsignada() { return personaAsignada; }
    public void setPersonaAsignada(Persona personaAsignada) { this.personaAsignada = personaAsignada; }

    /** Verifica vigencia, actividad, tope de usos y monto mínimo (no valida el código en sí). */
    @Transient
    public boolean esValidoPara(double subtotal, LocalDate hoy) {
        if (activo == null || !activo) return false;
        if (fechaInicio != null && hoy.isBefore(fechaInicio)) return false;
        if (fechaFin != null && hoy.isAfter(fechaFin)) return false;
        if (usoMaximo != null && usosRealizados != null && usosRealizados >= usoMaximo) return false;
        if (montoMinimo != null && subtotal < montoMinimo) return false;
        return true;
    }

    @Transient
    public double calcularDescuento(double subtotal) {
        if (valor == null || valor <= 0 || subtotal <= 0) return 0.0;
        double descuento = tipoDescuento == TipoDescuentoCupon.PORCENTAJE
                ? subtotal * (valor / 100.0)
                : valor;
        return Math.min(descuento, subtotal);
    }
}
