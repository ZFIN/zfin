package org.zfin.marker.presentation;

import org.zfin.feature.Feature;

import java.util.List;

/**
 */
public class ConstructBean extends MarkerBean{

    private List<Feature> transgenics;

    public List<Feature> getTransgenics() {
        return transgenics;
    }

    public void setTransgenics(List<Feature> transgenics) {
        this.transgenics = transgenics;
    }
}
