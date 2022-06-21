package org.zfin.alliancegenome;

import lombok.extern.log4j.Log4j2;
import org.alliancegenome.curation_api.model.entities.DiseaseAnnotation;
import org.alliancegenome.curation_api.response.ObjectResponse;

@Log4j2
public class DiseaseAnnotationRESTAllianceService extends RestAllianceService {

    private final DiseaseAnnotationRESTInterfaceAlliance api = AllianceRestManager.getDiseaseAnnotationEndpoints();

    public ObjectResponse<DiseaseAnnotation> addDiseaseAnnotation(DiseaseAnnotation annotation) {
        ObjectResponse<DiseaseAnnotation> response = null;
        try {
            response = api.addDiseaseAnnotation(token, annotation);
        } catch (Exception e) {
            String message = e.getMessage() != null ? e.getMessage() : e.getCause().getLocalizedMessage();
            log.error("Could not create Affected Genomic Model (Fish) at Alliance: " + message);
        }
        return response;
    }

}
