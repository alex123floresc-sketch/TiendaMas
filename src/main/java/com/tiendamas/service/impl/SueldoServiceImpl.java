package com.tiendamas.service.impl;

import com.tiendamas.entity.EstadoSueldo;
import com.tiendamas.entity.Sueldo;
import com.tiendamas.repository.SueldoRepository;
import com.tiendamas.service.SueldoService;
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
