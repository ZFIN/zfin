package org.zfin.framework.presentation.tags;

import org.zfin.properties.ZfinProperties;

import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.JspException;
import java.io.IOException;

/**
 * Simple class - in a scriptless jsp2 tag, you can't do a
 * <%=ZfinProperties.getWebDriver()%> - so I'm making this
 * tag to use in place of that.  Could take lots of args,
 * but it'll start simple.
 */
public class WebdriverTag extends TagSupport {

    public int doStartTag() throws JspException {

        try {
            pageContext.getOut().print(ZfinProperties.getWebDriver());
        } catch (IOException ioe) {
            throw new JspException("Error: IOException while writing to client" + ioe.getMessage());
        }

        return Tag.SKIP_BODY;
    }
}
