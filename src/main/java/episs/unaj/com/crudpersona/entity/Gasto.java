package episs.unaj.com.crudpersona.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "gasto")
public class Gasto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String concepto;

    @Enumerated(EnumType.STRING)
    private CategoriaGasto categoria;

    private Double monto;
    private LocalDate fecha;

    private boolean recurrente;

    @Enumerated(EnumType.STRING)
    private FrecuenciaGasto frecuencia;

    private String notas;

    public Gasto() {
        this.fecha = LocalDate.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getConcepto() { return concepto; }
    public void setConcepto(String concepto) { this.concepto = concepto; }

    public CategoriaGasto getCategoria() { return categoria; }
    public void setCategoria(CategoriaGasto categoria) { this.categoria = categoria; }

    public Double getMonto() { return monto; }
    public void setMonto(Double monto) { this.monto = monto; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public boolean isRecurrente() { return recurrente; }
    public void setRecurrente(boolean recurrente) { this.recurrente = recurrente; }

    public FrecuenciaGasto getFrecuencia() { return frecuencia; }
    public void setFrecuencia(FrecuenciaGasto frecuencia) { this.frecuencia = frecuencia; }

    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }
}
