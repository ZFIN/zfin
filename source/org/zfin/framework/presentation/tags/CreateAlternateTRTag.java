package org.zfin.framework.presentation.tags;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.jstl.core.LoopTagStatus;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;

/**
 * Create the TR tag for alternating records, e.g.
 * different css styles depending if the index is
 * even or odd.
 */
public class CreateAlternateTRTag extends TagSupport {

    /**
     * Default loop name is loop.
     */
    private String loopName;


    public int doStartTag() throws JspException {

        LoopTagStatus loopIndex = (LoopTagStatus) pageContext.getAttribute(loopName, PageContext.PAGE_SCOPE);
        if (loopIndex == null)
            throw new RuntimeException("No counter named " + loopName + " being found in page context");
        int count = loopIndex.getCount();
        StringBuilder sb = new StringBuilder();
        if (count % 2 != 0)
            sb.append("<tr class=\"odd\">");
        else
            sb.append("<tr>");
        
        try {
            pageContext.getOut().print(sb.toString());
        } catch (IOException ioe) {
            throw new JspException("Error: IOException while writing to client" + ioe.getMessage());
        }
        return Tag.EVAL_BODY_INCLUDE;
    }

    /**
     * Close the TR tag.
     * @return value indicating if the rest of the page should be evaluated or not.
     * @throws JspException
     */
    public int doEndTag() throws JspException {
        try {
            pageContext.getOut().print("</tr>");
        } catch (IOException ioe) {
            throw new JspException("Error: IOException while writing to client" + ioe.getMessage());
        }
        release();
        return Tag.EVAL_PAGE;
    }

    /**
     * Release all allocated resources.
     */
    public void release() {
        super.release();

        loopName = null;
    }


    public String getLoopName() {
        return loopName;
    }

    public void setLoopName(String loopName) {
        this.loopName = loopName;
    }
}
