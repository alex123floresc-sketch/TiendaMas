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

import java.util.List;
import java.util.Optional;

/**
 * Garantiza en cada arranque que exista una cuenta ADMIN utilizable (la crea
 * si no existe, o la corrige si no coincide). Así el acceso de administrador
 * es predecible y no depende de que otro seeder haya corrido antes ni de
 * contraseñas aleatorias perdidas en el log de una ejecución anterior.
 *
 * Si se definen ADMIN_USERNAME/ADMIN_PASSWORD, se usan esas credenciales
 * (obligatorio en el perfil "prod", ver docker-compose.yml). Fuera de "prod",
 * si no se definen, se usa admin/admin123 como valor por defecto de desarrollo.
 */
@Component
public class AdminPasswordSync implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminPasswordSync.class);
    private static final String ADMIN_PASSWORD_DEV_DEFECTO = "admin123";

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
        boolean passwordExplicita = password != null && !password.isBlank();
        boolean esProd = List.of(environment.getActiveProfiles()).contains("prod");
        if (!passwordExplicita) {
            if (esProd) {
                return; // en producción, ADMIN_PASSWORD es obligatoria (ver docker-compose.yml); no se adivina.
            }
            password = ADMIN_PASSWORD_DEV_DEFECTO;
        }
        String username = environment.getProperty("ADMIN_USERNAME", "admin");

        Optional<Usuario> existente = usuarioRepository.findByUsername(username);
        if (existente.isPresent()) {
            if (!passwordExplicita) {
                // Valor por defecto de desarrollo: no pisamos una cuenta que ya existe
                // (podría tener una contraseña que el usuario cambió a propósito).
                return;
            }
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
            log.info(passwordExplicita
                    ? "Usuario ADMIN '{}' creado a partir de ADMIN_USERNAME/ADMIN_PASSWORD."
                    : "Usuario ADMIN '{}' creado con la contraseña de desarrollo por defecto ('admin123'). "
                            + "Cambiala desde /perfil o definí ADMIN_PASSWORD.", username);
        }
    }
}
