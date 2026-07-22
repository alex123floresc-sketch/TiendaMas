package com.tiendamas.controller;

import com.tiendamas.entity.Producto;
import com.tiendamas.repository.CategoriaRepository;
import com.tiendamas.repository.PedidoRepository;
import com.tiendamas.repository.PersonaRepository;
import com.tiendamas.repository.ProductoRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    private static final int STOCK_MINIMO = 5;

    private final ProductoRepository productoRepository;
    private final PersonaRepository personaRepository;
    private final PedidoRepository pedidoRepository;
    private final CategoriaRepository categoriaRepository;

    public HomeController(ProductoRepository productoRepository, PersonaRepository personaRepository,
                           PedidoRepository pedidoRepository, CategoriaRepository categoriaRepository) {
        this.productoRepository = productoRepository;
        this.personaRepository = personaRepository;
        this.pedidoRepository = pedidoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @GetMapping("/")
    public String index(Model model, Authentication authentication) {
        if (tieneRol(authentication, "ROLE_VENDEDOR")) {
            return "redirect:/pos";
        }
        if (tieneRol(authentication, "ROLE_CLIENTE")) {
            return "redirect:/tienda";
        }
        model.addAttribute("titulo", "Inicio");

        List<Producto> productos = productoRepository.findAll();
        double ingresosTotales = pedidoRepository.findAll().stream()
                .mapToDouble(p -> p.getTotal() != null ? p.getTotal() : 0)
                .sum();
        List<Producto> stockBajo = productos.stream()
                .filter(p -> p.getStock() != null && p.getStock() <= STOCK_MINIMO)
                .toList();

        model.addAttribute("totalProductos", productos.size());
        model.addAttribute("totalPersonas", personaRepository.count());
        model.addAttribute("totalPedidos", pedidoRepository.count());
        model.addAttribute("totalCategorias", categoriaRepository.count());
        model.addAttribute("ingresosTotales", ingresosTotales);
        model.addAttribute("stockBajo", stockBajo);

        return "home";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    private boolean tieneRol(Authentication authentication, String rol) {
        if (authentication == null) return false;
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if (authority.getAuthority().equals(rol)) {
                return true;
            }
        }
        return false;
    }
}
