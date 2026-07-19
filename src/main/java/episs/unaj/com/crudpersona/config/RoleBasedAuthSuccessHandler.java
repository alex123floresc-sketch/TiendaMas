package episs.unaj.com.crudpersona.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

@Component
public class RoleBasedAuthSuccessHandler implements AuthenticationSuccessHandler {

    private final RequestCache requestCache = new HttpSessionRequestCache();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                         Authentication authentication) throws IOException, ServletException {
        // Si el usuario fue redirigido al login desde una página protegida
        // (p. ej. /tienda/checkout o /pos), lo devolvemos ahí en vez de a su home por rol.
        SavedRequest savedRequest = requestCache.getRequest(request, response);
        if (savedRequest != null) {
            response.sendRedirect(savedRequest.getRedirectUrl());
            return;
        }

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        String destino;
        if (tieneRol(authorities, "ROLE_ADMIN")) {
            destino = "/personas";
        } else if (tieneRol(authorities, "ROLE_VENDEDOR")) {
            destino = "/pos";
        } else if (tieneRol(authorities, "ROLE_CLIENTE")) {
            destino = "/tienda";
        } else {
            destino = "/";
        }
        response.sendRedirect(request.getContextPath() + destino);
    }

    private boolean tieneRol(Collection<? extends GrantedAuthority> authorities, String rol) {
        return authorities.stream().anyMatch(a -> a.getAuthority().equals(rol));
    }
}
