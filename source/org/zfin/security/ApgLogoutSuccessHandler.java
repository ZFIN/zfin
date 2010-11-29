package org.zfin.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.util.Assert;
import org.zfin.properties.ZfinProperties;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
                if(false==cookie.getValue().startsWith(GUEST)){
                    String id = GUEST + cookie.getValue();
                    String value = id.substring(0, 19);
                    cookie.setValue(value);
                    cookie.setPath(ZfinProperties.getCookiePath());
                    response.addCookie(cookie);
                }
            }
//            if (cookie.getName().equals(ServletService.JSESSIONID)) {
//            }
        }
        super.onLogoutSuccess(request,response,authentication);

    }

}
