package org.zfin.uniquery;

import org.springframework.stereotype.Service;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.repository.SequenceRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Create all marker go term evidence detail page urls.
 */
@Service
public class SequenceIdList extends AbstractEntityIdList {

    public List<String> getUrlList(int numberOfRecords) {
        SequenceRepository sequenceRepository = RepositoryFactory.getSequenceRepository();
        return convertIdsIntoUrls(sequenceRepository.getAllNSequences(numberOfRecords));
    }

    public List<String> convertIdsIntoUrls(List<String> allMarkerGos) {
        List<String> urlList = new ArrayList<String>(allMarkerGos.size());
        for (String id : allMarkerGos) {
            String individualUrl = getIndividualViewUrl(id, "SEQUENCE");
            if (individualUrl != null)
                urlList.add(individualUrl);
        }
        return urlList;
    }

}
