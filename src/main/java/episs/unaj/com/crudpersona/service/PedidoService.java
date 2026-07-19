package episs.unaj.com.crudpersona.service;

import episs.unaj.com.crudpersona.dto.PedidoForm;
import episs.unaj.com.crudpersona.entity.Pedido;

import java.util.List;

public interface PedidoService {
    List<Pedido> obtenerTodos();
    Pedido obtenerPorId(Long id);
    Pedido crearPedido(PedidoForm form);
    void eliminar(Long id);
}

