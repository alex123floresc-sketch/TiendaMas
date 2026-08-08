package com.tiendamas.util;

import com.tiendamas.entity.RolUsuario;
import com.tiendamas.entity.Usuario;
import com.tiendamas.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;

/**
 * Se ejecuta en cada arranque (en cualquier perfil) y rota automáticamente
 * cualquier cuenta ADMIN/VENDEDOR que todavía use una contraseña de fábrica
 * conocida (p. ej. sembrada por versiones anteriores de DataSeeder). Evita
 * que una base de datos ya sembrada quede con credenciales adivinables.
 */
@Component
public class WeakCredentialsGuard implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(WeakCredentialsGuard.class);
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final List<String> PASSWORDS_DE_FABRICA_CONOCIDAS = List.of("admin123", "vendedor123");

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        usuarioRepository.findByRolIn(List.of(RolUsuario.ADMIN, RolUsuario.VENDEDOR))
                .forEach(this::rotarSiEsDebil);
    }

    private void rotarSiEsDebil(Usuario usuario) {
        for (String conocida : PASSWORDS_DE_FABRICA_CONOCIDAS) {
            if (passwordEncoder.matches(conocida, usuario.getPassword())) {
                String nueva = generarPasswordAleatoria();
                usuario.setPassword(passwordEncoder.encode(nueva));
                usuarioRepository.save(usuario);
                log.warn("SEGURIDAD: el usuario '{}' (rol {}) tenia una contrasena de fabrica conocida y fue "
                                + "rotada automaticamente. Contrasena temporal: {} -- inicia sesion y cambiala de inmediato.",
                        usuario.getUsername(), usuario.getRol(), nueva);
                return;
            }
        }
    }

    private String generarPasswordAleatoria() {
        byte[] bytes = new byte[15];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
