package org.zfin.framework.presentation.tags;

import org.zfin.expression.service.ExpressionSearchService;
import org.zfin.marker.Marker;
import org.zfin.ontology.Term;
import org.zfin.profile.service.ProfileService;
import org.zfin.properties.ZfinPropertiesEnum;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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

    public int doStartTag() throws JspException {
        boolean isRoot = ProfileService.isRootUser();
        String linkUrl = isRoot ? getJavaUrl() : getWebdriverUrl();
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

    private void getDefaultQueryString(StringBuilder hyperLink) {
        hyperLink.append("?MIval=aa-xpatselect.apg");
        hyperLink.append("&query_results=true");
        hyperLink.append("&START=0");
        hyperLink.append("&searchtype=equals");
        hyperLink.append("&xpatsel_calledBySelf=true");
        hyperLink.append("&mutsearchtype=contains");
        hyperLink.append("&MOsearchtype=contains");
        hyperLink.append("&include_substructures=unchecked");
        hyperLink.append("&xpatsel_jtype=ANY");
        hyperLink.append("&xpatsel_jtypeDirect=checked");
        hyperLink.append("&xpatsel_jtypePublished=checked");
        hyperLink.append("&structure_bool=and");
        hyperLink.append("&WINSIZE=25");
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
                .includeSubstructures(false)
                .build();
    }

    private String getWebdriverUrl() {
        StringBuilder url = new StringBuilder("/");
        url.append(ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value());
        getDefaultQueryString(url);
        if (useGeneZdbID) {
            url.append("&xpatsel_geneZdbId=");
            url.append(marker.getZdbID());
        }

        if (marker != null) {
            url.append("&gene_name=");
            url.append(marker.getAbbreviation());
        }

        url.append("&TA_selected_structures=");
        String aoName = term.getTermName();
        String aoTermUrlEncoded;
        try {
            aoTermUrlEncoded = URLEncoder.encode(aoName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            aoTermUrlEncoded = URLEncoder.encode(aoName);
        }
        url.append(aoTermUrlEncoded);

        url.append("&xpatsel_processed_selected_structures_names=");
        url.append(aoTermUrlEncoded);
        url.append("&xpatsel_processed_selected_structures=");
        url.append(term.getZdbID());

        if (author != null) {
            url.append("&authsearchtype=contains&author=");
            url.append(author);
        }

        if (wildtypeOnly) {
            url.append("&xpatsel_wtOnly=checked");
        }
        return url.toString();
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
}
