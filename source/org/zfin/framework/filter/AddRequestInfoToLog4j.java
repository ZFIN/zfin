package org.zfin.framework.filter;

import org.apache.log4j.MDC;
import org.zfin.profile.Person;

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
        try {
            HttpServletRequest request = (HttpServletRequest) req;
            Map<String, String> logMap = new HashMap<String, String>(10);
            logMap.put("uri", request.getRequestURI());
            logMap.put("queryString", request.getQueryString());
            logMap.put("sessionID", request.getSession().getId());
            logMap.put("method", request.getMethod());
            logMap.put("url", request.getRequestURL().toString());
            logMap.put("user-agent", request.getHeader("user-agent"));
            logMap.put("referrer", request.getHeader("referrer"));
            logMap.put("cookie", request.getHeader("cookie"));
            // add map to mapped diagnostic context so it gets picked up by the JSONEventLayout
            MDC.put(REQUEST_MAP, logMap);
        } catch (Exception e) {
            e.printStackTrace();
            // ignore error to make sure request does not fail..
        }
        chain.doFilter(req, resp);
    }

    public void init(FilterConfig config) throws ServletException {

    }

}
