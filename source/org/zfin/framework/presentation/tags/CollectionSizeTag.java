package org.zfin.framework.presentation.tags;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;
import java.util.Collection;

/**
 *
 */
public class CollectionSizeTag extends TagSupport {

    private Object collectionEntity;

    public int doStartTag() throws JspException {
        long value = 0;
        if (collectionEntity != null)
            value = ((Collection) collectionEntity).size();
        try {
            pageContext.getOut().print(value);
        } catch (IOException ioe) {
            throw new JspException("Error: IOException while writing to client" + ioe.getMessage());
        }
        return EVAL_PAGE;
    }

    public void release() {
        super.release();
        collectionEntity = null;
    }


    public Object getCollectionEntity() {
        return collectionEntity;
    }

    public void setCollectionEntity(Object collectionEntity) {
        this.collectionEntity = collectionEntity;
    }
}
