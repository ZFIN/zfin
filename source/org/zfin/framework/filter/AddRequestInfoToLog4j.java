package org.zfin.framework.filter;

import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.ThreadContext;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Add Request info to log4j MDC (mapped diagnostic context).
 */
@Log4j2
public class AddRequestInfoToLog4j implements Filter {

    public static final String REQUEST_MAP = "requestMap";

    public void destroy() {
    }

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException, IOException {
        long startTime = System.currentTimeMillis();
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
        try {
            chain.doFilter(req, resp);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            ThreadContext.put("responseTimeMs", String.valueOf(duration));
            if (resp instanceof HttpServletResponse) {
                ThreadContext.put("statusCode", String.valueOf(((HttpServletResponse) resp).getStatus()));
            }
            log.info("Request completed");
            ThreadContext.clearAll();
        }
    }

    public void init(FilterConfig config) throws ServletException {

    }

}
