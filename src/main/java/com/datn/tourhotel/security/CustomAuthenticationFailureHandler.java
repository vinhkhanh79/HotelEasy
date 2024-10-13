package com.datn.tourhotel.security;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        String language = request.getParameter("language");
        String redirectUrl = "/login?error";

        if (language != null && !language.isEmpty()) {
            redirectUrl += "&language=" + language;
        }

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
