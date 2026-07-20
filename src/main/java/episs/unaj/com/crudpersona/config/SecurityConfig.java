package episs.unaj.com.crudpersona.config;

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
                        // Recursos públicos y páginas de acceso
                        .requestMatchers("/css/**", "/js/**", "/login", "/registro").permitAll()

                        // Tienda en línea: checkout, perfil y "mis pedidos" son solo del cliente autenticado;
                        // el resto (catálogo y carrito) es público, como en cualquier tienda grande.
                        .requestMatchers("/tienda/checkout/**", "/tienda/pedidos/**", "/tienda/perfil/**").hasRole("CLIENTE")
                        .requestMatchers("/tienda/**").permitAll()

                        // Punto de venta: solo vendedores (y administradores, para soporte).
                        .requestMatchers("/pos/**").hasAnyRole("VENDEDOR", "ADMIN")

                        // Panel administrativo.
                        .requestMatchers("/personas/**", "/categorias/**", "/productos/**", "/reportes/**",
                                "/gastos/**", "/sueldos/**").hasRole("ADMIN")
                        .requestMatchers("/pedidos", "/pedidos/nuevo", "/pedidos/*/eliminar").hasRole("ADMIN")

                        // Ver un comprobante puntual: cualquier autenticado; el controlador valida propiedad.
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
