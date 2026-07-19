package episs.unaj.com.crudpersona.repository;

import episs.unaj.com.crudpersona.entity.CanalVenta;
import episs.unaj.com.crudpersona.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByCanalOrderByFechaDesc(CanalVenta canal);
    List<Pedido> findByPersonaIdOrderByFechaDesc(Long personaId);
}
