package org.zfin.marker.presentation;

import org.zfin.sequence.MarkerDBLink;

public class RelatedMarkerDBLinkDisplay implements Comparable<RelatedMarkerDBLinkDisplay> {

    private String relationshipType;
    private MarkerDBLink link;

    public String getRelationshipType() {
        return relationshipType;
    }

    public void setRelationshipType(String relationshipType) {
        this.relationshipType = relationshipType;
    }

    public MarkerDBLink getLink() {
        return link;
    }

    public void setLink(MarkerDBLink link) {
        this.link = link;
    }

    @Override
    public int compareTo(RelatedMarkerDBLinkDisplay o) {
        int result = relationshipType.compareTo(o.getRelationshipType());
        if (result != 0) {
            return result;
        }

        result = link.compareTo(o.getLink());
        if (result != 0) {
            return result;
        }

        return 0;
    }
}
