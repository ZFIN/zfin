package org.zfin.framework.filter;

import org.zfin.util.ZfinStringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;

/**
 * Request parameter strings are escaped so Informix does not choke.
 */
public class CleanupCharacterEncodingFilter implements Filter {


    static class FilteredRequest extends HttpServletRequestWrapper {

        public FilteredRequest(ServletRequest request) {
            super((HttpServletRequest) request);
        }

        public String getParameter(String paramName) {
            String value = super.getParameter(paramName);
            return ZfinStringUtils.escapeHighUnicode(value);
        }

        public String[] getParameterValues(String paramName) {
            String values[] = super.getParameterValues(paramName);
            if(values == null)
                return null;
            for (int index = 0; index < values.length; index++)
                values[index] = ZfinStringUtils.escapeHighUnicode(values[index]);
            return values;
        }

        public Map getParameterMap() {
            return super.getParameterMap();
        }

        /**
         * The default behavior of this method is to return getParameterNames()
         * on the wrapped request object.
         */
        public Enumeration getParameterNames() {
            Enumeration paramNames = super.getParameterNames();
            return paramNames;
        }

    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        filterChain.doFilter(new FilteredRequest(request), response);
        String s = "";
    }

    @Override
    public void destroy() {
    }
}
