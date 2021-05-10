package org.zfin.marker.presentation;

import org.zfin.sequence.TranscriptDBLink;

import java.util.Set;
import java.util.TreeSet;

public class TranscriptTargets {
    private TranscriptDBLink predictedTarget;
    private Set<RelatedMarker> publishedTargets;


    public TranscriptDBLink getPredictedTarget() {
        return predictedTarget;
    }

    public void setPredictedTarget(TranscriptDBLink predictedTarget) {
        this.predictedTarget = predictedTarget;
    }

    public Set<RelatedMarker> getPublishedTargets() {
        //never return a null collection
        if (publishedTargets == null) {
            publishedTargets = new TreeSet<RelatedMarker>();
        }
        return publishedTargets;
    }

    public void setPublishedTargets(Set<RelatedMarker> publishedTargets) {
        this.publishedTargets = publishedTargets;
    }
}
