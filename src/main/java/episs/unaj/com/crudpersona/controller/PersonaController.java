package episs.unaj.com.crudpersona.controller;

import episs.unaj.com.crudpersona.entity.Persona;
import episs.unaj.com.crudpersona.service.PersonaService;
import org.springframework.beans.factory.annotation.Autowired;
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
        return "personas/index"; // Busca en templates/personas/index.html
    }

    // 2. MOSTRAR FORMULARIO DE NUEVA PERSONA (¡Esto arregla el error 404!)
    @GetMapping("/nuevo")
    public String mostrarFormularioCrear(Model model) {
        model.addAttribute("persona", new Persona());
        return "personas/form"; // Busca en templates/personas/form.html
    }

    // 3. GUARDAR PERSONA (CREAR O ACTUALIZAR)
    @PostMapping
    public String guardar(Persona persona) {
        personaService.guardar(persona); // Asegúrate de que tu servicio tenga el método guardar
        return "redirect:/personas";
    }

    // 4. MOSTRAR FORMULARIO DE EDICIÓN
    @GetMapping("/{id}/editar")
    public String mostrarFormularioEditar(@PathVariable("id") Long id, Model model) {
        Persona persona = personaService.obtenerPorId(id); // Asegúrate de que tu servicio tenga este método
        if (persona != null) {
            model.addAttribute("persona", persona);
            return "personas/form";
        }
        return "redirect:/personas";
    }

    // 5. ELIMINAR PERSONA
    @GetMapping("/{id}/eliminar")
    public String eliminar(@PathVariable("id") Long id) {
        personaService.eliminar(id); // Asegúrate de que tu servicio tenga el método eliminar
        return "redirect:/personas";
    }
}