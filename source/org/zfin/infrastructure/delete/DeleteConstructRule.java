package org.zfin.infrastructure.delete;

import org.zfin.feature.Feature;
import org.zfin.marker.Marker;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DeleteConstructRule extends AbstractDeleteEntityRule implements DeleteEntityRule {

    public DeleteConstructRule(String zdbID) {
        this.zdbID = zdbID;
    }

    @Override
    public List<DeleteValidationReport> validate() {
        Marker construct = RepositoryFactory.getMarkerRepository().getMarkerByID(zdbID);
        if (construct == null)
            throw new NullPointerException("No Construct found: " + zdbID);

        List<Feature> featureList = RepositoryFactory.getFeatureRepository().getFeaturesByConstruct(construct);
        Collections.sort(featureList, new Comparator<Feature>() {
            @Override
            public int compare(Feature o1, Feature o2) {
                return o1.getAbbreviationOrder().compareTo(o2.getAbbreviationOrder());
            }
        });
        addToValidationReport("Construct has a relationship to the following list of features", featureList);
        entity = construct;
        return validationReportList;

    }

    @Override
    public void prepareDelete() {
        entity = RepositoryFactory.getMarkerRepository().getMarkerByID(zdbID);
    }

    @Override
    public Publication getPublication() {
        return null;
    }
}
