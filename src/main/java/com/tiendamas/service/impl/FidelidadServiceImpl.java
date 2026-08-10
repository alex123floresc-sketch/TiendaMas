package com.tiendamas.service.impl;

import com.tiendamas.entity.Cupon;
import com.tiendamas.entity.Persona;
import com.tiendamas.entity.TipoDescuentoCupon;
import com.tiendamas.repository.CuponRepository;
import com.tiendamas.repository.PersonaRepository;
import com.tiendamas.service.FidelidadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;

@Service
public class FidelidadServiceImpl implements FidelidadService {

    /** 1 punto por cada S/ 10 gastados. */
    private static final int SOLES_POR_PUNTO_GANADO = 10;

    /** Al canjear, 10 puntos equivalen a S/ 1 de descuento. */
    private static final int PUNTOS_POR_SOL_CANJE = 10;

    private static final int MINIMO_PARA_CANJEAR = 50;

    private static final int DIAS_VIGENCIA_CUPON_CANJEADO = 90;

    private static final String ALFABETO_CODIGO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private final SecureRandom random = new SecureRandom();

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private CuponRepository cuponRepository;

    @Override
    @Transactional
    public void registrarCompra(Persona persona, double subtotalPagado) {
        if (persona == null || persona.getId() == null || subtotalPagado <= 0) return;
        int puntosGanados = (int) (subtotalPagado / SOLES_POR_PUNTO_GANADO);
        if (puntosGanados <= 0) return;

        Persona actual = personaRepository.findById(persona.getId()).orElse(null);
        if (actual == null) return;
        int puntosActuales = actual.getPuntosFidelidad() != null ? actual.getPuntosFidelidad() : 0;
        actual.setPuntosFidelidad(puntosActuales + puntosGanados);
        personaRepository.save(actual);
    }

    @Override
    public int obtenerPuntos(Long personaId) {
        if (personaId == null) return 0;
        Persona persona = personaRepository.findById(personaId).orElse(null);
        return persona != null && persona.getPuntosFidelidad() != null ? persona.getPuntosFidelidad() : 0;
    }

    @Override
    public double valorEnSoles(int puntos) {
        return puntos / (double) PUNTOS_POR_SOL_CANJE;
    }

    @Override
    public int getMinimoParaCanjear() {
        return MINIMO_PARA_CANJEAR;
    }

    @Override
    @Transactional
    public Cupon canjear(Long personaId, int puntos) {
        if (puntos < MINIMO_PARA_CANJEAR) {
            throw new IllegalArgumentException("El canje mínimo es de " + MINIMO_PARA_CANJEAR + " puntos");
        }
        Persona persona = personaId != null ? personaRepository.findById(personaId).orElse(null) : null;
        if (persona == null) {
            throw new IllegalArgumentException("Cliente no encontrado");
        }
        int puntosActuales = persona.getPuntosFidelidad() != null ? persona.getPuntosFidelidad() : 0;
        if (puntos > puntosActuales) {
            throw new IllegalArgumentException("No tenés suficientes puntos para ese canje");
        }

        Cupon cupon = new Cupon();
        cupon.setCodigo(generarCodigoUnico());
        cupon.setTipoDescuento(TipoDescuentoCupon.MONTO_FIJO);
        cupon.setValor(valorEnSoles(puntos));
        cupon.setActivo(true);
        cupon.setFechaInicio(LocalDate.now());
        cupon.setFechaFin(LocalDate.now().plusDays(DIAS_VIGENCIA_CUPON_CANJEADO));
        cupon.setUsoMaximo(1);
        cupon.setUsosRealizados(0);
        cupon.setPersonaAsignada(persona);
        cuponRepository.save(cupon);

        persona.setPuntosFidelidad(puntosActuales - puntos);
        personaRepository.save(persona);

        return cupon;
    }

    private String generarCodigoUnico() {
        String codigo;
        do {
            StringBuilder sb = new StringBuilder("FID-");
            for (int i = 0; i < 6; i++) {
                sb.append(ALFABETO_CODIGO.charAt(random.nextInt(ALFABETO_CODIGO.length())));
            }
            codigo = sb.toString();
        } while (cuponRepository.findByCodigoIgnoreCase(codigo).isPresent());
        return codigo;
    }
}
