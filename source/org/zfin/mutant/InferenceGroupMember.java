package org.zfin.mutant;

import java.io.Serializable;

/**
 */
public class InferenceGroupMember implements Serializable{

    private String markerGoTermEvidenceZdbID;
    private String inferredFrom ;

    public String getMarkerGoTermEvidenceZdbID() {
        return markerGoTermEvidenceZdbID;
    }

    public void setMarkerGoTermEvidenceZdbID(String markerGoTermEvidenceZdbID) {
        this.markerGoTermEvidenceZdbID = markerGoTermEvidenceZdbID;
    }

    public String getInferredFrom() {
        return inferredFrom;
    }

    public void setInferredFrom(String inferredFrom) {
        this.inferredFrom = inferredFrom;
    }
}
