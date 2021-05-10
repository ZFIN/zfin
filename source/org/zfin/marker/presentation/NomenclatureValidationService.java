package org.zfin.marker.presentation;

import org.apache.commons.lang3.StringUtils;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.repository.RepositoryFactory;

public class NomenclatureValidationService {

    private static final MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();

    public static String validateMarkerName(String name, boolean isEFG) {
        if (StringUtils.isBlank(name)) {
            return "gene.name.empty";
        }
        if (markerRepository.getMarkerByName(name) != null) {
            return "gene.name.inuse";
        }
        if (isEFG) {
            if (markerRepository.isMarkerExists(name)) {
                return "gene.name.inuse";
            }
        }
        return null;
    }

    public static String validateMarkerName(String name) {
        return validateMarkerName(name, false);
    }

    public static String validateMarkerAbbreviation(String abbreviation) {
        if (StringUtils.isBlank(abbreviation)) {
            return "gene.abbreviation.empty";
        }
        if (markerRepository.isMarkerExists(abbreviation)) {
            return "gene.abbreviation.inuse";
        }
        if (!abbreviation.matches("[a-z0-9_:.-]+")) {
            return "gene.abbreviation.invalidcharacters";
        }
        return null;
    }

}
