package com.tiendamas.service.impl;

import com.tiendamas.dto.RegistroForm;
import com.tiendamas.entity.Persona;
import com.tiendamas.entity.RolUsuario;
import com.tiendamas.entity.Usuario;
import com.tiendamas.repository.PersonaRepository;
import com.tiendamas.repository.UsuarioRepository;
import com.tiendamas.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public boolean existeUsername(String username) {
        return usuarioRepository.existsByUsername(username);
    }

    @Override
    public Optional<Usuario> buscarPorUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    @Override
    @Transactional
    public Usuario registrarCliente(RegistroForm form) {
        Persona persona = new Persona(form.getNombre(), form.getApellido(), form.getEmail(),
                form.getTelefono(), form.getDireccion(), form.getTipoDocumento(), form.getNumeroDocumento());
        personaRepository.save(persona);

        Usuario usuario = new Usuario(form.getUsername(), passwordEncoder.encode(form.getPassword()),
                RolUsuario.CLIENTE, form.getNombre(), form.getApellido());
        usuario.setPersona(persona);
        return usuarioRepository.save(usuario);
    }

    @Override
    @Transactional
    public Usuario crearUsuario(String username, String rawPassword, RolUsuario rol,
                                 String nombre, String apellido, Persona persona) {
        Usuario usuario = new Usuario(username, passwordEncoder.encode(rawPassword), rol, nombre, apellido);
        usuario.setPersona(persona);
        return usuarioRepository.save(usuario);
    }

    @Override
    public List<Usuario> obtenerEmpleados() {
        return usuarioRepository.findByRolIn(List.of(RolUsuario.VENDEDOR, RolUsuario.ADMIN));
    }

    @Override
    @Transactional
    public Usuario actualizarPerfil(String username, String nombre, String apellido) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + username));
        usuario.setNombre(nombre);
        usuario.setApellido(apellido);
        return usuarioRepository.save(usuario);
    }

    @Override
    @Transactional
    public boolean cambiarPassword(String username, String passwordActual, String passwordNueva) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + username));
        if (!passwordEncoder.matches(passwordActual, usuario.getPassword())) {
            return false;
        }
        usuario.setPassword(passwordEncoder.encode(passwordNueva));
        usuarioRepository.save(usuario);
        return true;
    }

    @Override
    public List<Usuario> obtenerTodos() {
        return usuarioRepository.findAll();
    }

    @Override
    public Usuario obtenerPorId(Long id) {
        return usuarioRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public Usuario actualizarUsuario(Long id, String nombre, String apellido, RolUsuario rol, boolean activo, String nuevaPassword) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + id));
        usuario.setNombre(nombre);
        usuario.setApellido(apellido);
        usuario.setRol(rol);
        usuario.setActivo(activo);
        if (nuevaPassword != null && !nuevaPassword.isBlank()) {
            usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        }
        return usuarioRepository.save(usuario);
    }

    @Override
    public long contarPorRol(RolUsuario rol) {
        return usuarioRepository.findByRolIn(List.of(rol)).size();
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        usuarioRepository.deleteById(id);
    }
}
