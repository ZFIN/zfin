package org.zfin.uniquery;

import org.springframework.stereotype.Service;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.mutant.Genotype;
import org.zfin.repository.RepositoryFactory;

import java.util.List;

/**
 * Create all genotypes detail page urls.
 */
@Service
public class GenotypeIdList extends AbstractEntityIdList {

    public List<String> getUrlList(int numberOfRecords) {
        InfrastructureRepository markerRepository = RepositoryFactory.getInfrastructureRepository();
        return convertIdsIntoUrls(markerRepository.getAllEntities(Genotype.class, "zdbID", numberOfRecords));
    }
}
