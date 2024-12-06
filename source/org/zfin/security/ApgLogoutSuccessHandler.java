package org.zfin.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.util.Assert;
import org.zfin.properties.ZfinProperties;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Add the logout cleanup of datablade to the usual invalidation of session.
 */
public class ApgLogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler {

    public static final String GUEST = "GUEST_";


    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        Assert.notNull(request, "HttpServletRequest required");

        Cookie[] cookies = request.getCookies();
        if (cookies == null){
            return;
        }

        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(ApgCookieHandler.APG_ZFIN_LOGIN)) {
                if(!cookie.getValue().startsWith(GUEST)){
                    String id = GUEST + cookie.getValue();
                    String value = id.substring(0, ApgAuthenticationSuccessHandler.MAX_LENGTH_OF_APG_COOKIE);
                    cookie.setValue(value);
                    cookie.setPath(ZfinProperties.getCookiePath());
                    response.addCookie(cookie);
                }
            }
        }
        super.onLogoutSuccess(request,response,authentication);

    }

}
