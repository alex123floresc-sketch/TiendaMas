package com.tiendamas.service.impl;

import com.tiendamas.entity.Persona;
import com.tiendamas.entity.TipoDocumento;
import com.tiendamas.repository.PersonaRepository;
import com.tiendamas.service.PersonaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PersonaServiceImpl implements PersonaService {

    private static final String DOCUMENTO_CLIENTE_GENERICO = "00000000";

    @Autowired
    private PersonaRepository personaRepository;

    @Override
    public List<Persona> obtenerTodas() {
        return personaRepository.findAll();
    }

    @Override
    public Persona guardar(Persona persona) {
        return personaRepository.save(persona);
    }

    @Override
    public Persona obtenerPorId(Long id) {
        return personaRepository.findById(id).orElse(null);
    }

    @Override
    public void eliminar(Long id) {
        personaRepository.deleteById(id);
    }

    @Override
    public Persona obtenerOClienteGenerico() {
        return personaRepository.findByNumeroDocumento(DOCUMENTO_CLIENTE_GENERICO)
                .orElseGet(() -> {
                    Persona generico = new Persona("Público", "General", null, null, null,
                            TipoDocumento.DNI, DOCUMENTO_CLIENTE_GENERICO);
                    return personaRepository.save(generico);
                });
    }
}