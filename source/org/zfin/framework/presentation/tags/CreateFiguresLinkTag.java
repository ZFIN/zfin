package org.zfin.framework.presentation.tags;

import org.zfin.expression.service.ExpressionSearchService;
import org.zfin.marker.Marker;
import org.zfin.ontology.Term;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;
import java.text.ChoiceFormat;

/**
 * Creates a hyperlink for the
 * 1) Anatomy term page
 * 2) Gene page
 */
public class CreateFiguresLinkTag extends TagSupport {

    private Marker marker;
    private Term term;
    private long numberOfFigures = -1;
    private String author;
    private boolean useGeneZdbID;
    private boolean wildtypeOnly = true;
    private boolean includeSubstructures = false;

    public int doStartTag() throws JspException {
        String linkUrl = getJavaUrl();
        ChoiceFormat cf = new ChoiceFormat("0#figures| 1#figure| 2#figures");
        String linkText = numberOfFigures + " " + cf.format(numberOfFigures);
        String link = "<a href='" + linkUrl + "'>" + linkText + "</a>";
        try {
            pageContext.getOut().print(link);
        } catch (IOException ioe) {
            throw new JspException("Error: IOException while writing to client" + ioe.getMessage());
        }
        release();
        return Tag.SKIP_BODY;
    }

    /**
     * Release all allocated resources.
     */
    public void release() {
        super.release();
        marker = null;
        term = null;
        numberOfFigures = 0;
        wildtypeOnly = true;
    }

    private String getJavaUrl() {
        return new ExpressionSearchService.LinkBuilder()
                .gene(marker)
                .anatomyTerm(term)
                .author(author)
                .wildtypeOnly(wildtypeOnly)
                .includeSubstructures(includeSubstructures)
                .build();
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public Term getTerm() {
        return term;
    }

    public void setTerm(Term term) {
        this.term = term;
    }

    public long getNumberOfFigures() {
        return numberOfFigures;
    }

    public void setNumberOfFigures(long numberOfFigures) {
        this.numberOfFigures = numberOfFigures;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public boolean isUseGeneZdbID() {
        return useGeneZdbID;
    }

    public void setUseGeneZdbID(boolean useGeneZdbID) {
        this.useGeneZdbID = useGeneZdbID;
    }

    public boolean isWildtypeOnly() {
        return wildtypeOnly;
    }

    public void setWildtypeOnly(boolean wildtypeOnly) {
        this.wildtypeOnly = wildtypeOnly;
    }

    public boolean isIncludeSubstructures() {
        return includeSubstructures;
    }

    public void setIncludeSubstructures(boolean includeSubstructures) {
        this.includeSubstructures = includeSubstructures;
    }
}
