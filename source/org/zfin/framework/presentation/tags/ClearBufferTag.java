package org.zfin.framework.presentation.tags;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;

public class ClearBufferTag extends SimpleTagSupport {
    @Override
    public void doTag() throws JspException {
        try {
            JspWriter out = getJspContext().getOut();
            out.clear();  // Clears the buffer
        } catch (IOException ioe) {
            //ignore
        }
    }
}