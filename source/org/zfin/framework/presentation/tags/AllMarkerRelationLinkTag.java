package org.zfin.framework.presentation.tags;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.presentation.MarkerPresentation;
import org.zfin.marker.service.MarkerService;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.Accession;
import org.zfin.sequence.MarkerDBLink;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.BodyTagSupport;
import jakarta.servlet.jsp.tagext.Tag;
import java.io.IOException;
import java.util.*;

/**
 * Creates a hyperlink for any sort of relation from a gene. FB 2385.
 *
 * @link http://zfinwinserver1/FogBUGZ/default.asp?2385
 */
public class AllMarkerRelationLinkTag extends BodyTagSupport {

    transient Logger logger = LogManager.getLogger(AllMarkerRelationLinkTag.class);

    private Accession accession;
    private boolean showParent;
    private boolean doAbbrev;

    public AllMarkerRelationLinkTag() {
    }

    public int doStartTag() throws JspException {


        StringBuilder sb = new StringBuilder();
        try {
            if (this.accession != null) {
                Collection<MarkerDBLink> markerLinks = RepositoryFactory.getSequenceRepository().getBlastableDBlinksForAccession(accession);//accession.getBlastableMarkerDBLinks();
                List<Marker> markers = new ArrayList<Marker>();
                for (MarkerDBLink link : markerLinks) {
                    if (link.getMarker() != null) {
                        markers.add(link.getMarker());
                    }
                }
                if (markers.size() > 0) {

                    if (showParent) {
                        sb.append("(");
                    }
                    Iterator<Marker> iter = markers.iterator();
                    while (iter.hasNext()) {
                        Marker marker = iter.next();
                        if (marker.isInTypeGroup(Marker.TypeGroup.GENEDOM)) {
                            sb.append(MarkerPresentation.getLink(marker));
                        } else {
                            Set<MarkerRelationship> markerRelationships = MarkerService.getRelatedGenedomMarkerRelations(marker);
                            String markerPresentationLink = MarkerPresentation.getRelationLinks(markerRelationships, doAbbrev);
                            // the very rare case where there is no gene associated with a marker
                            if (markerPresentationLink == null) {
                                markerPresentationLink = MarkerPresentation.getLink(marker);
                                if (markerPresentationLink != null) {
                                    markerPresentationLink += "[none]";
                                }
                            }
                            sb.append(markerPresentationLink);
                        }

                        if (iter.hasNext()) {
                            sb.append(", ");
                        }
                    }
                    if (showParent) {
                        sb.append(")");
                    }
                    pageContext.getOut().print(sb);
                    release();
                } else {
                    logger.info("No blastable marker found for this accession so displaying error text, instead: " + accession.getNumber());
                    return EVAL_BODY_INCLUDE;
                }
            }

        } catch (IOException ioe) {
            throw new JspException("Error: IOException while writing to client" + ioe.getMessage());
        }

        return Tag.SKIP_BODY;
    }


    public Accession getAccession() {
        return this.accession;
    }

    public void setAccession(Accession accession) {
        this.accession = accession;
    }

    public boolean getShowParent() {
        return showParent;
    }

    public void setShowParent(boolean showParent) {
        this.showParent = showParent;
    }


    public boolean getDoAbbrev() {
        return doAbbrev;
    }

    public void setDoAbbrev(boolean doAbbrev) {
        this.doAbbrev = doAbbrev;
    }
}
