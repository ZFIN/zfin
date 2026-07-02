package org.zfin.infrastructure.presentation;

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
    public String header() {
        return "layout/chrome-header";
    }

    @RequestMapping("/footer")
    public String footer() {
        return "layout/chrome-footer";
    }

}
