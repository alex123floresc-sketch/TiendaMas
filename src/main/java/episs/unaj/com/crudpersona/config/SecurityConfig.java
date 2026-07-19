package episs.unaj.com.crudpersona.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // Permitir acceso a carpetas de estilos estáticos sin loguearse
                        .requestMatchers("/css/**", "/js/**").permitAll()
                        // Cualquier otra vista (personas, productos, categorías) requerirá Login
                        .anyRequest().authenticated()
                )
                // Modifica este bloque dentro de tu securityFilterChain
                .formLogin(form -> form
                        .loginPage("/login")               // ⬅️ Especifica nuestra vista personalizada
                        .defaultSuccessUrl("/", true)      // Redirección al entrar con éxito
                        .failureUrl("/login?error=true")   // ⬅️ Si se equivoca, recarga con un parámetro de error
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        // Usuario administrador temporal en memoria para desarrollo rápido
        UserDetails admin = User.builder()
                .username("admin")
                .password("{noop}admin123") // "{noop}" indica que no requiere encriptación BCrypt aún
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(admin);
    }
}