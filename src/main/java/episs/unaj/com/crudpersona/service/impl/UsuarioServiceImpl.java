package episs.unaj.com.crudpersona.service.impl;

import episs.unaj.com.crudpersona.dto.RegistroForm;
import episs.unaj.com.crudpersona.entity.Persona;
import episs.unaj.com.crudpersona.entity.RolUsuario;
import episs.unaj.com.crudpersona.entity.Usuario;
import episs.unaj.com.crudpersona.repository.PersonaRepository;
import episs.unaj.com.crudpersona.repository.UsuarioRepository;
import episs.unaj.com.crudpersona.service.UsuarioService;
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
}
