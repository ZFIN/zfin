package org.zfin.marker.presentation;

import org.zfin.sequence.TranscriptDBLink;
import org.zfin.marker.presentation.RelatedMarker;

import java.util.TreeSet;

public class TranscriptTargets {
    private TranscriptDBLink predictedTarget;
    private TreeSet<RelatedMarker> publishedTargets;


    public TranscriptDBLink getPredictedTarget() {
        return predictedTarget;
    }

    public void setPredictedTarget(TranscriptDBLink predictedTarget) {
        this.predictedTarget = predictedTarget;
    }

    public TreeSet<RelatedMarker> getPublishedTargets() {
        //never return a null collection
        if (publishedTargets == null) { publishedTargets = new TreeSet<RelatedMarker>(); }
        return publishedTargets;
    }

    public void setPublishedTargets(TreeSet<RelatedMarker> publishedTargets) {
        this.publishedTargets = publishedTargets;
    }
}
