package episs.unaj.com.crudpersona.service;

import episs.unaj.com.crudpersona.dto.RegistroForm;
import episs.unaj.com.crudpersona.entity.Persona;
import episs.unaj.com.crudpersona.entity.RolUsuario;
import episs.unaj.com.crudpersona.entity.Usuario;

import java.util.List;
import java.util.Optional;

public interface UsuarioService {
    boolean existeUsername(String username);
    Optional<Usuario> buscarPorUsername(String username);
    Usuario registrarCliente(RegistroForm form);
    Usuario crearUsuario(String username, String rawPassword, RolUsuario rol, String nombre, String apellido, Persona persona);

    /** Usuarios con rol VENDEDOR o ADMIN, para asignarles un sueldo. */
    List<Usuario> obtenerEmpleados();

    Usuario actualizarPerfil(String username, String nombre, String apellido);

    /** @return false si passwordActual no coincide con la contraseña actual del usuario. */
    boolean cambiarPassword(String username, String passwordActual, String passwordNueva);
}
