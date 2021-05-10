package org.zfin.framework.presentation.tags;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;
import java.text.ChoiceFormat;
import java.util.Collection;

/**
 * Tag that creates the correct pluralization of a group of items.
 * 1) Provide the bean and the property on that bean that holds an integer
 * (int), (short) or (long)
 * 2) Provide the bean and a collection property. This tag handler calls
 * the .size() method on the collection to inquire the size of the collection.
 * Via the choicePattern parameter you configure how the choices are mapped.
 * The pattern needs to work with the {@link ChoiceFormat} class.
 */
public class ChoiceTag extends TagSupport {

    private Object integerEntity;
    private Object collectionEntity;
    private String choicePattern;
    private String scope;
    private boolean includeNumber;

    public int doStartTag() throws JspException {
        long value = 0;
        if (collectionEntity != null)
            value = ((Collection) collectionEntity).size();
        else if (integerEntity instanceof Integer)
            value = (Integer) integerEntity;
        else if (integerEntity instanceof Long)
            value = (Long) integerEntity;

        ChoiceFormat cf = new ChoiceFormat(choicePattern);
        String val = cf.format(value);
        try {
            if (includeNumber)
                pageContext.getOut().print(value + " " + val);
            else
                pageContext.getOut().print(val);
        } catch (IOException ioe) {
            throw new JspException("Error: IOException while writing to client" + ioe.getMessage());
        }
        return EVAL_PAGE;
    }

    public Object getIntegerEntity() {
        return integerEntity;
    }

    public void setIntegerEntity(Object integerEntity) {
        this.integerEntity = integerEntity;
    }

    public String getChoicePattern() {
        return choicePattern;
    }

    public void setChoicePattern(String choicePattern) {
        this.choicePattern = choicePattern;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }


    public Object getCollectionEntity() {
        return collectionEntity;
    }

    public void setCollectionEntity(Object collectionEntity) {
        this.collectionEntity = collectionEntity;
    }

    public void release() {

        super.release();
        choicePattern = null;
        integerEntity = null;
        collectionEntity = null;

    }


    public boolean isIncludeNumber() {
        return includeNumber;
    }

    public void setIncludeNumber(boolean includeNumber) {
        this.includeNumber = includeNumber;
    }
}
