package episs.unaj.com.crudpersona.dto;

import org.springframework.util.AutoPopulatingList;
import java.util.List;

public class PedidoForm {

    private Long personaId;
    private List<DetalleForm> detalles = new AutoPopulatingList<>(DetalleForm.class);

    public PedidoForm() {}

    public Long getPersonaId() { return personaId; }
    public void setPersonaId(Long personaId) { this.personaId = personaId; }

    public List<DetalleForm> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleForm> detalles) { this.detalles = detalles; }
}

