package org.zfin.marker.service;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.zfin.framework.presentation.EntityPresentation;
import org.zfin.gwt.root.util.StringUtils;
import org.zfin.marker.Marker;
import org.zfin.marker.presentation.MarkerGoViewTableRow;
import org.zfin.mutant.MarkerGoTermEvidence;
import org.zfin.mutant.presentation.MarkerGoEvidencePresentation;
import org.zfin.publication.Publication;
import org.zfin.publication.presentation.PublicationPresentation;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by kschaper on 12/16/14.
 */
@Service
public class MarkerGoService {

    public static Logger log = Logger.getLogger(MarkerGoService.class);

    public List<MarkerGoViewTableRow> getMarkerGoViewTableRows(Marker marker) {
        List<MarkerGoViewTableRow> rows = new ArrayList<>();


        for (MarkerGoTermEvidence evidence  : marker.getGoTermEvidence()) {
            MarkerGoViewTableRow row = new MarkerGoViewTableRow(evidence);
            row.setInferredFrom(getInferrenceLinks(evidence));
            if (!rows.contains(row)) {
                rows.add(row);
            } else {
                for (MarkerGoViewTableRow matchingRow : rows) {
                    if (row.equals(matchingRow)) {
                        matchingRow.addPublication(evidence.getSource());
                    }
                }
            }
        }

        //once we're done with grouping, we can populate the presentation links
        for (MarkerGoViewTableRow row : rows) {
            row.setReferencesLink(getReferencesLink(row, marker));
        }

        Collections.sort(rows);

        return rows;
    }


    String getInferrenceLinks(MarkerGoTermEvidence evidence) {
        StringBuilder sb = new StringBuilder();

        if (CollectionUtils.isNotEmpty(evidence.getInferredFrom()) ) {
            for (String s : evidence.getInferencesAsString()) {
                if (org.apache.commons.lang.StringUtils.isNotEmpty(sb.toString())) { sb.append(", "); }
                sb.append(MarkerGoEvidencePresentation.generateInferenceLink(s));
            }

        }

        return sb.toString();
    }

    String getReferencesLink(MarkerGoViewTableRow row, Marker marker) {
        if (CollectionUtils.isEmpty(row.getPublications())) {
            return null;
        }

        StringBuilder sb = new StringBuilder();

        if (row.getPublications().size() > 1) {
            sb.append(getShowPubsLink(row, marker));
        } else {
            Publication publication = row.getPublications().iterator().next();
            sb.append(PublicationPresentation.getLink(publication));
        }

        return sb.toString();
    }


    /* Not working where there's more than one id in &inf */
    String getShowPubsLink(MarkerGoViewTableRow row, Marker marker) {
        if (CollectionUtils.isEmpty(row.getPublications())) { return null; }

        StringBuilder sb = new StringBuilder();
        StringBuilder uri = new StringBuilder("/cgi-bin/webdriver?MIval=aa-showpubs.apg");
        uri.append("&OID=");
        uri.append(marker.getZdbID());

        uri.append("&rtype=goview");
        uri.append("&total_count=");
        uri.append(String.valueOf(row.getPublications().size()));

        uri.append("&goterm=");
        uri.append(row.getTerm().getZdbID());

        uri.append("&title=Gene+Ontology+Pubs");
        uri.append("&name=");

        uri.append("&goterm=");
        uri.append(row.getTerm().getZdbID());

        uri.append("&evidence=");
        uri.append(row.getEvidenceCode().getCode().toUpperCase());

        uri.append("&goflag=");
        uri.append(row.getQualifier());

        uri.append("&inf=");
        uri.append(row.getFirstInference());

        uri.append("&infer=");
        uri.append(StringUtils.isNotEmpty(row.getInferredFromAsString()) ? "t" : "f");


        sb.append("<a href=\"");
        sb.append(uri.toString());
        sb.append("\">");
        sb.append(row.getPublications().size());
        sb.append(" Publications");
        sb.append("</a>");

        return sb.toString();
    }

}
