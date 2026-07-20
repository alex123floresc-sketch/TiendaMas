package episs.unaj.com.crudpersona.service;

import episs.unaj.com.crudpersona.entity.Gasto;

import java.util.List;

public interface GastoService {
    List<Gasto> obtenerTodos();
    Gasto obtenerPorId(Long id);
    Gasto guardar(Gasto gasto);
    Gasto duplicar(Long id);
    void eliminar(Long id);
}
