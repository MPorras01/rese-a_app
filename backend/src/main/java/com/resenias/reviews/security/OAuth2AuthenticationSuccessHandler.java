package com.resenias.reviews.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.resenias.reviews.entity.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final String frontendUrl;

    public OAuth2AuthenticationSuccessHandler(JwtService jwtService,
                                              @Value("${app.frontend-url:http://localhost:5173}") String frontendUrl) {
        this.jwtService = jwtService;
        this.frontendUrl = frontendUrl;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserPrincipal userPrincipal)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid OAuth2 principal");
            return;
        }

        User user = userPrincipal.getUser();
        String token = jwtService.generateToken(user);
        String targetUrl = UriComponentsBuilder
            .fromUriString(frontendUrl)
            .path("/oauth2/callback")
            .queryParam("token", token)
            .build(true)
            .toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
