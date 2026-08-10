package com.tiendamas.controller;

import com.tiendamas.repository.SuscriptorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/suscriptores")
public class SuscriptorController {

    @Autowired
    private SuscriptorRepository suscriptorRepository;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("suscriptores", suscriptorRepository.findAllByOrderByFechaSuscripcionDesc());
        model.addAttribute("titulo", "Suscriptores");
        return "suscriptores/index";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id) {
        suscriptorRepository.deleteById(id);
        return "redirect:/suscriptores";
    }
}
