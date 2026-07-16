package org.zfin.infrastructure.presentation;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Serves the shared page "chrome" (site header and footer) as standalone HTML
 * fragments so statically-served pages (see the zfbook conversion) can fetch
 * and inject a live, login-aware header/footer at runtime.
 *
 * These render the same {@code <z:pageHeader/>} / {@code <z:pageFooter/>} tag
 * files used by page.tag, so there remains a single source of truth. Because
 * the endpoints run inside Tomcat under the /action servlet, the session cookie
 * is present and dynamic bits (Sign In vs. user menu, root Curation menu,
 * release number, copyright year) resolve correctly per request.
 *
 * Model attributes used by the header/footer (currentUser, releaseNumber,
 * copyrightYear, ...) are populated globally by PageLayoutControllerAdvice.
 *
 * Effective URLs (the DispatcherServlet is mapped at /action/*):
 *   GET /action/layout/header
 *   GET /action/layout/footer
 */
@Controller
@RequestMapping("/layout")
public class ChromeController {

    @RequestMapping("/header")
    public String header(HttpServletResponse response) {
        cacheBriefly(response);
        return "layout/chrome-header";
    }

    @RequestMapping("/footer")
    public String footer(HttpServletResponse response) {
        cacheBriefly(response);
        return "layout/chrome-footer";
    }

    /**
     * Cache the chrome fragments in the user's browser for a short window.
     *
     * <p>{@code private} keeps them out of any shared/proxy cache -- the header
     * is login-aware (Sign In vs. user menu, root Curation menu), so one user's
     * copy must never be served to another. {@code Vary: Cookie} keys the cache
     * entry on the request cookies, so logging in or out misses the cache and
     * re-fetches a fresh fragment: the {@code zfin_login} cookie (Path=/, so it is
     * sent with the fetch) changes value on login (session-derived) and on logout
     * (GUEST-prefixed, see Apg{Authentication,Logout}SuccessHandler), and the
     * JSESSIONID rotates on login -- either shift changes the Cookie header and
     * therefore the cache key.
     */
    private static void cacheBriefly(HttpServletResponse response) {
        response.setHeader("Cache-Control", "private, max-age=300");
        response.addHeader("Vary", "Cookie");
    }

}
