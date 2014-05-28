package org.zfin.orthology.repository;

import org.springframework.stereotype.Repository;
import org.zfin.criteria.ZfinCriteria;
import org.zfin.framework.CachedRepository;
import org.zfin.infrastructure.Updates;
import org.zfin.marker.Marker;
import org.zfin.orthology.Orthologue;
import org.zfin.orthology.SpeciesCriteria;
import org.zfin.orthology.presentation.OrthologySlimPresentation;
import org.zfin.publication.Publication;

import java.util.List;
import java.util.Set;

public interface OrthologyRepository extends CachedRepository {

    Object[] getOrthologies(List<SpeciesCriteria> speciesCriteria, ZfinCriteria criteria);

    void saveOrthology(Orthologue orthologue, Publication publication, Updates up);

    /**
     * Update a fast-search table to hold info about evidence codes. In order
     * to display them correctly evidence codes have to be stored in a particular way
     *
     * @param orthologue
     */
    void updateFastSearchEvidenceCodes(Set<Orthologue> orthologue);

    List<OrthologyPresentationRow> getOrthologyForGene(Marker m) ;

    List<String> getEvidenceCodes(Marker gene);

    List<OrthologySlimPresentation> getOrthologySlimForGeneId(String geneId);
}
