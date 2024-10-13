package com.datn.tourhotel.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {
    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        String language = request.getParameter("language");
        String redirectUrl = "/login?logout"; // Default redirect URL after logout

        // Thêm tham số ngôn ngữ vào redirect
        if (language != null) {
            redirectUrl += "&language=" + language;
        }

        response.sendRedirect(redirectUrl);
    }
}
