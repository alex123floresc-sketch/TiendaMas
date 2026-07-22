package com.tiendamas.controller;

import com.tiendamas.entity.RolUsuario;
import com.tiendamas.entity.Usuario;
import com.tiendamas.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public String listar(Model model) {
        List<Usuario> usuarios = usuarioService.obtenerTodos();
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("titulo", "Usuarios");
        return "usuarios/index";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioCrear(Model model) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("roles", List.of(RolUsuario.ADMIN, RolUsuario.VENDEDOR));
        model.addAttribute("titulo", "Nuevo Usuario");
        return "usuarios/form";
    }

    @PostMapping
    public String guardar(@RequestParam String username, @RequestParam String password,
                           @RequestParam RolUsuario rol, @RequestParam String nombre,
                           @RequestParam String apellido, Model model) {
        if (usuarioService.existeUsername(username)) {
            model.addAttribute("usuario", new Usuario(username, "", rol, nombre, apellido));
            model.addAttribute("roles", List.of(RolUsuario.ADMIN, RolUsuario.VENDEDOR));
            model.addAttribute("titulo", "Nuevo Usuario");
            model.addAttribute("error", "usernameDuplicado");
            return "usuarios/form";
        }
        usuarioService.crearUsuario(username, password, rol, nombre, apellido, null);
        return "redirect:/usuarios";
    }

    @GetMapping("/{id}/editar")
    public String mostrarFormularioEditar(@PathVariable("id") Long id, Model model) {
        Usuario usuario = usuarioService.obtenerPorId(id);
        if (usuario == null) {
            return "redirect:/usuarios";
        }
        model.addAttribute("usuario", usuario);
        model.addAttribute("roles", List.of(RolUsuario.ADMIN, RolUsuario.VENDEDOR, RolUsuario.CLIENTE));
        model.addAttribute("titulo", "Editar Usuario");
        return "usuarios/form";
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable("id") Long id, @RequestParam String nombre,
                              @RequestParam String apellido, @RequestParam RolUsuario rol,
                              @RequestParam(required = false) Boolean activo,
                              @RequestParam(required = false) String nuevaPassword,
                              Principal principal) {
        Usuario usuario = usuarioService.obtenerPorId(id);
        if (usuario == null) {
            return "redirect:/usuarios";
        }

        boolean esUltimoAdmin = usuario.getRol() == RolUsuario.ADMIN && usuarioService.contarPorRol(RolUsuario.ADMIN) <= 1;
        boolean intentaQuitarleAdmin = rol != RolUsuario.ADMIN || activo == null || !activo;
        if (esUltimoAdmin && intentaQuitarleAdmin) {
            return "redirect:/usuarios?error=ultimoAdmin";
        }

        usuarioService.actualizarUsuario(id, nombre, apellido, rol, activo != null, nuevaPassword);
        return "redirect:/usuarios";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable("id") Long id, Principal principal) {
        Usuario usuario = usuarioService.obtenerPorId(id);
        if (usuario == null) {
            return "redirect:/usuarios";
        }
        if (usuario.getUsername().equals(principal.getName())) {
            return "redirect:/usuarios?error=noPropio";
        }
        if (usuario.getRol() == RolUsuario.ADMIN && usuarioService.contarPorRol(RolUsuario.ADMIN) <= 1) {
            return "redirect:/usuarios?error=ultimoAdmin";
        }
        try {
            usuarioService.eliminar(id);
        } catch (DataIntegrityViolationException e) {
            return "redirect:/usuarios?error=conRelaciones";
        }
        return "redirect:/usuarios";
    }
}
