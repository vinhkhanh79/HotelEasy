package com.datn.tourhotel.security;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private static final Logger log = LoggerFactory.getLogger(CustomAuthenticationFailureHandler.class);

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        String language = request.getParameter("language");
        String redirectUrl = "/login?error";

        // Log chi tiết lỗi đăng nhập
        log.error("Authentication failed: " + exception.getMessage(), exception);

        if (language != null && !language.isEmpty()) {
            redirectUrl += "&language=" + language;
        }

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
