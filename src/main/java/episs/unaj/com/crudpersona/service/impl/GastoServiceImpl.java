package episs.unaj.com.crudpersona.service.impl;

import episs.unaj.com.crudpersona.entity.Gasto;
import episs.unaj.com.crudpersona.repository.GastoRepository;
import episs.unaj.com.crudpersona.service.GastoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class GastoServiceImpl implements GastoService {

    @Autowired
    private GastoRepository gastoRepository;

    @Override
    public List<Gasto> obtenerTodos() {
        return gastoRepository.findAll();
    }

    @Override
    public Gasto obtenerPorId(Long id) {
        return gastoRepository.findById(id).orElse(null);
    }

    @Override
    public Gasto guardar(Gasto gasto) {
        return gastoRepository.save(gasto);
    }

    @Override
    public Gasto duplicar(Long id) {
        Gasto original = obtenerPorId(id);
        if (original == null) return null;

        Gasto copia = new Gasto();
        copia.setConcepto(original.getConcepto());
        copia.setCategoria(original.getCategoria());
        copia.setMonto(original.getMonto());
        copia.setFecha(LocalDate.now());
        copia.setRecurrente(original.isRecurrente());
        copia.setFrecuencia(original.getFrecuencia());
        copia.setNotas(original.getNotas());
        return gastoRepository.save(copia);
    }

    @Override
    public void eliminar(Long id) {
        gastoRepository.deleteById(id);
    }
}
