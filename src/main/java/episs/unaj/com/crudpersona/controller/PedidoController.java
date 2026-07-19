package episs.unaj.com.crudpersona.controller;

import episs.unaj.com.crudpersona.dto.PedidoForm;
import episs.unaj.com.crudpersona.service.PedidoService;
import episs.unaj.com.crudpersona.service.PersonaService;
import episs.unaj.com.crudpersona.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/pedidos")
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private PersonaService personaService;

    @Autowired
    private ProductoService productoService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("pedidos", pedidoService.obtenerTodos());
        return "pedidos/index";
    }

    @GetMapping("/nuevo")
    public String mostrarFormulario(Model model) {
        model.addAttribute("pedidoForm", new PedidoForm());
        model.addAttribute("personas", personaService.obtenerTodas());
        model.addAttribute("productos", productoService.obtenerTodos());
        return "pedidos/form";
    }

    @PostMapping
    public String guardar(@ModelAttribute PedidoForm pedidoForm) {
        pedidoService.crearPedido(pedidoForm);
        return "redirect:/pedidos";
    }

    @GetMapping("/{id}")
    public String ver(@PathVariable Long id, Model model) {
        model.addAttribute("pedido", pedidoService.obtenerPorId(id));
        return "pedidos/ver";
    }

    @GetMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id) {
        pedidoService.eliminar(id);
        return "redirect:/pedidos";
    }
}

