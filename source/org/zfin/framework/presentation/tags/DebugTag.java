package org.zfin.framework.presentation.tags;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * Used to include or exclude debugging info on a JSP page.
 */
public class DebugTag extends TagSupport {

    public static final String DEBUG = "debug";
    public static final String DEBUG_VALUE = "true";

    public int doStartTag() throws JspException {

        HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        String debug = request.getParameter(DEBUG);
        if (debug != null && debug.equals(DEBUG_VALUE))
            return Tag.EVAL_BODY_INCLUDE;
        else
            return Tag.SKIP_BODY;
    }

    public int doEndTag() throws JspException {
        return Tag.EVAL_PAGE;
    }

}
