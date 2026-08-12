package com.tiendamas.controller;

import com.tiendamas.dto.ComparacionKpi;
import com.tiendamas.entity.CategoriaGasto;
import com.tiendamas.entity.Gasto;
import com.tiendamas.service.GastoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/gastos")
public class GastoController {

    @Autowired
    private GastoService gastoService;

    @GetMapping
    public String listar(Model model) {
        List<Gasto> gastos = gastoService.obtenerTodos();
        model.addAttribute("gastos", gastos);
        model.addAllAttributes(calcularKpis(gastos));
        model.addAttribute("titulo", "Gastos");
        return "gastos/index";
    }

    /** KPIs del resumen: gasto del mes actual (comparado contra el mes anterior, no
     *  contra un rango libre como en Reportes, para no necesitar un filtro de fechas acá),
     *  promedio histórico, cantidad total y el total comprometido en gastos recurrentes. */
    private Map<String, Object> calcularKpis(List<Gasto> gastos) {
        YearMonth mesActual = YearMonth.now();
        YearMonth mesAnterior = mesActual.minusMonths(1);

        double totalMesActual = sumaEnMes(gastos, mesActual);
        double totalMesAnterior = sumaEnMes(gastos, mesAnterior);

        double totalHistorico = gastos.stream().mapToDouble(g -> g.getMonto() != null ? g.getMonto() : 0.0).sum();
        double gastoPromedio = gastos.isEmpty() ? 0.0 : totalHistorico / gastos.size();
        double totalRecurrente = gastos.stream()
                .filter(Gasto::isRecurrente)
                .mapToDouble(g -> g.getMonto() != null ? g.getMonto() : 0.0)
                .sum();

        Map<String, Double> gastosPorCategoria = new LinkedHashMap<>();
        for (CategoriaGasto categoria : CategoriaGasto.values()) {
            double total = gastos.stream()
                    .filter(g -> g.getCategoria() == categoria)
                    .mapToDouble(g -> g.getMonto() != null ? g.getMonto() : 0.0)
                    .sum();
            if (total > 0) {
                gastosPorCategoria.put(categoria.getEtiqueta(), total);
            }
        }

        Map<String, Object> datos = new LinkedHashMap<>();
        datos.put("totalGastoMesActual", totalMesActual);
        datos.put("comparacionGastoMes", new ComparacionKpi(totalMesActual, totalMesAnterior));
        datos.put("gastoPromedio", gastoPromedio);
        datos.put("cantidadGastos", gastos.size());
        datos.put("totalRecurrente", totalRecurrente);
        datos.put("gastosPorCategoria", gastosPorCategoria);
        return datos;
    }

    private double sumaEnMes(List<Gasto> gastos, YearMonth mes) {
        return gastos.stream()
                .filter(g -> g.getFecha() != null && YearMonth.from(g.getFecha()).equals(mes))
                .mapToDouble(g -> g.getMonto() != null ? g.getMonto() : 0.0)
                .sum();
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioCrear(Model model) {
        model.addAttribute("gasto", new Gasto());
        model.addAttribute("titulo", "Nuevo Gasto");
        return "gastos/form";
    }

    @PostMapping
    public String guardar(@Valid Gasto gasto) {
        gastoService.guardar(gasto);
        return "redirect:/gastos";
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long id, @Valid Gasto gasto) {
        gasto.setId(id);
        gastoService.guardar(gasto);
        return "redirect:/gastos";
    }

    @GetMapping("/{id}/editar")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        Gasto gasto = gastoService.obtenerPorId(id);
        if (gasto == null) {
            return "redirect:/gastos";
        }
        model.addAttribute("gasto", gasto);
        model.addAttribute("titulo", "Editar Gasto");
        return "gastos/form";
    }

    @PostMapping("/{id}/duplicar")
    public String duplicar(@PathVariable Long id) {
        gastoService.duplicar(id);
        return "redirect:/gastos";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id) {
        try {
            gastoService.eliminar(id);
        } catch (EmptyResultDataAccessException e) {
            return "redirect:/gastos?error=noEncontrado";
        }
        return "redirect:/gastos";
    }
}
