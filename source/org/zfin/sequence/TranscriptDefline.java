package org.zfin.sequence;

import org.zfin.sequence.DBLink;
import org.zfin.sequence.TranscriptDBLink;
import org.zfin.sequence.AbstractMarkerDefline;
import org.zfin.marker.Marker;

/**
 * Description:
 * the blast database it is found in is important <limitable?>
 *  a unique  identifier <accession> is important  <searchable>
 *  the zfin object the sequence is associated with  <searchable> is good
 *  the origin of the sequence would be *awesome* <attribution>
 *  the <chromosome> if possible is nice
 *  the sequence length is good
 *
 * Proposal: <accession*> <data id*> <blastdb> <other data info [attribution chromosome]> <length>
 * Example:  <ZFINPROT000000123*>  <ZDB-TSCRIPT-12345-1*> <unpublishedRNA> <ddx56-001 mRNA non-coding ZDB-PUB-1234-1 lg3> <154 bp>
 * Note: * is searchable.
 */
public class TranscriptDefline extends AbstractMarkerDefline {

    private TranscriptDBLink transcriptDBLink;

    public TranscriptDefline(TranscriptDBLink transcriptDBLink){
        this.transcriptDBLink = transcriptDBLink;
    }

    protected DBLink getDBLink() {
        return transcriptDBLink;
    }

    protected Marker getMarker() {
        return transcriptDBLink.getTranscript();
    }

    protected StringBuilder createSpecificInformation(DBLink dbLink, Marker marker, StringBuilder stringBuilder) {
        stringBuilder.append(" ") ;
        stringBuilder.append(transcriptDBLink.getTranscript().getTranscriptType().getDisplay()) ;
        if(transcriptDBLink.getTranscript().getStatus()!=null){
            stringBuilder.append(" ").append(transcriptDBLink.getTranscript().getStatus().getDisplay()) ;
        }
        return stringBuilder ;
    }
}