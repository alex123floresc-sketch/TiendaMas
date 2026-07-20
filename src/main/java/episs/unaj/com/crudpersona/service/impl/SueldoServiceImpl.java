package episs.unaj.com.crudpersona.service.impl;

import episs.unaj.com.crudpersona.entity.EstadoSueldo;
import episs.unaj.com.crudpersona.entity.Sueldo;
import episs.unaj.com.crudpersona.repository.SueldoRepository;
import episs.unaj.com.crudpersona.service.SueldoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SueldoServiceImpl implements SueldoService {

    @Autowired
    private SueldoRepository sueldoRepository;

    @Override
    public List<Sueldo> obtenerTodos() {
        return sueldoRepository.findAll();
    }

    @Override
    public Sueldo obtenerPorId(Long id) {
        return sueldoRepository.findById(id).orElse(null);
    }

    @Override
    public Sueldo guardar(Sueldo sueldo) {
        return sueldoRepository.save(sueldo);
    }

    @Override
    public void marcarPagado(Long id) {
        Sueldo sueldo = obtenerPorId(id);
        if (sueldo != null) {
            sueldo.setEstado(EstadoSueldo.PAGADO);
            sueldoRepository.save(sueldo);
        }
    }

    @Override
    public void eliminar(Long id) {
        sueldoRepository.deleteById(id);
    }
}
