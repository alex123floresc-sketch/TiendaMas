package episs.unaj.com.crudpersona.repository;

import episs.unaj.com.crudpersona.entity.SerieCorrelativo;
import episs.unaj.com.crudpersona.entity.TipoComprobante;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

@Repository
public interface SerieCorrelativoRepository extends JpaRepository<SerieCorrelativo, TipoComprobante> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    SerieCorrelativo findWithLockByTipoComprobante(TipoComprobante tipoComprobante);
}
