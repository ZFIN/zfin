package org.zfin.framework.filter;

import org.apache.log4j.MDC;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Add Request info to log4j MDC (mapped diagnostic context).
 */
public class AddRequestInfoToLog4j implements Filter {

    public static final String REQUEST_MAP = "requestMap";

    public void destroy() {
    }

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException, IOException {
        HttpServletRequest request = (HttpServletRequest) req;
        Map<String, String> logMap = new HashMap<String, String>(10);
        logMap.put("uri", request.getRequestURI());
        logMap.put("queryString", request.getQueryString());
        logMap.put("sessionID", request.getSession().getId());
        logMap.put("method", request.getMethod());
        logMap.put("url", request.getRequestURL().toString());
        MDC.put(REQUEST_MAP, logMap);
        chain.doFilter(req, resp);
    }

    public void init(FilterConfig config) throws ServletException {

    }

}
