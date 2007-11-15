package org.zfin.framework.filter;

import javax.servlet.ServletOutputStream;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: Christian Pich
 * Date: Oct 13, 2006
 * Time: 12:03:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class HtmlCleanupOutputStream extends ServletOutputStream{
    ServletOutputStream stream = null;

    public HtmlCleanupOutputStream(ServletOutputStream stream)
      throws IOException {
        super();
        this.stream = stream;
    }

    public void write(int c) throws IOException {
        char ch = (char) c;
        if (ch == '\r' || ch == '\t')
            ch = ' ';
        stream.write((int) ch);
    }

    public void write(byte buf[], int off, int len) throws IOException {
        for (int i = off; i < (off + len); i++) {
            char ch = (char) buf[i];
            if (ch == '\r' || ch == '\t')
                ch = ' ';
            stream.write((int) ch);
        }
    }

    public void write(byte buf[]) throws IOException {
        write(buf, 0, buf.length);
    }

}
