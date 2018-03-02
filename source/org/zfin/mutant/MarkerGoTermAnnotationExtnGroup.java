package org.zfin.mutant;

import java.io.Serializable;
import java.util.Set;

/**
 */
public class MarkerGoTermAnnotationExtnGroup implements Serializable {
private  Long id;
    private MarkerGoTermEvidence mgtaegMarkerGoEvidence;

    private Set<MarkerGoTermAnnotationExtn> mgtAnnoExtns;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MarkerGoTermEvidence getMgtaegMarkerGoEvidence() {
        return mgtaegMarkerGoEvidence;
    }

    public void setMgtaegMarkerGoEvidence(MarkerGoTermEvidence mgtaegMarkerGoEvidence) {
        this.mgtaegMarkerGoEvidence = mgtaegMarkerGoEvidence;
    }

    public Set<MarkerGoTermAnnotationExtn> getMgtAnnoExtns() {
        return mgtAnnoExtns;
    }

    public void setMgtAnnoExtns(Set<MarkerGoTermAnnotationExtn> mgtAnnoExtns) {
        this.mgtAnnoExtns = mgtAnnoExtns;
    }
}
