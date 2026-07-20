package episs.unaj.com.crudpersona.repository;

import episs.unaj.com.crudpersona.entity.Sueldo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SueldoRepository extends JpaRepository<Sueldo, Long> {
}
