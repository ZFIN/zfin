package org.zfin.framework.filter;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by IntelliJ IDEA.
 * User: Christian Pich
 * Date: Oct 13, 2006
 * Time: 12:02:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class HtmlCleanupResponse extends HttpServletResponseWrapper {

    HttpServletResponse response = null;

    boolean stream = false; // Wrap our own output stream

    public HtmlCleanupResponse(HttpServletResponse response) {
        this(response, false);
    }

    public HtmlCleanupResponse(HttpServletResponse response, boolean stream) {
        super(response);
        this.response = response;
        this.stream = stream;
    }

    public ServletOutputStream getOutputStream() throws IOException {
        return (new HtmlCleanupOutputStream(response.getOutputStream()));
    }

    public PrintWriter getWriter() throws IOException {
        if (stream)
            return (new PrintWriter(getOutputStream(), true));
        else
            return (new HtmlCleanupWriter(response.getWriter()));
    }

}
