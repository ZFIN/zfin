package org.zfin.marker.presentation;

import org.zfin.sequence.TranscriptDBLink;
import org.zfin.marker.presentation.RelatedMarker;

import java.util.TreeSet;
import java.util.Set;

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
        if (publishedTargets == null) { publishedTargets = new TreeSet<RelatedMarker>(); }
        return publishedTargets;
    }

    public void setPublishedTargets(Set<RelatedMarker> publishedTargets) {
        this.publishedTargets = publishedTargets;
    }
}
