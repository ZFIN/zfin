package org.zfin.mutant;

import java.io.Serializable;

/**
 */
public class InferenceGroupMember implements Serializable {

    private String markerGoTermEvidenceZdbID;
    private String inferredFrom;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InferenceGroupMember that = (InferenceGroupMember) o;

        if (inferredFrom != null ? !inferredFrom.equals(that.inferredFrom) : that.inferredFrom != null) return false;
        if (markerGoTermEvidenceZdbID != null ? !markerGoTermEvidenceZdbID.equals(that.markerGoTermEvidenceZdbID) : that.markerGoTermEvidenceZdbID != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = markerGoTermEvidenceZdbID != null ? markerGoTermEvidenceZdbID.hashCode() : 0;
        result = 31 * result + (inferredFrom != null ? inferredFrom.hashCode() : 0);
        return result;
    }
}
