package org.zfin.mutant.service;

import lombok.extern.log4j.Log4j2;
import org.alliancegenome.curation_api.model.entities.AGMDiseaseAnnotation;
import org.alliancegenome.curation_api.model.entities.AffectedGenomicModel;
import org.alliancegenome.curation_api.model.entities.Reference;
import org.alliancegenome.curation_api.model.entities.ontology.DOTerm;
import org.alliancegenome.curation_api.model.entities.ontology.ECOTerm;
import org.alliancegenome.curation_api.model.ingest.dto.AGMDiseaseAnnotationDTO;
import org.alliancegenome.curation_api.model.ingest.dto.DataProviderDTO;
import org.alliancegenome.curation_api.model.ingest.dto.ExperimentalConditionDTO;
import org.springframework.stereotype.Service;
import org.zfin.alliancegenome.ApiException;
import org.zfin.alliancegenome.DiseaseAnnotationRESTAllianceService;
import org.zfin.alliancegenome.ZfinAllianceConverter;
import org.zfin.expression.ExperimentCondition;
import org.zfin.infrastructure.ActiveData;
import org.zfin.marker.agr.DiseaseAnnotationLinkMLInfo;
import org.zfin.marker.agr.RelationshipDTO;
import org.zfin.mutant.DiseaseAnnotationModel;
import org.zfin.mutant.Fish;
import org.zfin.mutant.FishExperiment;
import org.zfin.ontology.GenericTerm;
import org.zfin.publication.Publication;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static org.zfin.repository.RepositoryFactory.getMutantRepository;
import static org.zfin.repository.RepositoryFactory.getOntologyRepository;

@Log4j2
@Service
public class DiseaseAnnotationService extends AllianceService {

    DiseaseAnnotationRESTAllianceService restInterfaceAlliance = new DiseaseAnnotationRESTAllianceService();

    public AGMDiseaseAnnotation submitAnnotationToAlliance(DiseaseAnnotationModel dam) {
        AGMDiseaseAnnotationDTO dto = getAgmDiseaseAnnotationDTO(dam);
        AGMDiseaseAnnotation response = null;
        try {
            response = restInterfaceAlliance.updateZfinAgmDiseaseAnnotations(dto);
        } catch (ApiException e) {
            log.warn("Could not create disease annotation at Alliance " + e.getMessage());
        } catch (IOException e) {
            String message = e.getMessage() != null ? e.getMessage() : e.getCause().getLocalizedMessage();
            log.warn("Could not create Disease Annotation at Alliance: " + message);
        }
        return response;
    }

    private static Reference getCrossReference(org.zfin.mutant.DiseaseAnnotation diseaseAnnotation) {
        Reference reference = new Reference();
        reference.setCurie("PMID:" + diseaseAnnotation.getPublication().getAccessionNumber());
        return reference;
    }

    private static ECOTerm getEvidenceCodes(org.zfin.mutant.DiseaseAnnotation dam) {
        ECOTerm evidence = new ECOTerm();
        if (dam.getEvidenceCode().equals("ZDB-TERM-170419-250"))
            evidence.setCurie("ECO:0000304");
        if (dam.getEvidenceCode().equals("ZDB-TERM-170419-251"))
            evidence.setCurie("ECO:0000305");
        return evidence;
    }

    private static AffectedGenomicModel getBiologicalEntity(Fish fish) {
        AffectedGenomicModel entity = new AffectedGenomicModel();
        entity.setCurie("ZFIN:" + fish.getZdbID());
        entity.setTaxon(getNcbiTaxonTerm());
        return entity;
    }

    private static DOTerm getDoTerm(GenericTerm disease) {
        DOTerm term = new DOTerm();
        term.setCurie(disease.getOboID());
        return term;
    }

