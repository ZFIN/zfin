package org.zfin.framework.presentation.tags;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.zfin.marker.Marker;
import org.zfin.marker.service.MarkerService;
import org.zfin.marker.presentation.MarkerPresentation;
import org.zfin.sequence.Accession;
import org.zfin.sequence.MarkerDBLink;
import org.zfin.sequence.TranscriptService;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.tagext.Tag;
import java.io.IOException;
import java.util.*;

/**
 * Creates a hyperlink for a specific type. Supported types are:
 * 1) Anatomy term page
 * 2) Gene page
 */
public class GeneEncodedRelationLinkTag extends BodyTagSupport {

    transient Logger logger = Logger.getLogger(GeneEncodedRelationLinkTag.class);

    private Accession accession;
    private boolean showParenthesis;

    public GeneEncodedRelationLinkTag() {
    }

    public void listMarkers(StringBuilder sb, Collection<Marker> markers) {
        if (CollectionUtils.isNotEmpty(markers)) {
            for (Iterator<Marker> iterator = markers.iterator(); iterator.hasNext();) {
                Marker marker = iterator.next();
                sb.append(MarkerPresentation.getLink(marker));
                if (iterator.hasNext()) {
                    sb.append(",");
                }
            }
        }
    }

    public int doStartTag() throws JspException {


        StringBuilder sb = new StringBuilder();
        try {
            if (this.accession != null) {
                Set<MarkerDBLink> markerLinks = accession.getBlastableMarkerDBLinks();
                List<Marker> markers = new ArrayList<Marker>();
                for (MarkerDBLink link : markerLinks) {
                    if (link.getMarker() != null) {
                        markers.add(link.getMarker());
                    }
                }
                if (markers.size() > 0) {

                    if (showParenthesis) {
                        sb.append("(");
                    }
                    for (Marker marker : markers) {
                        if (marker.isInTypeGroup(Marker.TypeGroup.GENEDOM)) {
                            sb.append(MarkerPresentation.getLink(marker));
                        } else if (marker.isInTypeGroup(Marker.TypeGroup.TRANSCRIPT)) {
                            Set<Marker> genes = TranscriptService.getRelatedGenesFromTranscript(marker);
                            listMarkers(sb, genes);
                        } else {
                            Set<Marker> genes = MarkerService.getRelatedSmallSegmentGenesFromClone(marker);
                            listMarkers(sb, genes);
                        }

                        if (showParenthesis) {
                            sb.append(")");
                        }
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

    public boolean getShowParenthesis() {
        return showParenthesis;
    }

    public void setShowParenthesis(boolean showParenthesis) {
        this.showParenthesis = showParenthesis;
    }


}
