package org.zfin.marker.presentation;

import org.zfin.marker.Marker;
import org.zfin.framework.presentation.AnatomyFact;

/**
 * Class that maps to a statistics table for probes.
 */
public class HighQualityProbeAOStatistics extends AnatomyFact {

    Marker probe;

    public Marker getProbe() {
        return probe;
    }

    public void setProbe(Marker probe) {
        this.probe = probe;
    }
}