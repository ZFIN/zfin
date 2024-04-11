package org.zfin.security;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.zfin.framework.featureflag.FeatureFlagEnum;
import org.zfin.framework.featureflag.FeatureFlags;
import org.zfin.profile.service.ProfileService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class MigratingAuthenticationFailureHandler
        extends SimpleUrlAuthenticationFailureHandler {

    public static final String LAST_USERNAME_ATTEMPTED = "LAST_USERNAME_ATTEMPTED";

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception)
            throws IOException, ServletException {

        handleRedirectForExpiredPassword(request);
        super.onAuthenticationFailure(request, response, exception);

    }

    /**
     * If the user's password has expired, redirect them to the change password page.
     */
    private void handleRedirectForExpiredPassword(HttpServletRequest request) {
        String username = request.getParameter(UsernamePasswordAuthenticationFilter.SPRING_SECURITY_FORM_USERNAME_KEY);
        if (ProfileService.isPasswordDeprecatedFor(username)) {
            request.getSession().setAttribute(LAST_USERNAME_ATTEMPTED, username);
            setDefaultFailureUrl("/action/profile/expired-password");
        } else {
            setDefaultFailureUrl("/action/login-redirect?error=true");
        }
    }
}