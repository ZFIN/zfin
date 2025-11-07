package org.zfin.publication;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.zfin.curation.presentation.CurationStatusDTO;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.InvalidWebRequestException;
import org.zfin.profile.service.ProfileService;
import org.zfin.publication.repository.PublicationTrackingLocationDAO;
import org.zfin.publication.repository.PublicationTrackingStatusDAO;

import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class PublicationTrackingService {

    private PublicationTrackingStatusDAO statusDAO = new PublicationTrackingStatusDAO();
    private PublicationTrackingLocationDAO locationDAO = new PublicationTrackingLocationDAO();


    private Map<String, PublicationTrackingLocation> locationMap = new LinkedHashMap<>();

    public PublicationTrackingLocation getLocation(String locationIndex) {
        if (locationMap.isEmpty()) {
            PublicationTrackingLocation locationOne = locationDAO.findByStatus(PublicationTrackingLocation.Name.INDEXER_PRIORITY_1);
            PublicationTrackingLocation locationTwo = locationDAO.findByStatus(PublicationTrackingLocation.Name.INDEXER_PRIORITY_2);
            PublicationTrackingLocation locationThree = locationDAO.findByStatus(PublicationTrackingLocation.Name.INDEXER_PRIORITY_3);
            locationMap = new LinkedHashMap<>();
            locationMap.put("1", locationOne);
            locationMap.put("2", locationTwo);
            locationMap.put("3", locationThree);
        }
        return locationMap.get(locationIndex);
    }

    public PublicationTrackingStatus getPublicationStatus(PublicationTrackingStatus.Type type) {
        return statusDAO.findByStatus(type);
    }

}
