package org.zfin.security;

import org.acegisecurity.Authentication;
import org.acegisecurity.ui.logout.SecurityContextLogoutHandler;
import org.springframework.util.Assert;
import org.zfin.security.repository.UserRepository;
import org.zfin.properties.ZfinProperties;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Add the logout cleanup of datablade to the usual invalidation of session.
 */
public class ZfinSecurityContextLogoutHandler extends SecurityContextLogoutHandler {

    private UserRepository ur;
    public static final String GUEST = "GUEST_";

    /**
     * Requires the request to be passed in.
     *
     * @param request        from which to obtain a HTTP session (cannot be null)
     * @param response       not used (can be <code>null</code>)
     * @param authentication not used (can be <code>null</code>)
     */
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        Assert.notNull(request, "HttpServletRequest required");
        super.logout(request, response, authentication);

        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(ZfinAuthenticationProcessingFilter.ZDB_AUTHORIZE)) {
                String id = GUEST + cookie.getValue();
                String value = id.substring(0, 19);
                cookie.setValue(value);
                cookie.setPath(ZfinProperties.getCookiePath());
                response.addCookie(cookie);
            }
            if (cookie.getName().equals(ZfinAuthenticationProcessingFilter.JSESSIONID)) {
                 ZfinAuthenticationProcessingFilter.removeSession(cookie.getValue());
            }
        }

    }

    public UserRepository getUr() {
        return ur;
    }

    public void setUr(UserRepository ur) {
        this.ur = ur;
    }
}
