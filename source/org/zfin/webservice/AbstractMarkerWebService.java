package org.zfin.webservice;

import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.repository.RepositoryFactory;

/**
 */
public class AbstractMarkerWebService {

    protected MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();

    protected Marker getGeneForValue(String zdbID) {
        Marker returnGene;
        if (zdbID.startsWith("ZDB-GENE")) {
            returnGene = markerRepository.getGeneByID(zdbID);
        } else {
            returnGene = markerRepository.getGeneByAbbreviation(zdbID);
        }
        return returnGene;
    }

}
