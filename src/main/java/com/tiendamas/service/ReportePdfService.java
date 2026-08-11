package com.tiendamas.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.w3c.dom.Document;

import java.io.ByteArrayOutputStream;
import java.util.Map;

/** Genera el PDF del reporte gerencial a partir de la plantilla reportes/reporte-pdf.html. */
@Service
public class ReportePdfService {

    @Autowired
    private SpringTemplateEngine templateEngine;

    public byte[] generarPdf(Map<String, Object> variables) {
        Context context = new Context();
        context.setVariables(variables);
        String html = templateEngine.process("reportes/reporte-pdf", context);
        Document documentoW3c = W3CDom.convert(Jsoup.parse(html));

        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.useFastMode();
        builder.withW3cDocument(documentoW3c, "");
        builder.toStream(salida);
        try {
            builder.run();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar el PDF del reporte", e);
        }
        return salida.toByteArray();
    }
}
