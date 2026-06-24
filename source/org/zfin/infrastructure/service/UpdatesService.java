package org.zfin.infrastructure.service;

import org.apache.commons.lang3.StringUtils;
import org.zfin.infrastructure.ReplacementZdbID;
import org.zfin.infrastructure.UpdatesDTO;
import org.zfin.publication.Publication;
import org.zfin.publication.PublicationTrackingHistory;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.zfin.repository.RepositoryFactory.getInfrastructureRepository;
import static org.zfin.repository.RepositoryFactory.getPublicationRepository;

@Service
public class UpdatesService {
    public List<UpdatesDTO> getUpdatesDTOS(String zdbID, String filterFieldName) {
        // Audit-history rows in the updates table are never repointed when a marker is merged
        // (both the legacy Perl and the Java MarkerMergeService intentionally skip the updates
        // table). Resolve that here at the display layer: include the update history of any old
        // IDs that were merged into the record being viewed, resolved through zdb_replaced_data.
        List<UpdatesDTO> updatesDTO = new ArrayList<>(UpdatesDTO.fromUpdates(getInfrastructureRepository().getUpdates(zdbID)));
        for (String mergedAwayID : getMergedAwayIDs(zdbID)) {
            updatesDTO.addAll(UpdatesDTO.fromUpdates(getInfrastructureRepository().getUpdates(mergedAwayID)));
        }

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

    /**
     * Collect every old zdbID that was merged into the given surviving zdbID, resolved through
     * zdb_replaced_data. Follows replacement chains transitively (A merged into B, B merged into
     * survivor) and is cycle-safe. The surviving id itself is not included in the result.
     */
    private Set<String> getMergedAwayIDs(String survivingZdbID) {
        Set<String> mergedAwayIDs = new LinkedHashSet<>();
        Set<String> frontier = new HashSet<>();
        frontier.add(survivingZdbID);
        while (!frontier.isEmpty()) {
            Set<String> next = new HashSet<>();
            for (String newID : frontier) {
                for (ReplacementZdbID replacement : getInfrastructureRepository().getReplacementsForNewZdbID(newID)) {
                    String oldID = replacement.getOldZdbID();
                    if (oldID != null && !oldID.equals(survivingZdbID) && mergedAwayIDs.add(oldID)) {
                        next.add(oldID);
                    }
                }
            }
            frontier = next;
        }
        return mergedAwayIDs;
    }
}
