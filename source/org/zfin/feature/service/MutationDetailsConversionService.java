package org.zfin.feature.service;

import org.apache.commons.lang3.StringUtils;
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
        StringBuilder statement = new StringBuilder(dnaChange.getDnaMutationTerm().getDisplayName());
        String localization = getGeneLocalizationStatement(dnaChange);
        if (StringUtils.isNotEmpty(localization)) {
            statement.append(" in ").append(localization);
        }
        return statement.toString();
    }

    private String getGeneLocalizationStatement(FeatureDnaMutationDetail dnaChange) {
        if (dnaChange == null) {
            return  "";
        }
        if (dnaChange.getExonNumber() != null) {
            return "exon " + dnaChange.getExonNumber();
        }
        if (dnaChange.getIntronNumber() != null) {
            return "interon " + dnaChange.getIntronNumber();
        }
        return "";
    }

}
