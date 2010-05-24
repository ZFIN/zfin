package org.zfin.mutant.presentation;

import org.zfin.antibody.Antibody;
import org.zfin.antibody.repository.AntibodyRepository;
import org.zfin.framework.presentation.EntityStatistics;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.ontology.Term;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;

/**
 * This class is a statistics class about Morpholinos on the Anatomy Detail page.
 */
public class AntibodyStatistics extends EntityStatistics {

    private Antibody antibody;
    private Term anatomyItem;

    public AntibodyStatistics(Antibody antibody, Term anatomyItem) {
        this.anatomyItem = anatomyItem;
        this.antibody = antibody;
    }

    public Antibody getAntibody() {
        return antibody;
    }

    public PaginationResult<Publication> getPublicationPaginationResult() {
        AntibodyRepository abRepository = RepositoryFactory.getAntibodyRepository();
        return abRepository.getPublicationsWithFigures(antibody, anatomyItem);
    }

    public Term getAnatomyItem() {
        return anatomyItem;
    }

}