package org.zfin.alliancegenome;

import lombok.extern.log4j.Log4j2;
import org.alliancegenome.curation_api.model.entities.AffectedGenomicModel;
import org.alliancegenome.curation_api.model.entities.Gene;
import org.alliancegenome.curation_api.response.ObjectResponse;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class GeneRESTAllianceService extends RestAllianceService {

    private GeneRESTInterfaceAlliance api = AllianceRestManager.getGeneEndpoints();

    public ObjectResponse<Gene> addGene(Gene gene) {
        ObjectResponse<Gene> geneResponse = null;
        try {
            geneResponse = api.addAffectedGenomicModel(token, gene);
        } catch (Exception e) {
            String message = e.getMessage() != null ? e.getMessage() : e.getCause().getLocalizedMessage();
            log.error("Could not create Affected Genomic Model (Fish) at Alliance: " + message);
        }
        return geneResponse;
    }

}
