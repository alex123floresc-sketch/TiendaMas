package com.tiendamas.service.impl;

import com.tiendamas.entity.Pedido;
import com.tiendamas.entity.Persona;
import com.tiendamas.entity.Producto;
import com.tiendamas.entity.Resena;
import com.tiendamas.repository.PedidoRepository;
import com.tiendamas.repository.PersonaRepository;
import com.tiendamas.repository.ProductoRepository;
import com.tiendamas.repository.ResenaRepository;
import com.tiendamas.service.ResenaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResenaServiceImpl implements ResenaService {

    @Autowired
    private ResenaRepository resenaRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Override
    public List<Resena> obtenerPorProducto(Long productoId) {
        return resenaRepository.findByProductoIdOrderByFechaDesc(productoId);
    }

    @Override
    public double promedioPorProducto(Long productoId) {
        Double promedio = resenaRepository.promedioCalificacion(productoId);
        return promedio != null ? promedio : 0.0;
    }

    @Override
    public boolean puedeResenar(Long productoId, Long personaId) {
        if (productoId == null || personaId == null) return false;
        return compro(productoId, personaId) && !yaReseno(productoId, personaId);
    }

    @Override
    public boolean yaReseno(Long productoId, Long personaId) {
        if (productoId == null || personaId == null) return false;
        return resenaRepository.existsByProductoIdAndPersonaId(productoId, personaId);
    }

    private boolean compro(Long productoId, Long personaId) {
        List<Pedido> pedidos = pedidoRepository.findByPersonaIdOrderByFechaDesc(personaId);
        return pedidos.stream().anyMatch(pedido -> pedido.getDetalles().stream()
                .anyMatch(detalle -> detalle.getProducto() != null && detalle.getProducto().getId().equals(productoId)));
    }

    @Override
    public Resena crear(Long productoId, Long personaId, Integer calificacion, String comentario) {
        if (calificacion == null || calificacion < 1 || calificacion > 5) {
            throw new IllegalArgumentException("La calificación debe ser de 1 a 5 estrellas");
        }
        if (!puedeResenar(productoId, personaId)) {
            throw new IllegalArgumentException("Solo podés reseñar productos que hayas comprado, y una sola vez por producto");
        }
        Producto producto = productoRepository.findById(productoId).orElse(null);
        Persona persona = personaRepository.findById(personaId).orElse(null);
        if (producto == null || persona == null) {
            throw new IllegalArgumentException("Producto o cliente no encontrado");
        }

        Resena resena = new Resena();
        resena.setProducto(producto);
        resena.setPersona(persona);
        resena.setCalificacion(calificacion);
        resena.setComentario(comentario != null && !comentario.isBlank() ? comentario.trim() : null);
        return resenaRepository.save(resena);
    }
}
