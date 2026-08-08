package com.tiendamas.controller;

import com.tiendamas.entity.Producto;
import com.tiendamas.entity.VarianteProducto;
import com.tiendamas.repository.CategoriaRepository;
import com.tiendamas.repository.PedidoRepository;
import com.tiendamas.repository.PersonaRepository;
import com.tiendamas.repository.ProductoRepository;
import com.tiendamas.repository.VarianteProductoRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    private static final int STOCK_MINIMO = 5;

    private final ProductoRepository productoRepository;
    private final VarianteProductoRepository varianteProductoRepository;
    private final PersonaRepository personaRepository;
    private final PedidoRepository pedidoRepository;
    private final CategoriaRepository categoriaRepository;

    public HomeController(ProductoRepository productoRepository, VarianteProductoRepository varianteProductoRepository,
                           PersonaRepository personaRepository, PedidoRepository pedidoRepository,
                           CategoriaRepository categoriaRepository) {
        this.productoRepository = productoRepository;
        this.varianteProductoRepository = varianteProductoRepository;
        this.personaRepository = personaRepository;
        this.pedidoRepository = pedidoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    /**
     * La raíz del sitio siempre es la tienda pública, sin excepciones de rol.
     * El acceso a paneles internos (dashboard, POS, etc.) se hace por su propia
     * URL protegida, nunca a través de "/".
     */
    @GetMapping("/")
    public String index() {
        return "redirect:/tienda";
    }

    /**
     * Dashboard administrativo. La protección real ocurre en SecurityConfig
     * (hasRole ADMIN) — este método nunca debe confiar solo en la vista.
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("titulo", "Inicio");

        List<Producto> productos = productoRepository.findAll();
        double ingresosTotales = pedidoRepository.findAll().stream()
                .mapToDouble(p -> p.getTotal() != null ? p.getTotal() : 0)
                .sum();
        List<VarianteProducto> stockBajo = varianteProductoRepository.findAll().stream()
                .filter(v -> v.getStock() != null && v.getStock() <= STOCK_MINIMO)
                .sorted((a, b) -> a.getStock().compareTo(b.getStock()))
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
}
