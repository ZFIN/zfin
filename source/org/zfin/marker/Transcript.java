package org.zfin.marker;

import org.zfin.marker.Marker;
import org.zfin.sequence.*;

import java.util.Set;

/**
 */
public class Transcript extends Marker {

    private TranscriptType transcriptType;
    private TranscriptStatus status ;
    private Set<TranscriptDBLink> transcriptDBLinks ;

    public TranscriptType getTranscriptType() {
        return transcriptType;
    }


    public void setTranscriptType(TranscriptType transcriptType) {
        this.transcriptType = transcriptType;
    }

    public TranscriptStatus getStatus() {
        return status;
    }

    public void setStatus(TranscriptStatus status) {
        this.status = status;
    }

    public Set<TranscriptDBLink> getTranscriptDBLinks() {
        return transcriptDBLinks ;
    }

    public void setTranscriptDBLinks(Set<TranscriptDBLink> transcriptDBLinks) {
        this.transcriptDBLinks = transcriptDBLinks;
    }


    public Integer getLength() {
        return TranscriptService.getTranscriptLength(this,DisplayGroup.GroupName.DISPLAYED_NUCLEOTIDE_SEQUENCE);
    }




    public String toString() {
        StringBuilder sb = new StringBuilder("Transcript");
        sb.append("\n");
        sb.append("zdbID: ").append(getZdbID());
        sb.append("\n");
        sb.append("name: ").append(getName());
        sb.append("\n");
        sb.append("symbol: ").append(getAbbreviation());
        sb.append("\n");
        sb.append("type: ").append(getMarkerType());
        sb.append("\n");
        sb.append("transcript type: ").append(transcriptType);
        sb.append("\n");
        sb.append("transcript status: ").append(status);
        sb.append("\n");
        return sb.toString();
    }
}
