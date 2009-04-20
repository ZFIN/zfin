package org.zfin.marker.presentation;

import org.zfin.expression.Figure;
import org.zfin.marker.Marker;
import org.zfin.publication.Publication;
import org.zfin.framework.presentation.EntityStatistics;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.anatomy.AnatomyItem;

import java.text.ChoiceFormat;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

/**
 * Container to hold Gene and EST info.
 */
public class HighQualityProbe extends EntityStatistics {

    private Marker probe;
    private AnatomyItem aoTerm;

    public HighQualityProbe(Marker probe, AnatomyItem aoTerm) {
        this.probe = probe;
        this.aoTerm = aoTerm;
    }

    public Marker getProbe() {
        return probe;
    }
    protected PaginationResult<Publication> getPublicationPaginationResult() {
        return null;
    }

    public AnatomyItem getAnatomyTerm() {
        return aoTerm;
    }
}
