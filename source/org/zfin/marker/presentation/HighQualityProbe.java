package org.zfin.marker.presentation;

import org.zfin.framework.presentation.EntityStatistics;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.marker.Marker;
import org.zfin.ontology.Term;
import org.zfin.publication.Publication;

/**
 * Container to hold Gene and EST info.
 */
public class HighQualityProbe extends EntityStatistics {

    private Marker probe;
    private Term aoTerm;

    public HighQualityProbe(Marker probe, Term aoTerm) {
        this.probe = probe;
        this.aoTerm = aoTerm;
    }

    public Marker getProbe() {
        return probe;
    }

    protected PaginationResult<Publication> getPublicationPaginationResult() {
        return null;
    }

    public Term getAnatomyTerm() {
        return aoTerm;
    }
}
