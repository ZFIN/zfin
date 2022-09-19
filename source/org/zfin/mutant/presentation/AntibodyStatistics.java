package org.zfin.mutant.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.zfin.antibody.Antibody;
import org.zfin.antibody.repository.AntibodyRepository;
import org.zfin.framework.api.View;
import org.zfin.framework.presentation.EntityStatistics;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.ontology.GenericTerm;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;

/**
 * This class is a statistics class about Morpholinos on the Anatomy Detail page.
 */
@Getter
public class AntibodyStatistics extends EntityStatistics {

    @JsonView(View.API.class)
    private Antibody antibody;
    @JsonView(View.API.class)
    @JsonProperty("term")
    private GenericTerm anatomyItem;

    public AntibodyStatistics(Antibody antibody, GenericTerm anatomyItem) {
        this.anatomyItem = anatomyItem;
        this.antibody = antibody;
    }

    public PaginationResult<Publication> getPublicationPaginationResult() {
        AntibodyRepository abRepository = RepositoryFactory.getAntibodyRepository();
        return abRepository.getPublicationsWithFigures(antibody, anatomyItem);
    }

}