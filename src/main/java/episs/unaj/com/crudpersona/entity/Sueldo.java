package episs.unaj.com.crudpersona.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "sueldo")
public class Sueldo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    private Double monto;

    /** Periodo al que corresponde el pago, p. ej. "2026-07". */
    private String periodo;

    private LocalDate fechaPago;

    @Enumerated(EnumType.STRING)
    private EstadoSueldo estado = EstadoSueldo.PENDIENTE;

    private String notas;

    public Sueldo() {
        this.fechaPago = LocalDate.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public Double getMonto() { return monto; }
    public void setMonto(Double monto) { this.monto = monto; }

    public String getPeriodo() { return periodo; }
    public void setPeriodo(String periodo) { this.periodo = periodo; }

    public LocalDate getFechaPago() { return fechaPago; }
    public void setFechaPago(LocalDate fechaPago) { this.fechaPago = fechaPago; }

    public EstadoSueldo getEstado() { return estado; }
    public void setEstado(EstadoSueldo estado) { this.estado = estado; }

    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }
}
