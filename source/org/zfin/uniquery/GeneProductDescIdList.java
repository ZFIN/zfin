package org.zfin.uniquery;

import org.springframework.stereotype.Service;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.repository.RepositoryFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Create all marker go term evidence detail page urls.
 */
@Service
public class GeneProductDescIdList extends AbstractEntityIdList {

    public List<String> getUrlList(int numberOfRecords) {
        MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();
        return convertIdsIntoUrls(markerRepository.getNMarkersWithUniProtNote(numberOfRecords));
    }

    public List<String> convertIdsIntoUrls(List<String> allMarkerGos) {
        List<String> urlList = new ArrayList<String>(allMarkerGos.size());
        for (String id : allMarkerGos) {
            String individualUrl = getIndividualUrl(id, "GENE_PRODUCT");
            if (individualUrl != null)
                urlList.add(individualUrl);
        }
        return urlList;
    }

}
