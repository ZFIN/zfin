package org.zfin.sequence;

import org.zfin.marker.Marker;

/**
 * Description:
 * the blast database it is found in is important <limitable?>
 * a unique  identifier <accession> is important  <searchable>
 * the zfin object the sequence is associated with  <searchable> is good
 * the origin of the sequence would be *awesome* <attribution>
 * the <chromosome> if possible is nice
 * the sequence length is good
 * <p/>
 * Proposal: <accession*> <data id*> <blastdb> <other data info [attribution chromosome]> <length>
 * Example:  <ZFINPROT000000123*>  <ZDB-TSCRIPT-12345-1*> <unpublishedRNA> <ddx56-001 mRNA non-coding ZDB-PUB-1234-1 lg3> <154 bp>
 * Note: * is searchable.
 */
public class MarkerDefline extends AbstractMarkerDefline {

    private MarkerDBLink markerDBLink;

    public MarkerDefline(MarkerDBLink markerDBLink) {
        this.markerDBLink = markerDBLink;
    }

    protected DBLink getDBLink() {
        return markerDBLink;
    }

    protected Marker getMarker() {
        return markerDBLink.getMarker();
    }

    protected StringBuilder createSpecificInformation(DBLink dbLink, Marker marker, StringBuilder stringBuilder) {
        stringBuilder.append(" ");
        if (marker.getMarkerType() != null) {
            stringBuilder.append(marker.getMarkerType().getDisplayName()).append(" ");
        }
        if (marker.getPublicComments() != null) {
            stringBuilder.append(marker.getPublicComments()).append(" ");
        }
        if (marker.getLG() != null && marker.getLG().size() > 0) {
            for (String lg : marker.getLG()) {
                stringBuilder.append(lg);
            }
            stringBuilder.append(" ");
        }
        return stringBuilder;
    }
}
