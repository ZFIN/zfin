package org.zfin.framework.presentation.tags;

import org.zfin.marker.Marker;
import org.zfin.marker.presentation.MarkerPresentation;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;

/**
 * Tag that creates a abbreviation html tag for an object of type provided.
 */
public class CreateAbbrevTag extends TagSupport {

    private Object entity;

    public int doStartTag() throws JspException {

        Object o = getEntity();
        String link;
        if (o instanceof Marker)
            link = MarkerPresentation.getAbbreviation((Marker) o);
        else
            throw new JspException("Tag is not yet implemented for a class of type " + o.getClass());

        try {
            pageContext.getOut().print(link);
        } catch (IOException ioe) {
            throw new JspException("Error: IOException while writing to client" + ioe.getMessage());
        }
        return SKIP_BODY;
    }

    public int doEndTag() throws JspException {
        return Tag.EVAL_PAGE;
    }


    public Object getEntity() {
        return entity;
    }

    public void setEntity(Object entity) {
        this.entity = entity;
    }
}
