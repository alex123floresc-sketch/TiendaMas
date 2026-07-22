package com.tiendamas.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private RoleBasedAuthSuccessHandler roleBasedAuthSuccessHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/js/**", "/uploads/**", "/login", "/registro").permitAll()

                        .requestMatchers("/tienda/checkout/**", "/tienda/pedidos/**", "/tienda/perfil/**").hasRole("CLIENTE")
                        .requestMatchers("/tienda/**").permitAll()

                        .requestMatchers("/pos/**").hasAnyRole("VENDEDOR", "ADMIN")

                        .requestMatchers("/personas/**", "/categorias/**", "/productos/**", "/reportes/**",
                                "/gastos/**", "/sueldos/**", "/usuarios/**").hasRole("ADMIN")
                        .requestMatchers("/pedidos", "/pedidos/nuevo", "/pedidos/*/eliminar", "/pedidos/*/estado").hasRole("ADMIN")

                        .requestMatchers("/pedidos/**").authenticated()

                        .anyRequest().authenticated()
                )
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
