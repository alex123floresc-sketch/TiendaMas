package episs.unaj.com.crudpersona.service;

import episs.unaj.com.crudpersona.entity.Persona;
import java.util.List;

public interface PersonaService {
    List<Persona> obtenerTodas();
    Persona guardar(Persona persona);
    Persona obtenerPorId(Long id);
    void eliminar(Long id);
}