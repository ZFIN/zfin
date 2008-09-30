package org.zfin.framework.presentation.tags;

import org.apache.commons.lang.StringUtils;
import org.zfin.util.HighlightUtil;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;
import java.util.List;

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
 * string.
 */
public class HightlightTag extends TagSupport {

    private String highlightEntity;
    private String[] highlightEntities;
    private String highlightString;
    private List<String> highlightStrings;
    private boolean caseSensitive;

    public int doStartTag() throws JspException {

        StringBuilder val = new StringBuilder();
        if (!StringUtils.isEmpty(highlightEntity)) {
            if (caseSensitive) {
                if (!StringUtils.isEmpty(highlightString))
                    val.append(StringUtils.replace(highlightEntity, highlightString, "<B>" + highlightString + "</B>", -1));
                else if (highlightStrings != null && highlightStrings.size() > 0) {
                    String tempString = highlightEntity;
                    for (String highlightTerm : highlightStrings) {
                        tempString = StringUtils.replace(highlightEntity, highlightTerm, "<B>" + highlightTerm + "</B>", -1);
                    }
                    val.append(tempString);
                }
            } else {
                if (highlightString != null)
                    val.append(HighlightUtil.hightlightMatchHTML(highlightEntity, highlightString, false));
                else if (highlightStrings != null && highlightStrings.size() > 0) {
                    String tempString = highlightEntity;
                    for (String highlightTerm : highlightStrings) {
                        tempString = HighlightUtil.hightlightMatchHTML(tempString, highlightTerm, false);
                    }
                    val.append(tempString);
                }
            }
        }
        if (highlightEntities != null) {
            if (highlightEntities.length > 1) {
                val.append(highlightEntities[0]);
                for (int i = 1; i < highlightEntities.length; i++) {
                    if (caseSensitive)
                        val.append(StringUtils.replace(highlightEntities[i], highlightString,
                                "<B>" + highlightString + "</B>", -1));
                    else {
                        val.append(HighlightUtil.hightlightMatchHTML(highlightEntities[i], highlightString, false));
                    }
                }
            }
        }
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
        highlightEntities = null;
        highlightString = null;
        highlightStrings = null;
        caseSensitive = false;
    }


    public String getHighlightEntity() {
        return highlightEntity;
    }

    public void setHighlightEntity(String highlightEntity) {
        this.highlightEntity = highlightEntity;
    }

    public String getHighlightString() {
        return highlightString;
    }

    public void setHighlightString(String highlightString) {
        this.highlightString = highlightString;
    }

    public String[] getHighlightEntities() {
        return highlightEntities;
    }

    public void setHighlightEntities(String[] highlightEntities) {
        this.highlightEntities = highlightEntities;
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    public List<String> getHighlightStrings() {
        return highlightStrings;
    }

    public void setHighlightStrings(List<String> highlightStrings) {
        this.highlightStrings = highlightStrings;
    }
}
