package org.zfin.infrastructure.service;

import org.apache.commons.lang3.StringUtils;
import org.zfin.infrastructure.UpdatesDTO;
import org.zfin.publication.Publication;
import org.zfin.publication.PublicationTrackingHistory;
import org.springframework.stereotype.Service;


import java.util.Comparator;
import java.util.List;

import static org.zfin.repository.RepositoryFactory.getInfrastructureRepository;
import static org.zfin.repository.RepositoryFactory.getPublicationRepository;

@Service
public class UpdatesService {
    public List<UpdatesDTO> getUpdatesDTOS(String zdbID, String filterFieldName) {
        List<UpdatesDTO> updatesDTO = UpdatesDTO.fromUpdates(getInfrastructureRepository().getUpdates(zdbID));

        //is this a publication? If so, add pub tracking events
        Publication publication = getPublicationRepository().getPublication(zdbID);
        if (publication != null) {
            List<PublicationTrackingHistory> events = getPublicationRepository().fullTrackingHistory(publication);
            List<UpdatesDTO> publicationUpdates = UpdatesDTO.fromPublicationEvents(events);
            updatesDTO.addAll(publicationUpdates);
        }
        if (StringUtils.isNotEmpty(filterFieldName)) {
            updatesDTO.removeIf(updatesDTO1 -> !updatesDTO1.fieldName().toLowerCase().contains(filterFieldName.toLowerCase()));
        }

        updatesDTO.sort(Comparator.comparing(UpdatesDTO::whenUpdated, Comparator.nullsLast(Comparator.reverseOrder())));
        return updatesDTO;
    }
}
