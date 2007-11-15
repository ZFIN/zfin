package org.zfin.framework.presentation.tags;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;
import java.util.Collection;

/**
 * User: giles
 * Date: Sep 13, 2006
 * Time: 3:50:25 PM
 */

/**
 * This tag class needs to be expanded to support a "type" attribute which allows it to be used for any
 * bean/collection combination.  Right now, it only works for OrthologyItem.  See the logic:iterate
 * source code for an example of how to use the type parameter in the tag class.  Also, the
 * collectionName attribute is currently required for this tag, but it is not used in this class.
 * This attribute will be necessary in the future in order to generalize the tag.
 */

public class CreateDelimitedListTag extends TagSupport {

    private Object collectionEntity;
    private String delimiter;

    public int doStartTag() throws JspException {

        Collection items;
        if (!(collectionEntity instanceof Collection))
            throw new JspException("Error: bean needs to refer to a collection");

        items = (Collection) collectionEntity;

        StringBuilder result = new StringBuilder("");
        for (Object currentItem : items) {
            result.append(currentItem.toString());
            result.append(delimiter);
        }
        int index = result.lastIndexOf(delimiter);
        if (index > -1)
            result.delete(index, result.length());

        try {
            pageContext.getOut().print(result.toString());
        } catch (IOException ioe) {
            throw new JspException("Error: IOException while writing to client" + ioe.getMessage());
        }


        return EVAL_PAGE;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }


    public Object getCollectionEntity() {
        return collectionEntity;
    }

    public void setCollectionEntity(Object collectionEntity) {
        this.collectionEntity = collectionEntity;
    }
}
