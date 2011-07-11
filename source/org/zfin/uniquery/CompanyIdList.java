package org.zfin.uniquery;

import org.springframework.stereotype.Service;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.people.Company;
import org.zfin.repository.RepositoryFactory;

import java.util.List;

/**
 * Create all companies detail page urls.
 */
@Service
public class CompanyIdList extends AbstractEntityIdList {

    public List<String> getUrlList(int numberOfRecords) {
        InfrastructureRepository markerRepository = RepositoryFactory.getInfrastructureRepository();
        return convertIdsIntoUrls(markerRepository.getAllEntities(Company.class, "zdbID", numberOfRecords));
    }
}
