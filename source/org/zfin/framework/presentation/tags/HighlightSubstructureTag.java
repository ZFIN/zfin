package org.zfin.framework.presentation.tags;

import org.apache.commons.lang3.StringUtils;
import org.zfin.mutant.PhenotypeStatement;
import org.zfin.ontology.presentation.PhenotypePresentation;
import org.zfin.util.HighlightUtil;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.tagext.Tag;
import java.io.IOException;

/** This tag class needs to be expanded to support a "type" attribute which allows it to be used for any
 bean
 */

/**
 * Tag that creates a hyper link for an object of type provided.
 */
public class HighlightSubstructureTag extends BodyTagSupport {

    private String parentStructure;
    private PhenotypeStatement phenotypeStatement;

    public int doStartTag() throws JspException {
        return EVAL_PAGE;
    }

    public String getHighlightedString() {
        String content = PhenotypePresentation.getLink(phenotypeStatement, false, false);
        StringBuilder val = new StringBuilder();
        if (!StringUtils.isEmpty(parentStructure)) {
            val.append(HighlightUtil.highlightMatchHTML(content, parentStructure, false));
        } else
            val.append(content);
        return val.toString();
    }

    public int doEndTag() throws JspException {
        StringBuilder linkBuffer = new StringBuilder();
        linkBuffer.append(getHighlightedString());
        try {
            pageContext.getOut().print(linkBuffer.toString());
        } catch (IOException ioe) {
            throw new JspException("Error: IOException while writing to client" + ioe.getMessage());
        }

        return Tag.EVAL_PAGE;
    }


    public String getParentStructure() {
        return parentStructure;
    }

    public void setParentStructure(String parentStructure) {
        this.parentStructure = parentStructure;
    }

    public PhenotypeStatement getPhenotypeStatement() {
        return phenotypeStatement;
    }

    public void setPhenotypeStatement(PhenotypeStatement phenotypeStatement) {
        this.phenotypeStatement = phenotypeStatement;
    }
}
