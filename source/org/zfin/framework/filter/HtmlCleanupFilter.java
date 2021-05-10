package org.zfin.framework.filter;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.zfin.framework.ZfinStaticLogger;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Cleanup the response to the client:
 * Remove empty lines if more than one in a row and replace with a space character.
 */
public class HtmlCleanupFilter implements Filter {

    private static final Logger LOG = LogManager.getLogger(HtmlCleanupFilter.class);

    public void init(FilterConfig filterConfig) throws ServletException {

    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest wrequest = ((HttpServletRequest) request);
        HttpServletResponse wresponse = new HtmlCleanupResponse((HttpServletResponse) response);
        if (LOG.isDebugEnabled())
            ZfinStaticLogger.write("HtmlCleanupFilter.doFilter() begin");
        chain.doFilter(wrequest, wresponse);
        if (LOG.isDebugEnabled())
            ZfinStaticLogger.write("HtmlCleanupFilter.doFilter() end");

    }

    public void destroy() {

    }
}
