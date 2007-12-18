package org.zfin.framework.presentation.tags;

import org.zfin.marker.Marker;
import org.zfin.marker.MarkerService;
import org.zfin.marker.presentation.MarkerPresentation;
import org.zfin.sequence.Accession;
import org.zfin.sequence.MarkerDBLink;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;

/**
 * Creates a hyperlink for a specific type. Supported types are:
 * 1) Anatomy term page
 * 2) Gene page
 */
public class MarkerRelationLinkTag extends TagSupport {

    private Accession accession ;
    private boolean showParent;

    public int doStartTag() throws JspException {


        StringBuilder sb = new StringBuilder();
        try {
            if (this.accession!= null) {
//                Set<MarkerDBLink> markerLinks = accession.getMarkerDBLinksForMarkerTypes();
                Set<MarkerDBLink> markerLinks = accession.getBlastableMarkerDBLinks();
                List<Marker> markers = new ArrayList<Marker>() ;
                for(MarkerDBLink link : markerLinks){
                    if(link.getMarker()!=null){
                        markers.add(link.getMarker()) ;
                    }
                }

                if (showParent==true
                        && markers.size()>0) {
                    sb.append("(");
                }
                for(Marker marker: markers){
                    if (marker.isInTypeGroup(Marker.TypeGroup.GENEDOM)) {
                        sb.append(MarkerPresentation.getLink(marker));
                    } else {
                        Marker gene = MarkerService.getRelatedGeneFromClone(marker);
                        if(gene!=null){
                            sb.append(MarkerPresentation.getLink(gene));
                            sb.append(",");
                        }
                        sb.append(MarkerPresentation.getLink(marker));
                    }
                }
                if (showParent==true
                        && markers.size()>0) {
                    sb.append(")");
                }
            }

            pageContext.getOut().print(sb);
        } catch (IOException ioe) {
            throw new JspException("Error: IOException while writing to client" + ioe.getMessage());
        }
        release();
        return Tag.SKIP_BODY;
    }


    /**
     * Get showParent.
     *
     * @return showParent as boolean.
     */
    public boolean getShowParent() {
        return showParent;
    }

    /**
     * Set showParent.
     *
     * @param showParent the value to set.
     */
    public void setShowParent(boolean showParent) {
        this.showParent = showParent;
    }

    public Accession getAccession(){
        return this.accession ;
    }

    public void setAccession(Accession accession) {
        this.accession = accession;
    }
}
