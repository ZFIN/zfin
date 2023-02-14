package org.zfin.alliancegenome;

import lombok.extern.log4j.Log4j2;
import org.alliancegenome.curation_api.exceptions.ApiErrorException;
import org.alliancegenome.curation_api.exceptions.ObjectUpdateException;
import org.alliancegenome.curation_api.model.entities.AGMDiseaseAnnotation;
import org.alliancegenome.curation_api.model.ingest.dto.AGMDiseaseAnnotationDTO;
import org.alliancegenome.curation_api.response.ObjectResponse;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Service;
import org.zfin.expression.Experiment;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateUtil;
import org.zfin.mutant.DiseaseAnnotation;
import org.zfin.mutant.DiseaseAnnotationModel;
import org.zfin.mutant.Fish;
import org.zfin.mutant.FishExperiment;
import org.zfin.mutant.service.DiseaseAnnotationService;
import org.zfin.ontology.GenericTerm;
import org.zfin.properties.ZfinProperties;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.publication.Publication;

import java.io.IOException;

@Log4j2
@Service
public class DiseaseAnnotationRESTAllianceService extends RestAllianceService {

    private final DiseaseAnnotationRESTInterfaceAlliance api = AllianceRestManager.getDiseaseAnnotationEndpoints();

    public AGMDiseaseAnnotation updateZfinAgmDiseaseAnnotations(AGMDiseaseAnnotationDTO dto) throws IOException, ApiException {
        return api.createZfinAgmDiseaseAnnotations(token, dto);
    }

    public ObjectResponse<AGMDiseaseAnnotation> addDiseaseAnnotation(AGMDiseaseAnnotation annotation) {
        ObjectResponse<AGMDiseaseAnnotation> response = null;
        try {
            response = api.addDiseaseAnnotation(token, annotation);
        } catch (Exception e) {
            String message = e.getMessage() != null ? e.getMessage() : e.getCause().getLocalizedMessage();
            log.error("Could not create Disease Annotation at Alliance: " + message);
        }
        return response;
    }

    public static void main(String[] args) {
        ZfinPropertiesEnum.TARGETROOT.setValue(".");
        ZfinProperties.init();
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        if (sessionFactory == null) {
            new HibernateSessionCreator();
        }

        //ZfinPropertiesEnum.ALLIANCE_CURATION_URL.setValue("http://localhost:8080");
        ZfinPropertiesEnum.ALLIANCE_CURATION_URL.setValue("https://alpha-curation.alliancegenome.org");
        DiseaseAnnotationService service = new DiseaseAnnotationService();
        //service.agmRESTAllianceService = new AgmRESTAllianceService();
        DiseaseAnnotationModel dam = new DiseaseAnnotationModel();
        FishExperiment experiment = new FishExperiment();
        Fish fish = new Fish();
        //fish.setZdbID("ZDB-FISH-220707-340");
        fish.setZdbID("ZDB-FISH-151211-15");
        experiment.setFish(fish);
        Experiment experimentCond = new Experiment();
        experimentCond.setZdbID("ZDB-EXP-041102-1");
        experimentCond.setName("_Standard");
        experiment.setExperiment(experimentCond);
        dam.setFishExperiment(experiment);
        DiseaseAnnotation diseaseAnnotation = new DiseaseAnnotation();
        diseaseAnnotation.setZdbID("ZDB-DAT-220710-12");
        GenericTerm evidence = new GenericTerm();
        evidence.setOboID("ECO:0000304");
        diseaseAnnotation.setEvidenceCode(evidence);
        GenericTerm disease = new GenericTerm();
        disease.setOboID("DOID:4");
        diseaseAnnotation.setDisease(disease);
        Publication publication = new Publication();
        publication.setAccessionNumber(26186000);
        diseaseAnnotation.setPublication(publication);
        dam.setDiseaseAnnotation(diseaseAnnotation);
        service.submitAnnotationToAlliance(dam);
    }

}
