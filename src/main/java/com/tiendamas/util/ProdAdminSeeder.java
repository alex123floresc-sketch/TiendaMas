package com.tiendamas.util;

import com.tiendamas.entity.RolUsuario;
import com.tiendamas.repository.UsuarioRepository;
import com.tiendamas.service.UsuarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("prod")
public class ProdAdminSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ProdAdminSeeder.class);

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private Environment environment;

    @Override
    public void run(String... args) {
        if (!usuarioRepository.findByRolIn(List.of(RolUsuario.ADMIN)).isEmpty()) {
            return;
        }

        String username = environment.getProperty("ADMIN_USERNAME");
        String password = environment.getProperty("ADMIN_PASSWORD");

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            log.warn("No hay ningun usuario ADMIN y las variables ADMIN_USERNAME/ADMIN_PASSWORD no estan definidas. "
                    + "Definelas y reinicia la aplicacion para crear el administrador inicial.");
            return;
        }

        usuarioService.crearUsuario(username, password, RolUsuario.ADMIN, "Administrador", "Sistema", null);
        log.info("Usuario ADMIN inicial '{}' creado.", username);
    }
}
