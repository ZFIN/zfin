package org.zfin.framework.filter;

import java.io.PrintWriter;

/**
 * Created by IntelliJ IDEA.
 * User: Christian Pich
 * Date: Oct 13, 2006
 * Time: 12:06:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class HtmlCleanupWriter extends PrintWriter {

    public HtmlCleanupWriter(PrintWriter writer) {
        super(writer);
    }

    public void write(int c) {
        char ch = (char) c;
        if (ch == '\r' || ch == '\n')
            ch = ' ';
        super.write((int) ch);
    }

    public void write(char buf[], int off, int len) {
        boolean secondEmptyLine = false;
        for (int i = off; i < (off + len); i++) {
            char ch = buf[i];

            if ((ch == '\r' || ch == '\n') && secondEmptyLine){
                ch = ' ';
                super.write((int) ch);
                continue;
            }

            if (ch == '\n' && !secondEmptyLine)
                secondEmptyLine = true;

            if( !(ch == '\r' || ch == '\n'))
                secondEmptyLine = false;

            super.write((int) ch);
        }
    }

}
