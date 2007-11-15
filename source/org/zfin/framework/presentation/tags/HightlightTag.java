package org.zfin.framework.presentation.tags;

import org.apache.commons.lang.StringUtils;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;

/**
 * This tag is meant to hightlight (bold face) characters in a string that match a given search string.
 * <p/>
 * Usage in a JSP page:
 * <p/>
 * <zfin:hightlight name="ao" property="formattedSynonymList" hightlightName="anatomyForm"
 * hightlightProperty="searchTerm"/>
 * name: name of the bean of the string to be hightlighted and displayed
 * property: attribute on the bean (if a primitive type or string ;eave it out)
 * hightlightName: bean name for the search string
 * hightlightProperty: attribute name on the hightlightName bean. This string is used to match in the name.property
 * string.s
 */
public class HightlightTag extends TagSupport {

    private String highlightEntity;
    private String hightlightString;

    public int doStartTag() throws JspException {

        String val = StringUtils.replace(highlightEntity, hightlightString, "<B>" + hightlightString + "</B>", -1);
        try {
            pageContext.getOut().print(val);
        } catch (IOException ioe) {
            throw new JspException("Error: IOException while writing to client" + ioe.getMessage());
        }
        return EVAL_PAGE;
    }

    public void release() {
        super.release();
        highlightEntity = null;
        hightlightString = null;
    }


    public String getHighlightEntity() {
        return highlightEntity;
    }

    public void setHighlightEntity(String highlightEntity) {
        this.highlightEntity = highlightEntity;
    }

    public String getHightlightString() {
        return hightlightString;
    }

    public void setHightlightString(String hightlightString) {
        this.hightlightString = hightlightString;
    }
}
