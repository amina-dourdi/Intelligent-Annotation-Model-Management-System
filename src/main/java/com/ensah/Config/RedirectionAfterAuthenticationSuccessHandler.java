package com.ensah.Config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RedirectionAfterAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String role = authority.getAuthority();
            if (role.equals("ROLE_ADMIN_ROLE")) {  // ← Vérifie le nom exact du rôle
                response.sendRedirect(request.getContextPath() + "/admin/dashboard");
                return;
            } else if (role.equals("ROLE_ANNOTATOR_ROLE")) {  // ← Vérifie le nom exact du rôle
                response.sendRedirect(request.getContextPath() + "/annotateur/taches");
                return;
            }
        }
        response.sendRedirect(request.getContextPath() + "/");
    }
}