    public static AGMDiseaseAnnotationDTO getAgmDiseaseAnnotationDTO(DiseaseAnnotationModel damo) {
        Fish fish = damo.getFishExperiment().getFish();
        AGMDiseaseAnnotationDTO annotation = new AGMDiseaseAnnotationDTO();

        org.alliancegenome.curation_api.model.ingest.dto.DataProviderDTO dataProvider = new DataProviderDTO();
        dataProvider.setSourceOrganizationAbbreviation("ZFIN");
        annotation.setDataProviderDto(dataProvider);
        annotation.setCreatedByCurie("ZFIN:curator");
        annotation.setUpdatedByCurie("ZFIN:curator");
        //annotation.setModifiedBy("ZFIN:curator");
//            annotation.setModEntityId(damo.getDiseaseAnnotation().getZdbID());
        annotation.setDiseaseRelationName(RelationshipDTO.IS_MODEL_OF);
        annotation.setPrimaryExternalId("ZFIN:" + fish.getZdbID());
        annotation.setDateUpdated(format(damo.getDiseaseAnnotation().getZdbID()));

        annotation.setDoTermCurie(damo.getDiseaseAnnotation().getDisease().getOboID());

        List<String> ecoTerms = ZfinAllianceConverter.convertEvidenceCodes(damo.getDiseaseAnnotation().getEvidenceCode().getZdbID()).stream()
                .map(ECOTerm::getCurie).collect(toList());
        annotation.setEvidenceCodeCuries(ecoTerms);
        annotation.setReferenceCurie(getSingleReference(damo.getDiseaseAnnotation().getPublication()));

        org.alliancegenome.curation_api.model.ingest.dto.ConditionRelationDTO condition = populateExperimentConditions(damo.getFishExperiment());
//        org.alliancegenome.curation_api.model.ingest.dto.ConditionRelationDTO condition =new ConditionRelationDTO();
        condition.setHandle(damo.getFishExperiment().getExperiment().getName().replace("_", ""));
        condition.setReferenceCurie(getSingleReference(damo.getDiseaseAnnotation().getPublication()));
        annotation.setConditionRelationDtos(List.of(condition));
        return annotation;
    }

    public static String format(String zdbID) {
        GregorianCalendar date = ActiveData.getDateFromId(zdbID);
        return format(date);
    }

    public static String format(GregorianCalendar calendar) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        fmt.setCalendar(calendar);
        return fmt.format(calendar.getTime());
    }

    private static String getSingleReference(Publication publication) {
        return "PMID:" + publication.getAccessionNumber();
    }

    public static org.alliancegenome.curation_api.model.ingest.dto.ConditionRelationDTO populateExperimentConditions(FishExperiment fishExperiment) {
        org.alliancegenome.curation_api.model.ingest.dto.ConditionRelationDTO relation = new org.alliancegenome.curation_api.model.ingest.dto.ConditionRelationDTO();
        if (fishExperiment.getExperiment() != null) {
            List<ExperimentCondition> allConditions = getMutantRepository().getExperimentConditions(fishExperiment.getExperiment());
            relation.setConditionRelationTypeName("has_condition");
            List<ExperimentalConditionDTO> expconds = new ArrayList<>();
            for (ExperimentCondition condition : allConditions) {
                ExperimentalConditionDTO expcond = new ExperimentalConditionDTO();
                String conditionStatement = condition.getZecoTerm().getTermName();
                if (condition.getAoTerm() != null) {
                    conditionStatement = conditionStatement + " " + condition.getAoTerm().getTermName();
                    expcond.setConditionAnatomyCurie(condition.getAoTerm().getOboID());
                }
                if (condition.getChebiTerm() != null) {
                    expcond.setConditionChemicalCurie(condition.getChebiTerm().getOboID());
                    conditionStatement = conditionStatement + " " + condition.getChebiTerm().getTermName();
                }
                if (condition.getGoCCTerm() != null) {
                    expcond.setConditionGeneOntologyCurie(condition.getGoCCTerm().getOboID());
                    conditionStatement = conditionStatement + " " + condition.getGoCCTerm().getTermName();
                }
                if (condition.getTaxaonymTerm() != null) {
                    expcond.setConditionTaxonCurie(condition.getTaxaonymTerm().getOboID());
                    conditionStatement = conditionStatement + " " + condition.getTaxaonymTerm().getTermName();
                }
                populateConditionClass(expcond, condition);
/*
                String highLevelTermName =
                expcond.setConditionStatement(expcond.getConditionClass()+": "+conditionStatement);
*/
                expconds.add(expcond);
            }
            relation.setConditionDtos(expconds);

        }
        return relation;
    }

    private static void populateConditionClass(ExperimentalConditionDTO expcond, ExperimentCondition condition) {
        String oboID = condition.getZecoTerm().getOboID();
        if (DiseaseAnnotationLinkMLInfo.highLevelConditionTerms.stream().map(GenericTerm::getOboID).toList().contains(oboID)) {
            expcond.setConditionClassCurie(oboID);
        } else {
            Optional<GenericTerm> highLevelterm = DiseaseAnnotationLinkMLInfo.highLevelConditionTerms.stream().filter(parentTerm -> getOntologyRepository().isParentChildRelationshipExist(parentTerm, condition.getZecoTerm()))
                    .findFirst();
            if (highLevelterm.isPresent()) {
                expcond.setConditionClassCurie(highLevelterm.get().getOboID());
                expcond.setConditionIdCurie(oboID);
            }
        }
    }


}
