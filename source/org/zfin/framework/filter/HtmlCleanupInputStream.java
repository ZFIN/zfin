package org.zfin.framework.filter;

import javax.servlet.ServletInputStream;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: Christian Pich
 * Date: Oct 13, 2006
 * Time: 11:50:49 AM
 * To change this template use File | Settings | File Templates.
 */
public class HtmlCleanupInputStream extends ServletInputStream{

    ServletInputStream stream = null;

    public HtmlCleanupInputStream(ServletInputStream stream) {
        super();
        this.stream = stream;
    }


    public int read() throws IOException {
        int c = stream.read();
        if (c < 0)
            return (c);
        char ch = (char) c;
        if (ch == '\r' || ch == '\t')
            return ((int) ' ');
        else
            return c;
    }

    public int read(byte buf[], int off, int len) throws IOException {
        int n = 0;
        for (int i = off; i < (off + len); i++) {
            int c = stream.read();
            if (c < 0) {
                if (n == 0)
                    return (-1);
                break;
            }
            char ch = (char) c;
            if (ch == '\r' || ch == '\t')
                ch = ' ';
            buf[i] = (byte) ch;
            n++;
        }
        return (n);
    }

    public int read(byte buf[]) throws IOException {
        return (read(buf, 0, buf.length));
    }

    public int readLine(byte buf[], int off, int len) throws IOException {
        int n = 0;
        for (int i = off; i < (off + len); i++) {
            int c = stream.read();
            if (c < 0) {
                if (n == 0)
                    return (-1);
                break;
            }
            char ch = (char) c;
            if (ch == '\r' || ch == '\t')
                ch = ' ';
            buf[i] = (byte) ch;
            n++;
            if (ch == '\n')
                break;
        }
        return (n);
    }

}
