package org.zfin.feature.repository;

import org.zfin.infrastructure.DataAlias;
import org.zfin.mutant.Feature;


public interface FeatureRepository {

    public Feature getFeatureByID(String zdbID);

    DataAlias getSpecificDataAlias(Feature feature, String alias);

}
