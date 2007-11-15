package org.zfin.framework.filter;

import org.apache.log4j.Logger;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.DecimalFormat;

/**
 * Filter that measures the time a request took to be processed.
 */
public class TimingLoggerFilter implements Filter {

    private static Logger LOG = Logger.getLogger("processTimes");

    public void init(FilterConfig filterConfig) throws ServletException {
        LOG.info("Start Timing Logger Filter");
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        long startTime = System.currentTimeMillis();
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        StringBuffer url = new StringBuffer(request.getRequestURI());
        String query = request.getQueryString();
        if (query != null){
            url.append("?");
            url.append(query);
        }
        filterChain.doFilter(servletRequest, servletResponse);
        long endTime = System.currentTimeMillis();
        long processTimeMilli = endTime - startTime;
        double processTimeSeconds = (double) processTimeMilli / 1000.;
        DecimalFormat format = new DecimalFormat("###.##");
        LOG.info("Process Time for:  " + url + " [" + format.format(processTimeSeconds) + " s]");
    }

    public void destroy() {
    }
}
