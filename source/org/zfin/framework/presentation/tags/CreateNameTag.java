package org.zfin.framework.presentation.tags;

import org.zfin.anatomy.AnatomyItem;
import org.zfin.anatomy.presentation.AnatomyItemPresentation;
import org.zfin.framework.presentation.RunCandidatePresentation;
import org.zfin.marker.Marker;
import org.zfin.marker.Transcript;
import org.zfin.marker.presentation.MarkerPresentation;
import org.zfin.sequence.reno.RunCandidate;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;

/**
 * Tag that creates a hyper link for an object of type provided.
 */
public class CreateNameTag extends TagSupport {

    private Object entity;

    public int doStartTag() throws JspException {

        Object o = getEntity();
        String link;
        if (o instanceof Marker || o instanceof Transcript)
            link = MarkerPresentation.getName((Marker) o);
        else if (o instanceof RunCandidate)
            link = RunCandidatePresentation.getName((RunCandidate) o);
        else if (o instanceof AnatomyItem)
            link = AnatomyItemPresentation.getName((AnatomyItem) o);
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
