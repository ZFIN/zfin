package org.zfin.feature.repository;

import org.zfin.infrastructure.DataAlias;
import org.zfin.mutant.Feature;

import java.util.List;


public interface FeatureRepository {

    public Feature getFeatureByID(String zdbID);

    DataAlias getSpecificDataAlias(Feature feature, String alias);

    /**
     * Retrieve a list of all feature for a given publication.
     * Features need to be directly attributed to the publication in question.
     * @param publicationID publication
     * @return list of features
     */
    List<Feature> getFeaturesByPublication(String publicationID);
}
