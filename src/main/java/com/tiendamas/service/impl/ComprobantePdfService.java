package com.tiendamas.service.impl;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.tiendamas.entity.Pedido;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.w3c.dom.Document;

import java.io.ByteArrayOutputStream;

/** Genera el PDF de la boleta/factura a partir de la plantilla pedidos/boleta-pdf.html. */
@Service
public class ComprobantePdfService {

    @Autowired
    private SpringTemplateEngine templateEngine;

    public byte[] generarPdf(Pedido pedido) {
        Context context = new Context();
        context.setVariable("pedido", pedido);
        String html = templateEngine.process("pedidos/boleta-pdf", context);
        Document documentoW3c = W3CDom.convert(Jsoup.parse(html));

        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.useFastMode();
        builder.withW3cDocument(documentoW3c, "");
        builder.toStream(salida);
        try {
            builder.run();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar el PDF del pedido " + pedido.getId(), e);
        }
        return salida.toByteArray();
    }
}
