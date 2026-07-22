package com.tiendamas.service;

import com.tiendamas.dto.RegistroForm;
import com.tiendamas.entity.Persona;
import com.tiendamas.entity.RolUsuario;
import com.tiendamas.entity.Usuario;

import java.util.List;
import java.util.Optional;

public interface UsuarioService {
    boolean existeUsername(String username);
    Optional<Usuario> buscarPorUsername(String username);
    Usuario registrarCliente(RegistroForm form);
    Usuario crearUsuario(String username, String rawPassword, RolUsuario rol, String nombre, String apellido, Persona persona);

    List<Usuario> obtenerEmpleados();

    Usuario actualizarPerfil(String username, String nombre, String apellido);

    boolean cambiarPassword(String username, String passwordActual, String passwordNueva);

    List<Usuario> obtenerTodos();
    Usuario obtenerPorId(Long id);
    Usuario actualizarUsuario(Long id, String nombre, String apellido, RolUsuario rol, boolean activo, String nuevaPassword);
    long contarPorRol(RolUsuario rol);
    void eliminar(Long id);
}
