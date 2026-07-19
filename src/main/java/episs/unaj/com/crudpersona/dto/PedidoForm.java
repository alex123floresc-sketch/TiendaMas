package episs.unaj.com.crudpersona.dto;

import episs.unaj.com.crudpersona.entity.MetodoPago;
import org.springframework.util.AutoPopulatingList;
import java.util.List;

public class PedidoForm {

    private Long personaId;
    private MetodoPago metodoPago = MetodoPago.EFECTIVO;
    private List<DetalleForm> detalles = new AutoPopulatingList<>(DetalleForm.class);

    public PedidoForm() {}

    public Long getPersonaId() { return personaId; }
    public void setPersonaId(Long personaId) { this.personaId = personaId; }

    public MetodoPago getMetodoPago() { return metodoPago; }
    public void setMetodoPago(MetodoPago metodoPago) { this.metodoPago = metodoPago; }

    public List<DetalleForm> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleForm> detalles) { this.detalles = detalles; }
}

