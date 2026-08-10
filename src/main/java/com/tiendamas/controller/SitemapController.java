package com.tiendamas.controller;

import com.tiendamas.entity.Producto;
import com.tiendamas.service.ProductoService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SitemapController {

    @Autowired
    private ProductoService productoService;

    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public String sitemap(HttpServletRequest request) {
        String baseUrl = request.getRequestURL().substring(0, request.getRequestURL().length() - request.getRequestURI().length());

        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");

        agregarUrl(xml, baseUrl + "/tienda", "daily", "1.0");
        agregarUrl(xml, baseUrl + "/tienda/info", "monthly", "0.3");

        for (Producto producto : productoService.obtenerTodos()) {
            agregarUrl(xml, baseUrl + "/tienda/productos/" + producto.getId(), "weekly", "0.8");
        }

        xml.append("</urlset>\n");
        return xml.toString();
    }

    private void agregarUrl(StringBuilder xml, String loc, String changefreq, String priority) {
        xml.append("  <url>\n");
        xml.append("    <loc>").append(escapar(loc)).append("</loc>\n");
        xml.append("    <changefreq>").append(changefreq).append("</changefreq>\n");
        xml.append("    <priority>").append(priority).append("</priority>\n");
        xml.append("  </url>\n");
    }

    private String escapar(String texto) {
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
