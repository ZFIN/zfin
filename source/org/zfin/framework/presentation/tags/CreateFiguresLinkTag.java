package org.zfin.framework.presentation.tags;

import org.zfin.anatomy.AnatomyItem;
import org.zfin.marker.Marker;
import org.zfin.properties.ZfinProperties;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;
import java.text.ChoiceFormat;
import java.util.Collection;

/**
 * Creates a hyperlink for the
 * 1) Anatomy term page
 * 2) Gene page
 */
public class CreateFiguresLinkTag extends TagSupport {

    private Marker marker;
    private AnatomyItem anatomyItem;
    private long numberOfFigures = -1;
    private Collection numberOfFiguresCollection;
    private String author;
    private boolean useGeneZdbID;

    public int doStartTag() throws JspException {

        StringBuilder hyperLink = getWebdriverHyperLinkStart();
        getDefaultQueryString(hyperLink);
        if (useGeneZdbID) {
            hyperLink.append("&xpatsel_geneZdbId=");
            hyperLink.append(marker.getZdbID());
        }

        if (marker != null) {
            hyperLink.append("&gene_name=");
            hyperLink.append(marker.getAbbreviation());
        }

        hyperLink.append("&TA_selected_structures=");
        hyperLink.append(anatomyItem.getName());

        hyperLink.append("&xpatsel_processed_selected_structures=");
        hyperLink.append(anatomyItem.getName());

        if (author != null) {
            hyperLink.append("&authsearchtype=contains&author=");
            hyperLink.append(author);
        }

        hyperLink.append("'>");
        long numOfFigs;
        if (numberOfFiguresCollection != null)
            numOfFigs = numberOfFiguresCollection.size();
        else
            numOfFigs = numberOfFigures;
        hyperLink.append(numOfFigs);
        ChoiceFormat cf = new ChoiceFormat("0#Figures| 1#Figure| 2#Figures");
        hyperLink.append(" ");
        hyperLink.append(cf.format(numOfFigs));
        hyperLink.append("</a>");

        try {
            pageContext.getOut().print(hyperLink);
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
        hyperLink.append("&searchtype=contains");
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
        anatomyItem = null;
        numberOfFigures = 0;
    }


    protected StringBuilder getWebdriverHyperLinkStart() {
        StringBuilder sb = new StringBuilder();
        sb.append("<a href='/");
        sb.append(ZfinProperties.getWebDriver());
        return sb;
    }


    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public AnatomyItem getAnatomyItem() {
        return anatomyItem;
    }

    public void setAnatomyItem(AnatomyItem anatomyItem) {
        this.anatomyItem = anatomyItem;
    }

    public long getNumberOfFigures() {
        return numberOfFigures;
    }

    public void setNumberOfFigures(long numberOfFigures) {
        this.numberOfFigures = numberOfFigures;
    }


    public Collection getNumberOfFiguresCollection() {
        return numberOfFiguresCollection;
    }

    public void setNumberOfFiguresCollection(Collection numberOfFiguresCollection) {
        this.numberOfFiguresCollection = numberOfFiguresCollection;
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
}
