package org.zfin.framework.presentation.tags;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspWriter;
import jakarta.servlet.jsp.tagext.SimpleTagSupport;
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
