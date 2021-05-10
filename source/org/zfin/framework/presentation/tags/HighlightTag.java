package org.zfin.framework.presentation.tags;

import org.apache.commons.lang.StringUtils;
import org.zfin.util.HighlightUtil;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;
import java.util.List;

/**
 * This tag is meant to highlight (bold face) characters in a string that match a given search string.
 * <p/>
 * Usage in a JSP page:
 * <p/>
 * <zfin:highlight name="ao" property="formattedSynonymList" highlightName="anatomyForm"
 * highlightProperty="searchTerm"/>
 * name: name of the bean of the string to be highlighted and displayed
 * property: attribute on the bean (if a primitive type or string ;eave it out)
 * highlightName: bean name for the search string
 * highlightProperty: attribute name on the highlightName bean. This string is used to match in the name.property
 * string.
 */
public class HighlightTag extends TagSupport {

    private String highlightEntity;
    private String[] highlightEntities;
    private String highlightString;
    private List<String> highlightStrings;
    private boolean caseSensitive;

    public int doStartTag() throws JspException {

        String val = getHighlightedString();
        try {
            pageContext.getOut().print(val);
        } catch (IOException ioe) {
            throw new JspException("Error: IOException while writing to client" + ioe.getMessage());
        }
        return EVAL_PAGE;
    }

    public String getHighlightedString() {
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
                    val.append(HighlightUtil.highlightMatchHTML(highlightEntity, highlightString, caseSensitive));
                else if (highlightStrings != null && highlightStrings.size() > 0) {
                    String tempString = highlightEntity;
                    for (String highlightTerm : highlightStrings) {
                        tempString = HighlightUtil.highlightMatchHTML(tempString, highlightTerm, caseSensitive);
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
                        val.append(HighlightUtil.highlightMatchHTML(highlightEntities[i], highlightString, caseSensitive));
                    }
                }
            }
        }
        return val.toString();
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
