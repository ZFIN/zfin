package org.zfin.uniquery;

import org.springframework.stereotype.Service;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.people.Person;
import org.zfin.repository.RepositoryFactory;

import java.util.List;

/**
 * Create all labs detail page urls.
 */
@Service
public class PersonIdList extends AbstractEntityIdList {

    public List<String> getUrlList(int numberOfRecords) {
        InfrastructureRepository markerRepository = RepositoryFactory.getInfrastructureRepository();
        return convertIdsIntoUrls(markerRepository.getAllEntities(Person.class, "zdbID", numberOfRecords));
    }
}
