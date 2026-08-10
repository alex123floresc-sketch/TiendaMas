package com.tiendamas.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedido")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime fecha;

    @ManyToOne
    @JoinColumn(name = "persona_id", nullable = false)
    private Persona persona;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<DetallePedido> detalles = new ArrayList<>();

    private Double total;

    private Double costoEnvio = 0.0;

    private Double descuento = 0.0;

    @ManyToOne
    @JoinColumn(name = "cupon_id")
    private Cupon cupon;

    @Enumerated(EnumType.STRING)
    private TipoComprobante tipoComprobante;

    private String serie;
    private Integer numero;

    @Enumerated(EnumType.STRING)
    private MetodoPago metodoPago;

    @Enumerated(EnumType.STRING)
    private CanalVenta canal;

    private String vendedorUsername;

    @Enumerated(EnumType.STRING)
    private TipoEntrega tipoEntrega = TipoEntrega.RETIRO_TIENDA;

    private String direccionEntrega;

    @Enumerated(EnumType.STRING)
    private EstadoPedido estado = EstadoPedido.PENDIENTE;

    /** Id del cargo en Culqi cuando el pago fue con tarjeta (referencia para reconciliación). */
    private String culquiChargeId;

    public Pedido() {
        this.fecha = LocalDateTime.now();
    }

    public void agregarDetalle(DetallePedido d) {
        d.setPedido(this);
        this.detalles.add(d);
    }

    @Transient
    public String getFechaTexto() {
        if (fecha == null) return "";
        return fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public Persona getPersona() { return persona; }
    public void setPersona(Persona persona) { this.persona = persona; }

    public List<DetallePedido> getDetalles() { return detalles; }
    public void setDetalles(List<DetallePedido> detalles) { this.detalles = detalles; }

    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }

    public Double getCostoEnvio() { return costoEnvio; }
    public void setCostoEnvio(Double costoEnvio) { this.costoEnvio = costoEnvio; }

    public Double getDescuento() { return descuento; }
    public void setDescuento(Double descuento) { this.descuento = descuento; }

    public Cupon getCupon() { return cupon; }
    public void setCupon(Cupon cupon) { this.cupon = cupon; }

    /** Total de los productos solo (sin envío, sin descuento), para desglosar el comprobante. */
    @Transient
    public double getSubtotal() {
        double totalPedido = total != null ? total : 0.0;
        double envio = costoEnvio != null ? costoEnvio : 0.0;
        double desc = descuento != null ? descuento : 0.0;
        return totalPedido - envio + desc;
    }

    public TipoComprobante getTipoComprobante() { return tipoComprobante; }
    public void setTipoComprobante(TipoComprobante tipoComprobante) { this.tipoComprobante = tipoComprobante; }

    public String getSerie() { return serie; }
    public void setSerie(String serie) { this.serie = serie; }

    public Integer getNumero() { return numero; }
    public void setNumero(Integer numero) { this.numero = numero; }

    public MetodoPago getMetodoPago() { return metodoPago; }
    public void setMetodoPago(MetodoPago metodoPago) { this.metodoPago = metodoPago; }

    public CanalVenta getCanal() { return canal; }
    public void setCanal(CanalVenta canal) { this.canal = canal; }

    public String getVendedorUsername() { return vendedorUsername; }
    public void setVendedorUsername(String vendedorUsername) { this.vendedorUsername = vendedorUsername; }

    public TipoEntrega getTipoEntrega() { return tipoEntrega; }
    public void setTipoEntrega(TipoEntrega tipoEntrega) { this.tipoEntrega = tipoEntrega; }

    public String getDireccionEntrega() { return direccionEntrega; }
    public void setDireccionEntrega(String direccionEntrega) { this.direccionEntrega = direccionEntrega; }

    public EstadoPedido getEstado() { return estado; }
    public void setEstado(EstadoPedido estado) { this.estado = estado; }

    public String getCulquiChargeId() { return culquiChargeId; }
    public void setCulquiChargeId(String culquiChargeId) { this.culquiChargeId = culquiChargeId; }

    @Transient
    public String getNumeroCompleto() {
        if (serie == null || numero == null) return "";
        return serie + "-" + String.format("%06d", numero);
    }
}
