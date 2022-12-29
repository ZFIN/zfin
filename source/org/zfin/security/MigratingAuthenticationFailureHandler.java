package org.zfin.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.zfin.framework.featureflag.FeatureFlagEnum;
import org.zfin.framework.featureflag.FeatureFlags;
import org.zfin.profile.service.ProfileService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class MigratingAuthenticationFailureHandler
        extends SimpleUrlAuthenticationFailureHandler {

    public static final String LAST_USERNAME_ATTEMPTED = "LAST_USERNAME_ATTEMPTED";

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception)
            throws IOException, ServletException {

        handleRedirectForExpiredPassword(request, response);
        super.onAuthenticationFailure(request, response, exception);

    }

    /**
     * If the user's password has expired, redirect them to the change password page.
     * @param response
     * @return
     */
    private void handleRedirectForExpiredPassword(HttpServletRequest request, HttpServletResponse response) {
        String username = request.getParameter(UsernamePasswordAuthenticationFilter.SPRING_SECURITY_FORM_USERNAME_KEY);
        String password = request.getParameter(UsernamePasswordAuthenticationFilter.SPRING_SECURITY_FORM_PASSWORD_KEY);
        if (FeatureFlags.isFlagEnabled(FeatureFlagEnum.REQUIRE_MODERN_PASSWORD_HASH) && ProfileService.isPasswordExpiredFor(username, password)) {
            request.getSession().setAttribute(LAST_USERNAME_ATTEMPTED, username);
            setDefaultFailureUrl("/action/profile/expired-password");
        } else {
            setDefaultFailureUrl("/action/login-redirect?error=true");
        }
    }
}