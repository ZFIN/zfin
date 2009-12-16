package org.zfin.orthology.repository;

import org.zfin.criteria.ZfinCriteria;
import org.zfin.framework.CachedRepository;
import org.zfin.infrastructure.Updates;
import org.zfin.orthology.Orthologue;
import org.zfin.orthology.SpeciesCriteria;
import org.zfin.publication.Publication;

import java.util.List;
import java.util.Set;

/**
 * User: giles
 * Date: Aug 9, 2006
 * Time: 1:43:30 PM
 */

public interface OrthologyRepository extends CachedRepository {

    public Object[] getOrthologies(List<SpeciesCriteria> speciesCriteria, ZfinCriteria criteria);

    public void saveOrthology(Orthologue orthologue, Publication publication, Updates up);

    /**
     * Update a fast-search table to hold info about evidence codes. In order
     * to display them correctly evidence codes have to be stored in a particular way
     *
     * @param orthologue
     */
    public void updateFastSearchEvidenceCodes(Set<Orthologue> orthologue);
}
