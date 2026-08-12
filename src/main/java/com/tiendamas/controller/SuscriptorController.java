package com.tiendamas.controller;

import com.tiendamas.entity.Suscriptor;
import com.tiendamas.repository.SuscriptorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/suscriptores")
public class SuscriptorController {

    @Autowired
    private SuscriptorRepository suscriptorRepository;

    @GetMapping
    public String listar(Model model) {
        List<Suscriptor> suscriptores = suscriptorRepository.findAllByOrderByFechaSuscripcionDesc();
        LocalDateTime hace30Dias = LocalDateTime.now().minusDays(30);
        long nuevos = suscriptores.stream()
                .filter(s -> s.getFechaSuscripcion() != null && s.getFechaSuscripcion().isAfter(hace30Dias))
                .count();
        model.addAttribute("suscriptores", suscriptores);
        model.addAttribute("suscriptoresNuevos", nuevos);
        model.addAttribute("titulo", "Suscriptores");
        return "suscriptores/index";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id) {
        suscriptorRepository.deleteById(id);
        return "redirect:/suscriptores";
    }
}
