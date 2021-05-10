package org.zfin.uniquery;

import org.springframework.stereotype.Service;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.mutant.MarkerGoTermEvidence;
import org.zfin.repository.RepositoryFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Create all marker go term evidence detail page urls.
 */
@Service
public class MarkerGoTermEvidenceIdList extends AbstractEntityIdList {

    public List<String> getUrlList(int numberOfRecords) {
        InfrastructureRepository infrastructureRepository = RepositoryFactory.getInfrastructureRepository();
        return convertIdsIntoUrls(infrastructureRepository.getAllEntities(MarkerGoTermEvidence.class, "marker.zdbID", numberOfRecords));
    }

    public List<String> convertIdsIntoUrls(List<String> allMarkerGos) {
        List<String> urlList = new ArrayList<String>(allMarkerGos.size());
        for (String id : allMarkerGos) {
            String individualUrl = getIndividualUrl(id, "MARKERGO");
            if (individualUrl != null)
                urlList.add(individualUrl);
        }
        return urlList;
    }

}
