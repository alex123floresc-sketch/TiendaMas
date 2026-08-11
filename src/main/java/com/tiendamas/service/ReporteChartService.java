package com.tiendamas.service;

import com.tiendamas.dto.ResumenMensual;
import com.tiendamas.dto.ResumenVentasPeriodo;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.text.NumberFormat;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Genera, del lado del servidor, los mismos gráficos que se ven en la pantalla de reportes
 * (con Chart.js) para poder incrustarlos como imagen en el PDF: el renderizador de PDF
 * (openhtmltopdf) solo pinta HTML/CSS estático y no ejecuta JavaScript, así que los <canvas>
 * de Chart.js nunca llegan al documento exportado si no se reemplazan por una imagen.
 */
@Service
public class ReporteChartService {

    static {
        System.setProperty("java.awt.headless", "true");
    }

    private static final Color[] PALETA = {
            new Color(0x4F, 0x46, 0xE5), new Color(0x81, 0x8C, 0xF8), new Color(0x05, 0x96, 0x69),
            new Color(0xF5, 0x9E, 0x0B), new Color(0xEF, 0x44, 0x44), new Color(0x0E, 0xA5, 0xE9),
            new Color(0x8B, 0x5C, 0xF6), new Color(0x64, 0x74, 0x8B)
    };
    private static final Color COLOR_FISICA = PALETA[0];
    private static final Color COLOR_ONLINE = PALETA[1];
    private static final Color COLOR_VENTAS = PALETA[2];
    private static final Color COLOR_GASTOS = PALETA[4];
    private static final Color COLOR_SUELDOS = PALETA[3];

    public String graficoTorta(Map<String, Double> datos, String titulo) {
        if (datos == null || datos.isEmpty()) return null;

        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        datos.forEach(dataset::setValue);

        JFreeChart chart = ChartFactory.createPieChart(titulo, dataset, true, false, false);
        PiePlot<String> plot = (PiePlot<String>) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setShadowPaint(null);
        plot.setLabelFont(plot.getLabelFont().deriveFont(10f));

        NumberFormat formatoMonto = NumberFormat.getNumberInstance();
        formatoMonto.setMaximumFractionDigits(0);
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator(
                "{0}: S/ {1} ({2})", formatoMonto, NumberFormat.getPercentInstance()));

        int i = 0;
        for (String clave : datos.keySet()) {
            plot.setSectionPaint(clave, PALETA[i % PALETA.length]);
            i++;
        }
        return aBase64Png(chart, 480, 300);
    }

    public String graficoVentasPorCanal(List<ResumenVentasPeriodo> resumen, String titulo) {
        if (resumen == null || resumen.isEmpty()) return null;

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (ResumenVentasPeriodo r : resumen) {
            dataset.addValue(r.getVentasTiendaFisica(), "Tienda física", r.getEtiqueta());
            dataset.addValue(r.getVentasOnline(), "Online", r.getEtiqueta());
        }

        JFreeChart chart = ChartFactory.createStackedBarChart(
                titulo, "", "S/", dataset, PlotOrientation.VERTICAL, true, false, false);
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.getRenderer().setSeriesPaint(0, COLOR_FISICA);
        plot.getRenderer().setSeriesPaint(1, COLOR_ONLINE);
        return aBase64Png(chart, 560, 300);
    }

    public String graficoFinanciero(List<ResumenMensual> resumen) {
        if (resumen == null || resumen.isEmpty()) return null;

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (ResumenMensual r : resumen) {
            dataset.addValue(r.getVentas(), "Ventas", r.getMes());
            dataset.addValue(r.getGastos(), "Gastos", r.getMes());
            dataset.addValue(r.getSueldos(), "Sueldos", r.getMes());
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Resumen financiero (últimos 6 meses)", "", "S/", dataset,
                PlotOrientation.VERTICAL, true, false, false);
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.getRenderer().setSeriesPaint(0, COLOR_VENTAS);
        plot.getRenderer().setSeriesPaint(1, COLOR_GASTOS);
        plot.getRenderer().setSeriesPaint(2, COLOR_SUELDOS);
        return aBase64Png(chart, 700, 320);
    }

    private String aBase64Png(JFreeChart chart, int ancho, int alto) {
        try {
            ByteArrayOutputStream salida = new ByteArrayOutputStream();
            ChartUtils.writeChartAsPNG(salida, chart, ancho, alto);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(salida.toByteArray());
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo generar el gráfico del reporte", e);
        }
    }
}
