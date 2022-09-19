package org.zfin.marker.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.zfin.antibody.repository.AntibodyRepository;
import org.zfin.framework.api.View;
import org.zfin.framework.presentation.EntityStatistics;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.marker.Marker;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Term;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;

/**
 * Container to hold Gene and EST info.
 */
public class HighQualityProbe extends EntityStatistics {

    @JsonView(View.API.class)
    private Marker probe;
    @JsonView(View.API.class)
    @JsonProperty("term")
    private Term aoTerm;

    public HighQualityProbe(Marker probe, Term aoTerm) {
        this.probe = probe;
        this.aoTerm = aoTerm;
    }

    public HighQualityProbe(Marker probe, Marker marker, Term aoTerm) {
        this.probe = probe;
        this.getGenes().add(marker);
        this.aoTerm = aoTerm;
    }

    public Marker getProbe() {
        return probe;
    }

    protected PaginationResult<Publication> getPublicationPaginationResult() {
        AntibodyRepository abRepository = RepositoryFactory.getAntibodyRepository();
        return abRepository.getPublicationsProbeWithFigures(probe, (GenericTerm) aoTerm);
    }

    public Term getAnatomyTerm() {
        return aoTerm;
    }
}
