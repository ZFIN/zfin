package org.zfin.alliancegenome;

import lombok.extern.log4j.Log4j2;
import org.alliancegenome.curation_api.model.entities.AffectedGenomicModel;
import org.alliancegenome.curation_api.response.ObjectResponse;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class AgmRESTAllianceService extends RestAllianceService {

    private AgmRESTInterfaceAlliance api = AllianceRestManager.getAgmEndpoints();

    public ObjectResponse<AffectedGenomicModel> addAffectedGenomicModel(AffectedGenomicModel model) {
        ObjectResponse<AffectedGenomicModel> agmFish = null;
        try {
            agmFish = api.addAffectedGenomicModel(token, model);
        } catch (Exception e) {
            String message = e.getMessage() != null ? e.getMessage() : e.getCause().getLocalizedMessage();
            log.error("Could not create Affected Genomic Model (Fish) at Alliance: " + message);
        }
        return agmFish;
    }

}
