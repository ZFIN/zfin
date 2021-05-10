package org.zfin.framework.filter;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: Christian Pich
 * Date: Oct 13, 2006
 * Time: 11:59:46 AM
 * To change this template use File | Settings | File Templates.
 */
public class HtmlCleanupReader extends BufferedReader{

    public HtmlCleanupReader(BufferedReader reader) {
        super(reader);
    }

    public int read() throws IOException {
        int c = super.read();
        if (c < 0)
            return (c);
        char ch = (char) c;
        if (ch == '\r' || ch == '\t')
            ch = ' ';
        return ((int) ch);
    }

    public int read(char buf[], int off, int len) throws IOException {
        int n = 0;
        for (int i = off; i < (off + len); i++) {
            int c = super.read();
            if (c < 0) {
                if (n == 0)
                    return (-1);
                break;
            }
            char ch = (char) c;
            if (ch == '\r' || ch == '\t')
                ch = ' ';
            buf[i] = ch;
            n++;
        }
        return (n);
    }

    public int read(char buf[]) throws IOException {
        return (read(buf, 0, buf.length));
    }

}
