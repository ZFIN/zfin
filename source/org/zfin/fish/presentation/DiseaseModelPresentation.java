package org.zfin.fish.presentation;

import org.zfin.framework.presentation.EntityPresentation;
import org.zfin.mutant.DiseaseAnnotation;
import org.zfin.ontology.presentation.TermPresentation;
import org.zfin.publication.presentation.PublicationPresentation;

/**
 * Presentation for a disease model entity
 */
public class DiseaseModelPresentation extends EntityPresentation {

    private static final String uri = "/";

    /**
     * Generates a disease model link using the name.
     *
     * @param model disease model
     * @return html for disease model
     */
    public static String getLink(DiseaseAnnotation model) {
        String linkDisplayText = TermPresentation.getLink(model.getDisease(), false);
        linkDisplayText += ", " + PublicationPresentation.getLink(model.getPublication());
        linkDisplayText += ", " + model.getEvidenceCode();
        return linkDisplayText;
    }

}
