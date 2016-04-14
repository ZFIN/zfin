package org.zfin.feature.service;

import org.springframework.stereotype.Service;
import org.zfin.feature.Feature;
import org.zfin.feature.FeatureDnaMutationDetail;
import org.zfin.feature.presentation.MutationDetailsPresentation;

@Service
public class MutationDetailsConversionService {

    public MutationDetailsPresentation convert(Feature feature) {
        MutationDetailsPresentation details = new MutationDetailsPresentation();
        details.setMutationType(getMutationTypeStatement(feature));
        details.setDnaChangeStatement(getDnaMutationStatement(feature));
        return details;
    }

    public String getMutationTypeStatement(Feature feature) {
        return feature.getType().getDisplay();
    }

    public String getDnaMutationStatement(Feature feature) {
        FeatureDnaMutationDetail dnaChange = feature.getFeatureDnaMutationDetail();
        switch (feature.getType()) {
            case POINT_MUTATION:
                return getDnaMutationStatementForPointMutation(dnaChange);
            default:
                return "";
        }
    }

    private String getDnaMutationStatementForPointMutation(FeatureDnaMutationDetail dnaChange) {
        if (dnaChange == null || dnaChange.getDnaMutationTerm() == null) {
            return "";
        }
        return dnaChange.getDnaMutationTerm().getDisplayName();
    }

}
