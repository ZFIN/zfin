package org.zfin.mutant;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 *
 */
public class MarkerGoTermAnnotationExtnGroup implements Serializable {
    private Long id;
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

    public void addMgtAnnoExtns(MarkerGoTermAnnotationExtn mgtAnnoExtn) {
        if (this.mgtAnnoExtns == null)
            this.mgtAnnoExtns = new HashSet<>();
        this.mgtAnnoExtns.add(mgtAnnoExtn);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MarkerGoTermAnnotationExtnGroup that = (MarkerGoTermAnnotationExtnGroup) o;


        return true;
    }


}
