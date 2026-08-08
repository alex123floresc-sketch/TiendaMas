package com.tiendamas.util;

import com.tiendamas.entity.RolUsuario;
import com.tiendamas.entity.Usuario;
import com.tiendamas.repository.UsuarioRepository;
import com.tiendamas.service.UsuarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Si se definen las variables de entorno ADMIN_USERNAME/ADMIN_PASSWORD, este
 * componente garantiza en cada arranque que esa cuenta ADMIN exista con esa
 * contraseña exacta (la crea si no existe, o la actualiza si no coincide).
 * Así el acceso de administrador es predecible y no depende de contraseñas
 * aleatorias generadas en el log de una ejecución anterior.
 */
@Component
public class AdminPasswordSync implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminPasswordSync.class);

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private Environment environment;

    @Override
    public void run(String... args) {
        String password = environment.getProperty("ADMIN_PASSWORD");
        if (password == null || password.isBlank()) {
            return;
        }
        String username = environment.getProperty("ADMIN_USERNAME", "admin");

        Optional<Usuario> existente = usuarioRepository.findByUsername(username);
        if (existente.isPresent()) {
            Usuario usuario = existente.get();
            boolean coincide = passwordEncoder.matches(password, usuario.getPassword());
            if (!coincide || usuario.getRol() != RolUsuario.ADMIN || !usuario.isActivo()) {
                usuario.setPassword(passwordEncoder.encode(password));
                usuario.setRol(RolUsuario.ADMIN);
                usuario.setActivo(true);
                usuarioRepository.save(usuario);
                log.info("Usuario ADMIN '{}' sincronizado con las variables ADMIN_USERNAME/ADMIN_PASSWORD.", username);
            }
        } else {
            usuarioService.crearUsuario(username, password, RolUsuario.ADMIN, "Administrador", "Sistema", null);
            log.info("Usuario ADMIN '{}' creado a partir de ADMIN_USERNAME/ADMIN_PASSWORD.", username);
        }
    }
}
