package org.zfin.fish.presentation;

import org.zfin.expression.presentation.ExperimentPresentation;
import org.zfin.framework.presentation.EntityPresentation;
import org.zfin.mutant.DiseaseModel;
import org.zfin.ontology.presentation.TermPresentation;
import org.zfin.publication.presentation.PublicationPresentation;

/**
 * Presentation for a disease model entity
 */
public class DiseaseModelPresentation extends EntityPresentation {

    private static final String uri = "/";

    /**
     * Generates a OrthologySpecies link using the name.
     *
     * @param model disease model
     * @return html for disease model
     */
    public static String getLink(DiseaseModel model) {
        String linkDisplayText = TermPresentation.getLink(model.getDisease(), false);
        linkDisplayText += ", " + PublicationPresentation.getLink(model.getPublication());
        linkDisplayText += ", " + model.getEvidenceCode();
        linkDisplayText += ", " + ExperimentPresentation.getLink(model.getFishExperiment().getExperiment(), false);
        return linkDisplayText;
    }

}
