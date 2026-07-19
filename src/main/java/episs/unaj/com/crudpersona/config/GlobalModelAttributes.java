package episs.unaj.com.crudpersona.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    @ModelAttribute("currentUri")
    public String currentUri(HttpServletRequest request) {
        return request.getRequestURI();
    }

    @ModelAttribute("esAdmin")
    public boolean esAdmin(Authentication authentication) {
        return tieneRol(authentication, "ROLE_ADMIN");
    }

    @ModelAttribute("esVendedor")
    public boolean esVendedor(Authentication authentication) {
        return tieneRol(authentication, "ROLE_VENDEDOR");
    }

    @ModelAttribute("esCliente")
    public boolean esCliente(Authentication authentication) {
        return tieneRol(authentication, "ROLE_CLIENTE");
    }

    @ModelAttribute("usuarioAutenticado")
    public String usuarioAutenticado(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return authentication.getName();
    }

    private boolean tieneRol(Authentication authentication, String rol) {
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return false;
        }
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if (authority.getAuthority().equals(rol)) {
                return true;
            }
        }
        return false;
    }
}
