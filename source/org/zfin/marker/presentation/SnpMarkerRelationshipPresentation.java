package org.zfin.marker.presentation;

import org.zfin.framework.presentation.EntityPresentation;

/**
 */
public class SnpMarkerRelationshipPresentation extends MarkerRelationshipPresentation {

    public SnpMarkerRelationshipPresentation(){
        setMarkerType("SNP");
    }

    @Override
    public String getLinkWithAttributionAndOrderThis() {
        return getLinkWithAttribution();
    }

    @Override
    public String getAttributionLink() {
        StringBuilder sb = new StringBuilder();
        sb.append(" (");
        sb.append(EntityPresentation.getGeneralHyperLink("/action/marker/snp-publication-list?markerID="+getZdbId()+"&orderBy=author", "1"));
        sb.append(")");
        return sb.toString();
    }

    @Override
    public String getLink() {
        StringBuilder sb = new StringBuilder();
        String uri = "?MIval=aa-snpview.apg&OID="+getZdbId()+"&snppubs=1" ;
        sb.append(EntityPresentation.getWebdriverLink(uri, getZdbId(), "Retrieval Details"));
        return sb.toString();
    }

}
