package com.tiendamas.config;

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
        // SecurityConfig instala un RequestCache que solo guarda GETs seguros de repetir
        // (ver su bean "requestCache"), así que acá alcanza con leerlo y limpiarlo siempre
        // para que no quede pegado un destino viejo en logueos futuros.
        SavedRequest savedRequest = requestCache.getRequest(request, response);
        if (savedRequest != null) {
            requestCache.removeRequest(request, response);
            response.sendRedirect(savedRequest.getRedirectUrl());
            return;
        }

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        String destino;
        if (tieneRol(authorities, "ROLE_ADMIN")) {
            destino = "/dashboard";
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
