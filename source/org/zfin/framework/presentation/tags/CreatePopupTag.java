package org.zfin.framework.presentation.tags;

import org.zfin.mutant.PhenotypeStatement;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.BodyTag;
import jakarta.servlet.jsp.tagext.Tag;
import jakarta.servlet.jsp.tagext.TagSupport;
import java.io.IOException;
import java.util.Collection;

/**
 * Tag that creates a popup view of a given zfin object
 */
public class CreatePopupTag extends TagSupport {
    private Object entity;
    private String name;

    public int doStartTag() throws JspException {
        return BodyTag.EVAL_BODY_BUFFERED;
    }

    private String createPopupLinkFromSingleDomainObject(Object o) throws JspException {
        String popupLink;

        if (o instanceof PhenotypeStatement) {
            popupLink = null; //PhenotypePresentation.getPopupLink()..
        }
        else
            throw new JspException("Tag is not yet implemented for a class of type " + o.getClass());
        return popupLink;

    }


    public int doEndTag() throws JspException {
        Object o = getEntity();
        StringBuilder linkBuffer = new StringBuilder();

        if (o == null)
            return Tag.SKIP_BODY;

        if (o instanceof Collection collection) {
            int numberOfItems = collection.size();
            int index = 1;
            for (Object ob : collection) {
                linkBuffer.append(createPopupLinkFromSingleDomainObject(ob));
                if (index < numberOfItems)
                    linkBuffer.append(", ");
                index++;
            }
        } else {
            linkBuffer.append(createPopupLinkFromSingleDomainObject(o));
        }
        try {
            pageContext.getOut().print(linkBuffer.toString());
        } catch (IOException ioe) {
            throw new JspException("Error: IOException while writing to client" + ioe.getMessage());
        }

        return Tag.EVAL_PAGE;
    }

    public Object getEntity() {
        return entity;
    }

    public void setEntity(Object entity) {
        this.entity = entity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
