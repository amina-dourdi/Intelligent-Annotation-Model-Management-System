package com.ensah.Config;

import com.ensah.Core.dao.IAnnotateurRepository;
import com.ensah.Core.model.Annotateur;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final IAnnotateurRepository annotateurRepository;

    public CustomAuthenticationSuccessHandler(IAnnotateurRepository annotateurRepository) {
        this.annotateurRepository = annotateurRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        String login = authentication.getName();
        Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());

        // Si c'est un annotateur, vérifier s'il doit changer son mot de passe
        if (roles.contains("ROLE_ANNOTATOR_ROLE")) {
            Optional<Annotateur> annotateurOpt = annotateurRepository.findByLogin(login);
            if (annotateurOpt.isPresent()) {
                Annotateur a = annotateurOpt.get();
                if (!a.isPasswordChanged()) {
                    // Forcer la redirection vers la page de première connexion
                    response.sendRedirect(request.getContextPath() + "/first-login?token=" + a.getFirstLoginToken());
                    return;
                }
            }
        }

        // Redirection normale selon le rôle
        if (roles.contains("ROLE_ADMIN_ROLE")) {
            response.sendRedirect(request.getContextPath() + "/admin/dashboard");
        } else if (roles.contains("ROLE_ANNOTATOR_ROLE")) {
            response.sendRedirect(request.getContextPath() + "/annotator");
        } else {
            response.sendRedirect(request.getContextPath() + "/");
        }
    }
}