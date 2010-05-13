package org.zfin.framework.presentation.tags;

import org.zfin.properties.ZfinProperties;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;


public class GBrowseTag extends TagSupport {

    public int doStartTag() throws JspException {

        try {
            pageContext.getOut().print(ZfinProperties.getGBrowse());
        } catch (IOException ioe) {
            throw new JspException("Error: IOException while writing to client" + ioe.getMessage());
        }

        return Tag.SKIP_BODY;
    }
}
