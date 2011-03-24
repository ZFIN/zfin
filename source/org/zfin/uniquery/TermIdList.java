package org.zfin.uniquery;

import org.springframework.stereotype.Service;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.repository.OntologyRepository;
import org.zfin.repository.RepositoryFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Create all marker detail page urls.
 */
@Service
public class TermIdList extends AbstractEntityIdList {

    public List<String> getUrlList(int numberOfRecords) {
        OntologyRepository ontologyRepository = RepositoryFactory.getOntologyRepository();
        if (numberOfRecords < 1) {
            return convertIdsIntoUrls(ontologyRepository.getFirstNTermsPerOntology(0));
        }
        return convertIdsIntoUrls(ontologyRepository.getFirstNTermsPerOntology(numberOfRecords));

    }

    public List<String> convertIdsIntoUrls(List<String> allTermIds) {
        List<String> urlList = new ArrayList<String>(allTermIds.size());
        for (String id : allTermIds) {
            String individualUrl = getIndividualUrl(id, "TERM");
            if (individualUrl != null)
                urlList.add(individualUrl);
        }
        return urlList;
    }

}
