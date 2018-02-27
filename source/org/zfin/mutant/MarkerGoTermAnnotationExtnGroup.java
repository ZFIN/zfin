package org.zfin.mutant;

import java.io.Serializable;
import java.util.Set;

/**
 */
public class MarkerGoTermAnnotationExtnGroup implements Serializable {
private  Long id;
    private Set<MarkerGoTermEvidence> mgtaegMarkerGoEvidence;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Set<MarkerGoTermEvidence> getMgtaegMarkerGoEvidence() {
        return mgtaegMarkerGoEvidence;
    }

    public void setMgtaegMarkerGoEvidence(Set<MarkerGoTermEvidence> mgtaegMarkerGoEvidence) {
        this.mgtaegMarkerGoEvidence = mgtaegMarkerGoEvidence;
    }
}
