package org.zfin.feature.service;

import org.springframework.stereotype.Service;
import org.zfin.feature.Feature;
import org.zfin.feature.presentation.MutationDetailsPresentation;

@Service
public class MutationDetailConversionService {

    public MutationDetailsPresentation convert(Feature feature) {
        MutationDetailsPresentation details = new MutationDetailsPresentation();
        details.setMutationType(feature.getType().getDisplay());
        return details;
    }

}
