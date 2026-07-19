package episs.unaj.com.crudpersona.controller;

import episs.unaj.com.crudpersona.entity.Persona;
import episs.unaj.com.crudpersona.service.PersonaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/personas")
public class PersonaController {

    @Autowired
    private PersonaService personaService;

    // 1. LISTAR PERSONAS
    @GetMapping
    public String listar(Model model) {
        List<Persona> lista = personaService.obtenerTodas();
        if (lista == null) {
            lista = new java.util.ArrayList<>();
        }
        model.addAttribute("personas", lista);
        model.addAttribute("titulo", "Personas");
        return "personas/index";
    }

    // 2. MOSTRAR FORMULARIO DE NUEVA PERSONA
    @GetMapping("/nuevo")
    public String mostrarFormularioCrear(Model model) {
        model.addAttribute("persona", new Persona());
        model.addAttribute("titulo", "Nueva Persona");
        return "personas/form";
    }

    // 3. GUARDAR PERSONA (CREAR O ACTUALIZAR)
    @PostMapping
    public String guardar(Persona persona) {
        personaService.guardar(persona);
        return "redirect:/personas";
    }

    // 4. VER DETALLE DE UNA PERSONA
    @GetMapping("/{id}")
    public String ver(@PathVariable("id") Long id, Model model) {
        Persona persona = personaService.obtenerPorId(id);
        if (persona == null) {
            return "redirect:/personas";
        }
        model.addAttribute("persona", persona);
        model.addAttribute("titulo", "Detalle de Persona");
        return "personas/ver";
    }

    // 5. MOSTRAR FORMULARIO DE EDICIÓN
    @GetMapping("/{id}/editar")
    public String mostrarFormularioEditar(@PathVariable("id") Long id, Model model) {
        Persona persona = personaService.obtenerPorId(id);
        if (persona != null) {
            model.addAttribute("persona", persona);
            model.addAttribute("titulo", "Editar Persona");
            return "personas/form";
        }
        return "redirect:/personas";
    }

    // 6. ELIMINAR PERSONA (POST para evitar borrados accidentales vía GET/CSRF)
    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable("id") Long id) {
        try {
            personaService.eliminar(id);
        } catch (DataIntegrityViolationException e) {
            return "redirect:/personas?error=conRelaciones";
        }
        return "redirect:/personas";
    }
}
