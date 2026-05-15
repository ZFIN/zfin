package org.zfin.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.zfin.profile.service.ProfileService;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class MigratingAuthenticationFailureHandler
        extends SimpleUrlAuthenticationFailureHandler {

    public static final String LAST_USERNAME_ATTEMPTED = "LAST_USERNAME_ATTEMPTED";

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception)
            throws IOException, ServletException {

        // Determine the failure URL per-request rather than mutating the shared
        // defaultFailureUrl field, which would race across concurrent failed logins.
        String failureUrl = determineFailureUrl(request);
        saveException(request, exception);
        getRedirectStrategy().sendRedirect(request, response, failureUrl);
    }

    private String determineFailureUrl(HttpServletRequest request) {
        String username = request.getParameter(UsernamePasswordAuthenticationFilter.SPRING_SECURITY_FORM_USERNAME_KEY);
        if (ProfileService.isPasswordDeprecatedFor(username)) {
            request.getSession().setAttribute(LAST_USERNAME_ATTEMPTED, username);
            return "/action/profile/expired-password";
        }
        String redirect = request.getParameter("redirect");
        if (StringUtils.isNotEmpty(redirect)) {
            return "/action/login-redirect?error=true&redirect="
                    + URLEncoder.encode(redirect, StandardCharsets.UTF_8);
        }
        return "/action/login-redirect?error=true";
    }
}