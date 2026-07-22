package com.tiendamas.util;

import com.tiendamas.entity.SerieCorrelativo;
import com.tiendamas.entity.TipoComprobante;
import com.tiendamas.repository.SerieCorrelativoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class SerieCorrelativoSeeder implements CommandLineRunner {

    @Autowired
    private SerieCorrelativoRepository serieCorrelativoRepository;

    @Override
    public void run(String... args) {
        for (TipoComprobante tipo : TipoComprobante.values()) {
            if (!serieCorrelativoRepository.existsById(tipo)) {
                serieCorrelativoRepository.save(new SerieCorrelativo(tipo));
            }
        }
    }
}
