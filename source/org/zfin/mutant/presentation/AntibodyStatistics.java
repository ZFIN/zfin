package org.zfin.mutant.presentation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.zfin.antibody.Antibody;
import org.zfin.antibody.repository.AntibodyRepository;
import org.zfin.framework.api.View;
import org.zfin.framework.presentation.EntityStatistics;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.marker.Marker;
import org.zfin.ontology.GenericTerm;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;

import java.util.List;

/**
 * This class is a statistics class about Morpholinos on the Anatomy Detail page.
 */
@Getter
@Setter
public class AntibodyStatistics extends EntityStatistics {

    @JsonView(View.API.class)
    private Antibody antibody;
    @JsonView(View.API.class)
    @JsonProperty("term")
    private GenericTerm anatomyItem;

    @JsonView(View.API.class)
    private List<Marker> antigenGeneList;

    public AntibodyStatistics(Antibody antibody, GenericTerm anatomyItem) {
        this.anatomyItem = anatomyItem;
        this.antibody = antibody;
    }

    public PaginationResult<Publication> getPublicationPaginationResult() {
        AntibodyRepository abRepository = RepositoryFactory.getAntibodyRepository();
        return abRepository.getPublicationsWithFigures(antibody, anatomyItem);
    }

}