package com.tiendamas.controller;

import com.tiendamas.dto.ComparacionKpi;
import com.tiendamas.entity.EstadoSueldo;
import com.tiendamas.entity.Sueldo;
import com.tiendamas.entity.Usuario;
import com.tiendamas.service.SueldoService;
import com.tiendamas.service.UsuarioService;
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
@RequestMapping("/sueldos")
public class SueldoController {

    @Autowired
    private SueldoService sueldoService;

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public String listar(Model model) {
        List<Sueldo> sueldos = sueldoService.obtenerTodos();
        model.addAttribute("sueldos", sueldos);
        model.addAllAttributes(calcularKpis(sueldos));
        model.addAttribute("titulo", "Sueldos");
        return "sueldos/index";
    }

    /** Igual criterio que en GastoController: comparación simple "mes actual vs. mes
     *  anterior" (según fechaPago) en vez del filtro de rango libre que usa Reportes. */
    private Map<String, Object> calcularKpis(List<Sueldo> sueldos) {
        YearMonth mesActual = YearMonth.now();
        YearMonth mesAnterior = mesActual.minusMonths(1);

        double pagadoMesActual = sumaPagadaEnMes(sueldos, mesActual);
        double pagadoMesAnterior = sumaPagadaEnMes(sueldos, mesAnterior);

        double totalPendiente = sueldos.stream()
                .filter(s -> s.getEstado() == EstadoSueldo.PENDIENTE)
                .mapToDouble(s -> s.getMonto() != null ? s.getMonto() : 0.0)
                .sum();
        long empleadosPendientes = sueldos.stream()
                .filter(s -> s.getEstado() == EstadoSueldo.PENDIENTE)
                .count();

        double totalHistorico = sueldos.stream().mapToDouble(s -> s.getMonto() != null ? s.getMonto() : 0.0).sum();
        double sueldoPromedio = sueldos.isEmpty() ? 0.0 : totalHistorico / sueldos.size();

        Map<String, Double> sueldosPorEmpleado = new LinkedHashMap<>();
        for (Sueldo s : sueldos) {
            if (s.getUsuario() == null || s.getMonto() == null) continue;
            String nombre = s.getUsuario().getNombre() + " " + s.getUsuario().getApellido();
            sueldosPorEmpleado.merge(nombre, s.getMonto(), Double::sum);
        }

        Map<String, Object> datos = new LinkedHashMap<>();
        datos.put("pagadoMesActual", pagadoMesActual);
        datos.put("comparacionSueldoMes", new ComparacionKpi(pagadoMesActual, pagadoMesAnterior));
        datos.put("totalPendiente", totalPendiente);
        datos.put("empleadosPendientes", empleadosPendientes);
        datos.put("sueldoPromedio", sueldoPromedio);
        datos.put("sueldosPorEmpleado", sueldosPorEmpleado);
        return datos;
    }

    private double sumaPagadaEnMes(List<Sueldo> sueldos, YearMonth mes) {
        return sueldos.stream()
                .filter(s -> s.getEstado() == EstadoSueldo.PAGADO
                        && s.getFechaPago() != null && YearMonth.from(s.getFechaPago()).equals(mes))
                .mapToDouble(s -> s.getMonto() != null ? s.getMonto() : 0.0)
                .sum();
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioCrear(Model model) {
        model.addAttribute("sueldo", new Sueldo());
        model.addAttribute("empleados", usuarioService.obtenerEmpleados());
        model.addAttribute("titulo", "Nuevo Sueldo");
        return "sueldos/form";
    }

    @PostMapping
    public String guardar(@RequestParam Long usuarioId, @Valid @ModelAttribute Sueldo sueldo) {
        Usuario empleado = usuarioService.obtenerEmpleados().stream()
                .filter(u -> u.getId().equals(usuarioId))
                .findFirst()
                .orElse(null);
        if (empleado == null) {
            return "redirect:/sueldos/nuevo?error=empleadoInvalido";
        }
        sueldo.setUsuario(empleado);
        sueldoService.guardar(sueldo);
        return "redirect:/sueldos";
    }

    @PostMapping("/{id}/pagar")
    public String pagar(@PathVariable Long id) {
        sueldoService.marcarPagado(id);
        return "redirect:/sueldos";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id) {
        try {
            sueldoService.eliminar(id);
        } catch (EmptyResultDataAccessException e) {
            return "redirect:/sueldos?error=noEncontrado";
        }
        return "redirect:/sueldos";
    }
}
