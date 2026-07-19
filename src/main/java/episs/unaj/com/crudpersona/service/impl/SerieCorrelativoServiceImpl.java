package episs.unaj.com.crudpersona.service.impl;

import episs.unaj.com.crudpersona.entity.SerieCorrelativo;
import episs.unaj.com.crudpersona.entity.TipoComprobante;
import episs.unaj.com.crudpersona.repository.SerieCorrelativoRepository;
import episs.unaj.com.crudpersona.service.SerieCorrelativoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SerieCorrelativoServiceImpl implements SerieCorrelativoService {

    @Autowired
    private SerieCorrelativoRepository serieCorrelativoRepository;

    @Override
    @Transactional
    public int siguienteNumero(TipoComprobante tipoComprobante) {
        SerieCorrelativo serie = serieCorrelativoRepository.findWithLockByTipoComprobante(tipoComprobante);
        if (serie == null) {
            serie = new SerieCorrelativo(tipoComprobante);
        }
        int siguiente = serie.getUltimoNumero() + 1;
        serie.setUltimoNumero(siguiente);
        serieCorrelativoRepository.save(serie);
        return siguiente;
    }
}
