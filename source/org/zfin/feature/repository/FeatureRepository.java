package org.zfin.feature.repository;
import org.zfin.mutant.Feature;
import org.zfin.infrastructure.DataAlias;


public interface FeatureRepository {

    public Feature getFeatureByID(String zdbID);
    DataAlias getSpecificDataAlias(Feature feature, String alias);
    
}
