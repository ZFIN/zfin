package org.zfin.uniquery;

import org.springframework.stereotype.Service;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.people.Lab;
import org.zfin.repository.RepositoryFactory;

import java.util.List;

/**
 * Create all labs detail page urls.
 */
@Service
public class LabIdList extends AbstractEntityIdList {

    public List<String> getUrlList(int numberOfRecords) {
        InfrastructureRepository infrastructureRepository = RepositoryFactory.getInfrastructureRepository();
        return convertIdsIntoUrls(infrastructureRepository.getAllEntities(Lab.class, "zdbID", numberOfRecords));
    }
}
