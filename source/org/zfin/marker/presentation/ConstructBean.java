package org.zfin.marker.presentation;

import org.zfin.feature.Feature;
import org.zfin.mutant.presentation.GenotypeFishResult;

import java.util.List;

/**
 */
public class ConstructBean extends MarkerBean{

    private List<Feature> transgenics;
    private List<GenotypeFishResult> fish;

    public List<Feature> getTransgenics() {
        return transgenics;
    }

    public void setTransgenics(List<Feature> transgenics) {
        this.transgenics = transgenics;
    }

    public List<GenotypeFishResult> getFish() {
        return fish;
    }

    public void setFish(List<GenotypeFishResult> fish) {
        this.fish = fish;
    }
}
