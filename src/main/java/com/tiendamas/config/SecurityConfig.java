package com.tiendamas.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private RoleBasedAuthSuccessHandler roleBasedAuthSuccessHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Solo se guarda la URL original si es un GET normal y no es la página de error.
     * Sin este filtro, una petición POST cuyo CSRF quedó vencido (típicamente porque la
     * sesión expiró mientras el usuario llenaba un formulario) termina reenviada
     * internamente a "/error" antes de que Spring Security guarde el request — y eso es
     * lo que se guardaba y se reproducía después de loguearse, mandando al usuario
     * directo a la pantalla de error apenas iniciaba sesión con éxito.
     */
    @Bean
    public RequestCache requestCache() {
        HttpSessionRequestCache requestCache = new HttpSessionRequestCache();
        requestCache.setRequestMatcher(request ->
                "GET".equalsIgnoreCase(request.getMethod()) && !request.getRequestURI().contains("/error"));
        return requestCache;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/css/**", "/js/**", "/uploads/**", "/login", "/registro",
                                "/robots.txt", "/sitemap.xml").permitAll()

                        .requestMatchers("/tienda/checkout/**", "/tienda/pedidos/**", "/tienda/perfil/**",
                                "/tienda/productos/*/resenas", "/tienda/fidelidad/**").hasRole("CLIENTE")
                        .requestMatchers("/tienda/**").permitAll()

                        .requestMatchers("/pos/**").hasAnyRole("VENDEDOR", "ADMIN")

                        // El POS permite registrar un cliente nuevo al vuelo durante una venta
                        // (ver pos/index.html, link "Regístralo aquí"), así que un VENDEDOR
                        // necesita poder llegar al formulario y guardarlo aunque /personas/**
                        // en general sea solo de ADMIN.
                        .requestMatchers(HttpMethod.GET, "/personas/nuevo").hasAnyRole("VENDEDOR", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/personas").hasAnyRole("VENDEDOR", "ADMIN")

                        .requestMatchers("/dashboard", "/personas/**", "/categorias/**", "/productos/**", "/reportes/**",
                                "/gastos/**", "/sueldos/**", "/usuarios/**", "/suscriptores/**", "/devoluciones/**",
                                "/contenido/**").hasRole("ADMIN")
                        .requestMatchers("/pedidos", "/pedidos/nuevo", "/pedidos/*/eliminar", "/pedidos/*/estado").hasRole("ADMIN")

                        .requestMatchers("/pedidos/**").authenticated()

                        .anyRequest().authenticated()
                )
                .requestCache(cache -> cache.requestCache(requestCache()))
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler(roleBasedAuthSuccessHandler)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }
}
