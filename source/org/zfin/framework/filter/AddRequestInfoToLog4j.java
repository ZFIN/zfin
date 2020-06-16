package org.zfin.framework.filter;

import org.apache.logging.log4j.ThreadContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.zfin.profile.service.ProfileService;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Add Request info to log4j MDC (mapped diagnostic context).
 */
public class AddRequestInfoToLog4j implements Filter {

    public static final String REQUEST_MAP = "requestMap";

    public void destroy() {
    }

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException, IOException {
        try {
            HttpServletRequest request = (HttpServletRequest) req;
            // add map to mapped diagnostic context so it gets picked up by the JSONEventLayout
            ThreadContext.put("uri", request.getRequestURI());
            if (request.getQueryString() != null) {
                ThreadContext.put("queryString", request.getQueryString());
            }
            ThreadContext.put("sessionID", request.getSession().getId());
            ThreadContext.put("method", request.getMethod());
            ThreadContext.put("url", request.getRequestURL().toString());
            ThreadContext.put("user-agent", request.getHeader("user-agent"));
            ThreadContext.put("referrer", request.getHeader("referrer"));
            ThreadContext.put("cookie", request.getHeader("cookie"));
        } catch (Exception e) {
            e.printStackTrace();
            // ignore error to make sure request does not fail..
        }
        chain.doFilter(req, resp);
    }

    public void init(FilterConfig config) throws ServletException {

    }

}
