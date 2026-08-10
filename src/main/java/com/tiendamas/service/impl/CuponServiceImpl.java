package com.tiendamas.service.impl;

import com.tiendamas.dto.ResultadoCupon;
import com.tiendamas.entity.Cupon;
import com.tiendamas.repository.CuponRepository;
import com.tiendamas.service.CuponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class CuponServiceImpl implements CuponService {

    @Autowired
    private CuponRepository cuponRepository;

    @Override
    public List<Cupon> obtenerTodos() {
        return cuponRepository.findAllByOrderByIdDesc();
    }

    @Override
    public Cupon obtenerPorId(Long id) {
        return id == null ? null : cuponRepository.findById(id).orElse(null);
    }

    @Override
    public Cupon guardar(Cupon cupon) {
        if (cupon.getUsosRealizados() == null) {
            cupon.setUsosRealizados(0);
        }
        if (cupon.getActivo() == null) {
            cupon.setActivo(true);
        }
        return cuponRepository.save(cupon);
    }

    @Override
    public void eliminar(Long id) {
        cuponRepository.deleteById(id);
    }

    @Override
    public ResultadoCupon validar(String codigo, double subtotal) {
        if (codigo == null || codigo.isBlank()) {
            return ResultadoCupon.invalido("Ingresa un código de cupón");
        }
        Cupon cupon = cuponRepository.findByCodigoIgnoreCase(codigo.trim()).orElse(null);
        if (cupon == null) {
            return ResultadoCupon.invalido("El cupón \"" + codigo.trim() + "\" no existe");
        }
        if (cupon.getMontoMinimo() != null && subtotal < cupon.getMontoMinimo()) {
            return ResultadoCupon.invalido(String.format(
                    "Este cupón requiere una compra mínima de S/ %.2f", cupon.getMontoMinimo()));
        }
        if (!cupon.esValidoPara(subtotal, LocalDate.now())) {
            return ResultadoCupon.invalido("Este cupón no está vigente o ya alcanzó su límite de usos");
        }
        return ResultadoCupon.valido(cupon);
    }

    @Override
    @Transactional
    public void registrarUso(Cupon cupon) {
        if (cupon == null || cupon.getId() == null) return;
        Cupon actual = cuponRepository.findById(cupon.getId()).orElse(null);
        if (actual == null) return;
        actual.setUsosRealizados((actual.getUsosRealizados() != null ? actual.getUsosRealizados() : 0) + 1);
        cuponRepository.save(actual);
    }
}
