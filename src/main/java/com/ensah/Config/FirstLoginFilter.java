package com.ensah.Config;

import com.ensah.Core.dao.IAnnotateurRepository;
import com.ensah.Core.model.Annotateur;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
public class FirstLoginFilter extends OncePerRequestFilter {

    private final IAnnotateurRepository annotateurRepository;

    public FirstLoginFilter(IAnnotateurRepository annotateurRepository) {
        this.annotateurRepository = annotateurRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        // Ignorer les ressources publiques et /first-login
        if (path.startsWith("/login") || path.startsWith("/first-login") ||
                path.startsWith("/css/") || path.startsWith("/js/") ||
                path.startsWith("/images/") || path.startsWith("/webjars/") ||
                path.startsWith("/error")) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof org.springframework.security.core.userdetails.User) {
            String login = auth.getName();
            Optional<Annotateur> opt = annotateurRepository.findByLogin(login);

            if (opt.isPresent()) {
                Annotateur a = opt.get();
                if (!a.isPasswordChanged()) {
                    // Bloquer et rediriger vers la page de première connexion
                    response.sendRedirect(request.getContextPath() + "/first-login?token=" + a.getFirstLoginToken());
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}