package org.zfin.framework.filter;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: Christian Pich
 * Date: Oct 13, 2006
 * Time: 11:48:35 AM
 * To change this template use File | Settings | File Templates.
 */
public class HtmlCleanupRequest extends HttpServletRequestWrapper {

    HttpServletRequest request = null;

    public HtmlCleanupRequest(HttpServletRequest request) {
        super(request);
        this.request = request;
    }

    public ServletInputStream getInputStream() throws IOException {
        return (new HtmlCleanupInputStream(request.getInputStream()));
    }

    public BufferedReader getReader() throws IOException {
        return (new HtmlCleanupReader(request.getReader()));
    }

}
