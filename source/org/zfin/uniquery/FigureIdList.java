package org.zfin.uniquery;

import org.springframework.stereotype.Service;
import org.zfin.expression.Figure;
import org.zfin.feature.Feature;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.repository.RepositoryFactory;

import java.util.List;

/**
 * ToDo: ADD DOCUMENTATION!
 */
@Service
public class FigureIdList extends AbstractEntityIdList {

    public List<String> getUrlList(int numberOfRecords) {
        InfrastructureRepository markerRepository = RepositoryFactory.getInfrastructureRepository();
        return convertIdsIntoUrls(markerRepository.getAllEntities(Figure.class, "zdbID", numberOfRecords));
    }
}
