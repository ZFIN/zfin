package org.zfin.uniquery;

import org.springframework.stereotype.Service;
import org.zfin.infrastructure.ActiveData;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.repository.RepositoryFactory;

import java.util.ArrayList;
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

    public List<String> convertIdsIntoUrls(List<String> ids) {
        List<String> urlList = new ArrayList<String>(ids.size());
        for (String id : ids) {
            String typeName = ActiveData.validateID(id).name();
            String individualUrl = getIndividualViewUrl(id, typeName);
            if (individualUrl != null)
                urlList.add(individualUrl);
        }
        return urlList;
    }


}
