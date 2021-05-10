package org.zfin.marker.presentation;

import org.zfin.framework.presentation.AnatomyFact;
import org.zfin.marker.Marker;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * Class that maps to a statistics table for probes.
 */
@Entity
@DiscriminatorValue("High-Quality-Probe")
public class HighQualityProbeAOStatistics extends AnatomyFact {

    @ManyToOne()
    @JoinColumn(name = "fstat_feat_zdb_id")
    Marker probe;

    public Marker getProbe() {
        return probe;
    }

    public void setProbe(Marker probe) {
        this.probe = probe;
    }
}