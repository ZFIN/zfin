package org.zfin.marker;

import com.fasterxml.jackson.annotation.JsonView;
import org.zfin.framework.api.View;
import org.zfin.sequence.DisplayGroup;

import org.zfin.sequence.TranscriptDBLink;
import org.zfin.sequence.service.TranscriptService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 */
public class Transcript extends Marker {
    @JsonView(View.TranscriptDetailsAPI.class)
    private TranscriptType transcriptType;

    @JsonView(View.TranscriptDetailsAPI.class)
    private TranscriptStatus status;

    private Set<TranscriptDBLink> transcriptDBLinks;
    private String ensdartId;
    private String loadId;
    private TranscriptSequence trSequence;



    public TranscriptSequence getTrSequence() {
        return trSequence;
    }

    public void setTrSequence(TranscriptSequence trSequence) {
        this.trSequence = trSequence;
    }



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
        return transcriptDBLinks;
    }

    public void setTranscriptDBLinks(Set<TranscriptDBLink> transcriptDBLinks) {
        this.transcriptDBLinks = transcriptDBLinks;
    }


    public Integer getLength() {
        return TranscriptService.getTranscriptLength(this, DisplayGroup.GroupName.DISPLAYED_NUCLEOTIDE_SEQUENCE);
    }

    public boolean isWithdrawn(){
        if(status!=null){
            return status.getStatus()== TranscriptStatus.Status.WITHDRAWN_BY_SANGER;
        }
        return false ;
    }

    public List<TranscriptDBLink> getTranscriptDBLinksForDisplayGroup(DisplayGroup.GroupName displayGroup) {
        List<TranscriptDBLink> returnDBLinks = new ArrayList<TranscriptDBLink>();
        for (TranscriptDBLink transcriptDBLink : getTranscriptDBLinks()) {
            if (transcriptDBLink.getReferenceDatabase().isInDisplayGroup(displayGroup)) {
                returnDBLinks.add(transcriptDBLink);
            }
        }
        return returnDBLinks;
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

    public String getEnsdartId() {
        return ensdartId;
    }

    public void setEnsdartId(String ensdartId) {
        this.ensdartId = ensdartId;
    }

    public String getLoadId() {
        return loadId;
    }

    public void setLoadId(String loadId) {
        this.loadId = loadId;
    }
}
