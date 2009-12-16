package org.zfin.framework.presentation.tags;

import org.zfin.infrastructure.presentation.DataAliasPresentation;
import org.zfin.marker.MarkerAlias;
import org.zfin.marker.presentation.MarkerPresentation;
import org.zfin.marker.presentation.RelatedMarker;
import org.zfin.sequence.MarkerDBLink;
import org.zfin.sequence.TranscriptDBLink;
import org.zfin.sequence.presentation.DBLinkPresentation;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;


public class AttributionTag extends TagSupport {

    private Object entity;

    public int doStartTag() throws JspException {

        Object o = getEntity();
        StringBuilder linkBuffer = new StringBuilder();

        if (o == null)
            return SKIP_BODY;
        else if (o instanceof MarkerDBLink)
            linkBuffer.append(DBLinkPresentation.getAttributionLink((MarkerDBLink) o));
        else if (o instanceof TranscriptDBLink)
            linkBuffer.append(DBLinkPresentation.getAttributionLink((TranscriptDBLink) o));
        else if (o instanceof MarkerAlias)
            linkBuffer.append(DataAliasPresentation.getAttributionLink((MarkerAlias) o));
        else if (o instanceof RelatedMarker)
            linkBuffer.append(MarkerPresentation.getAttributionLink((RelatedMarker) o));
        else throw new JspException("Tag is not yet implemented for a class of type " + o.getClass());

        try {
            pageContext.getOut().print(linkBuffer.toString());
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
