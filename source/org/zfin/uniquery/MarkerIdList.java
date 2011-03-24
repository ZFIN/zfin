package org.zfin.uniquery;

import org.springframework.stereotype.Service;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.repository.RepositoryFactory;

import java.util.List;

/**
 * Create all marker detail page urls.
 */
@Service
public class MarkerIdList extends AbstractEntityIdList {

    public List<String> getUrlList(int numberOfRecords) {
        MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();
        return convertIdsIntoUrls(markerRepository.getNMarkersPerType(numberOfRecords));
    }
}
