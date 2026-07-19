package episs.unaj.com.crudpersona.repository;

import episs.unaj.com.crudpersona.entity.Persona;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonaRepository extends JpaRepository<Persona,Long> {
}
