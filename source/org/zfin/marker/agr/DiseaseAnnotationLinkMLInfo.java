package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.alliancegenome.curation_api.model.entities.AffectedGenomicModel;
import org.alliancegenome.curation_api.model.entities.ontology.ECOTerm;
import org.alliancegenome.curation_api.model.ingest.dto.AGMDiseaseAnnotationDTO;
import org.alliancegenome.curation_api.model.ingest.dto.DataProviderDTO;
import org.alliancegenome.curation_api.model.ingest.dto.ExperimentalConditionDTO;
import org.apache.commons.collections4.CollectionUtils;
import org.zfin.alliancegenome.ZfinAllianceConverter;
import org.zfin.expression.ExperimentCondition;
import org.zfin.feature.Feature;
import org.zfin.infrastructure.ActiveData;
import org.zfin.marker.Marker;
import org.zfin.mutant.*;
import org.zfin.ontology.GenericTerm;
import org.zfin.publication.Publication;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.zfin.repository.RepositoryFactory.getMutantRepository;
import static org.zfin.repository.RepositoryFactory.getOntologyRepository;

public class DiseaseAnnotationLinkMLInfo extends LinkMLInfo {

    public DiseaseAnnotationLinkMLInfo(int number) {
        super(number);
    }

    public static void main(String[] args) throws IOException {
        int number = 0;
        mainParent(args);
        DiseaseAnnotationLinkMLInfo diseaseInfo = new DiseaseAnnotationLinkMLInfo(number);
        diseaseInfo.init(LINK_ML_VERSION);
        System.exit(0);
    }

    private void init(String linkMlVersion) throws IOException {
        initAll();
        populateHighLevelConditionTerms();
        List<AGMDiseaseAnnotationDTO> allDiseaseDTO = getDiseaseInfo(numfOfRecords);
        BasicDiseaseAnnotationLinkML basicInfo = new BasicDiseaseAnnotationLinkML();
        basicInfo.setDiseaseAgmIngest(allDiseaseDTO);
        basicInfo.setLinkMlVersion(linkMlVersion);
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
        String jsonInString = writer.writeValueAsString(basicInfo);
        try (PrintStream out = new PrintStream(new FileOutputStream("ZFIN_5.1.1_disease_annotation_ml.json"))) {
            out.print(jsonInString);
        }
    }

    public List<AGMDiseaseAnnotationDTO> getDiseaseInfo(int numberOrRecords) {
        List<AGMDiseaseAnnotationDTO> diseaseDTOList = new ArrayList<>();

        List<FishExperiment> fishExperiments = getMutantRepository().getAllFishExperiment();
        Map<Fish, List<FishExperiment>> fishFishExpMap = fishExperiments.stream().collect(groupingBy(FishExperiment::getFish));
        // get all genes from mutant_fast_search table and list their disease info
        List<GeneGenotypeExperiment> geneGenotypeExperiments = getMutantRepository().getGeneDiseaseAnnotationModels(numberOrRecords);

        // loop over each fish
        fishFishExpMap.forEach((fish, fishExperimentSet) -> {
            // loop over each FishExperiment
            fishExperimentSet.forEach((FishExperiment fishExperiment) -> {
                Genotype genotype = fish.getGenotype();

                // group the diseaseAnnotation by disease
                // so publications and evidence codes are grouped together
                Map<GenericTerm, Set<DiseaseAnnotation>> termMap = fishExperiment.getDiseaseAnnotationModels().stream().collect(Collectors.groupingBy(diseaseAnnotationModel -> diseaseAnnotationModel.getDiseaseAnnotation().getDisease(), Collectors.mapping(DiseaseAnnotationModel::getDiseaseAnnotation, Collectors.toSet())));
                // loop over each disease
                termMap.forEach((disease, diseaseAnnotations) -> {
                    // group disease annotations into pubs and their corresponding list of evidence codes
                    Map<Publication, List<String>> evidenceMap = diseaseAnnotations.stream().collect(Collectors.groupingBy(DiseaseAnnotation::getPublication, Collectors.mapping(this::getEvidenceCodeString, toList())));
                    Map<Publication, List<String>> publicationDateMap = diseaseAnnotations.stream().collect(Collectors.groupingBy(DiseaseAnnotation::getPublication, Collectors.mapping(DiseaseAnnotation::getZdbID, toList())));

                    // Hack: get the date stamp from the ZDB-DAT ID.
                    // Use the earliest one we have per pub
                    Map<Publication, String> map = new HashMap<>();
                    publicationDateMap.forEach((publication, ids) -> {
                        ids.sort(Comparator.naturalOrder());
                        map.put(publication, ids.get(0));
                    });
                    // loop over each publication: final loop as each publication should generate an individual record in the file.
                    evidenceMap.forEach((publication, evidenceSet) -> {

                        // Use wildtype fish with STR
                        // treat as purely implicated by a gene
                        org.alliancegenome.curation_api.model.ingest.dto.DataProviderDTO dataProvider = new DataProviderDTO();
                        dataProvider.setSourceOrganizationAbbreviation("ZFIN");
                        org.alliancegenome.curation_api.model.ingest.dto.CrossReferenceDTO crossReferenceDTO = new org.alliancegenome.curation_api.model.ingest.dto.CrossReferenceDTO();
                        crossReferenceDTO.setDisplayName(disease.getOboID());
                        crossReferenceDTO.setPrefix("ZFIN");
                        crossReferenceDTO.setPageArea("disease");
                        crossReferenceDTO.setReferencedCurie(disease.getOboID());
                        dataProvider.setCrossReferenceDto(crossReferenceDTO);

                        AGMDiseaseAnnotationDTO annotation = new AGMDiseaseAnnotationDTO();
                        annotation.setDataProviderDto(dataProvider);

                        annotation.setDiseaseRelationName(RelationshipDTO.IS_MODEL_OF);
                        AffectedGenomicModel model = getAffectedGenomicModel(fish);
                        annotation.setAgmIdentifier(model.getCurie());
                        annotation.setDoTermCurie(disease.getOboID());
                        annotation.setDateUpdated(format(map.get(publication)));
                        annotation.setCreatedByCurie("ZFIN:CURATOR");
                        //annotation.setModifiedBy("ZFIN:CURATOR");
                        List<Marker> affectedGenes = fish.getAffectedGenes();
                        if (genotype.isWildtype()) {
                            if (CollectionUtils.isNotEmpty(affectedGenes) && affectedGenes.size() == 1)
                                annotation.setInferredGeneIdentifier("ZFIN:" + affectedGenes.get(0).getZdbID());
                        } else {
                            if (CollectionUtils.isNotEmpty(affectedGenes) && affectedGenes.size() == 1) {
                                Marker gene = affectedGenes.get(0);
                                genotype.getGenotypeFeatures().forEach(genotypeFeature -> {
                                    Feature feature = genotypeFeature.getFeature();
                                    if (feature.isSingleAlleleOfMarker(gene)) {
                                        annotation.setInferredAlleleIdentifier("ZFIN:" + feature.getZdbID());
                                    }
                                });
                                annotation.setInferredGeneIdentifier("ZFIN:" + gene.getZdbID());
                            }

                        }
                        List<String> evidenceCodes = evidenceSet.stream().map(ZfinAllianceConverter::convertEvidenceCodes).flatMap(Collection::stream).map(ECOTerm::getCurie).collect(toList());
                        annotation.setEvidenceCodeCuries(evidenceCodes);
                        annotation.setEvidenceCurie(getSingleReference(publication));
                        annotation.setModInternalId(getUniqueID(fish, publication, evidenceCodes, fishExperiment.getExperiment().getName(), disease));

                        org.alliancegenome.curation_api.model.ingest.dto.ConditionRelationDTO condition = populateExperimentConditions(fishExperiment);
                        condition.setHandle(fishExperiment.getExperiment().getName().replace("_", ""));
                        condition.setReferenceCurie(getSingleReference(publication));
                        annotation.setConditionRelationDtos(List.of(condition));
                        diseaseDTOList.add(annotation);

                    });
                });

            });
        });
//        // get all genes from mutant_fast_search table and list their disease info
        List<DiseaseAnnotationModel> damos = getMutantRepository().getDiseaseAnnotationModelsNoStd(numfOfRecords);
        for (DiseaseAnnotationModel damo : damos) {
            AGMDiseaseAnnotationDTO annotation = getAgmDiseaseAnnotationDTO(damo);
            diseaseDTOList.add(annotation);
        }

        return diseaseDTOList;
    }

    private String getUniqueID(Fish fish, Publication publication, List<String> evidenceCodes, String experiment, GenericTerm disease) {
        String uniqueID = fish.getZdbID();
        uniqueID +="|";
        uniqueID += disease.getOboID();
        uniqueID +="|";
        uniqueID += publication.getZdbID();
        uniqueID +="|";
        StringJoiner values = new StringJoiner(",");
        evidenceCodes.forEach(values::add);
        uniqueID += values.toString();
        uniqueID +="|";
        uniqueID += experiment;
        return uniqueID;
    }

    private AGMDiseaseAnnotationDTO getAgmDiseaseAnnotationDTO(DiseaseAnnotationModel damo) {
        Fish fish = damo.getFishExperiment().getFish();
        AGMDiseaseAnnotationDTO annotation = new AGMDiseaseAnnotationDTO();

        org.alliancegenome.curation_api.model.ingest.dto.DataProviderDTO dataProvider = new DataProviderDTO();
        dataProvider.setSourceOrganizationAbbreviation("ZFIN");
        annotation.setDataProviderDto(dataProvider);
        annotation.setCreatedByCurie("ZFIN:curator");
        //annotation.setModifiedBy("ZFIN:curator");
//            annotation.setModEntityId(damo.getDiseaseAnnotation().getZdbID());
        annotation.setDiseaseRelationName(RelationshipDTO.IS_MODEL_OF);
        annotation.setAgmIdentifier("ZFIN:" + fish.getZdbID());
        annotation.setDateUpdated(format(damo.getDiseaseAnnotation().getZdbID()));

        annotation.setDoTermCurie(damo.getDiseaseAnnotation().getDisease().getOboID());

        List<String> ecoTerms = ZfinAllianceConverter.convertEvidenceCodes(damo.getDiseaseAnnotation().getEvidenceCode().getZdbID()).stream().map(ECOTerm::getCurie).collect(toList());
        annotation.setEvidenceCodeCuries(ecoTerms);
        annotation.setEvidenceCurie(getSingleReference(damo.getDiseaseAnnotation().getPublication()));
        annotation.setModInternalId(getUniqueID(fish, damo.getDiseaseAnnotation().getPublication(), ecoTerms, damo.getFishExperiment().getExperiment().getName(), damo.getDiseaseAnnotation().getDisease()));

        org.alliancegenome.curation_api.model.ingest.dto.ConditionRelationDTO condition = populateExperimentConditions(damo.getFishExperiment());
        List<org.alliancegenome.curation_api.model.ingest.dto.ConditionRelationDTO> conditions = new ArrayList<>();
        condition.setHandle(damo.getFishExperiment().getExperiment().getName().replace("_", ""));
        condition.setReferenceCurie(getSingleReference(damo.getDiseaseAnnotation().getPublication()));
        conditions.add(condition);
        annotation.setConditionRelationDtos(conditions);
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

    private void populateConditionClass(ExperimentalConditionDTO expcond, ExperimentCondition condition) {
        String oboID = condition.getZecoTerm().getOboID();
        if (highLevelConditionTerms.stream().map(GenericTerm::getOboID).toList().contains(oboID)) {
            expcond.setConditionClassCurie(oboID);
        } else {
            Optional<GenericTerm> highLevelterm = highLevelConditionTerms.stream().filter(parentTerm -> getOntologyRepository().isParentChildRelationshipExist(parentTerm, condition.getZecoTerm())).findFirst();
            if (highLevelterm.isPresent()) {
                expcond.setConditionClassCurie(highLevelterm.get().getOboID());
                expcond.setConditionIdCurie(oboID);
            }
        }
    }

    private AffectedGenomicModel getAffectedGenomicModel(Fish fish) {
        AffectedGenomicModel model = new AffectedGenomicModel();
        model.setCurie("ZFIN:" + fish.getZdbID());
        model.setName(fish.getDisplayName());
        return model;
    }

    public org.alliancegenome.curation_api.model.ingest.dto.ConditionRelationDTO populateExperimentConditions(FishExperiment fishExperiment) {
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

    public EvidenceDTO getEvidenceDTO(Publication publication, List<String> evidences) {
        PublicationAgrDTO fixedPub = new PublicationAgrDTO();
        List<String> pubPages = new ArrayList<>();
        pubPages.add("reference");
        CrossReferenceDTO pubXref = new CrossReferenceDTO("ZFIN", publication.getZdbID(), pubPages);
        if (publication.getAccessionNumber() != null) {
            fixedPub.setPublicationId("PMID:" + publication.getAccessionNumber());
            fixedPub.setCrossReference(pubXref);
        } else {
            fixedPub.setPublicationId("ZFIN:" + publication.getZdbID());
        }

        EvidenceDTO evDto = new EvidenceDTO(fixedPub);
        evDto.setEvidenceCodes(evidences);
        return evDto;
    }

    // hard-coded for now as the ECO ontology does not provide the codes in
    // abbreviated form easily. The term names are very long and only in the synonym list
    // you can find TAS an IC.
    // Needs to be changed in the future.
    private String getEvidenceCodeString(DiseaseAnnotation diseaseAnnotations) {
        return getEvidenceCodeFromString(diseaseAnnotations.getEvidenceCode().getZdbID());
    }

    private String getEvidenceCodeFromString(String ecoValue) {
        if (ecoValue.equals("ZDB-TERM-170419-250")) return "ECO:0000304";
        if (ecoValue.equals("ZDB-TERM-170419-251")) return "ECO:0000305";
        return "";
    }

    // ToDo: This list should be a slim in ZECO to identify those high-level terms.
    public static final List<GenericTerm> highLevelConditionTerms = new ArrayList<>(18);

    static {
        highLevelConditionTerms.add(new GenericTerm("ZDB-TERM-160831-7", "ZECO:0000105"));
        highLevelConditionTerms.add(new GenericTerm("ZDB-TERM-160831-13", "ZECO:0000111"));
        highLevelConditionTerms.add(new GenericTerm("ZDB-TERM-160831-14", "ZECO:0000112"));
        highLevelConditionTerms.add(new GenericTerm("ZDB-TERM-160831-15", "ZECO:0000113"));
        highLevelConditionTerms.add(new GenericTerm("ZDB-TERM-160831-33", "ZECO:0000131"));
        highLevelConditionTerms.add(new GenericTerm("ZDB-TERM-160831-42", "ZECO:0000140"));
        highLevelConditionTerms.add(new GenericTerm("ZDB-TERM-160831-45", "ZECO:0000143"));
        highLevelConditionTerms.add(new GenericTerm("ZDB-TERM-160831-48", "ZECO:0000146"));
        highLevelConditionTerms.add(new GenericTerm("ZDB-TERM-160831-56", "ZECO:0000154"));
        highLevelConditionTerms.add(new GenericTerm("ZDB-TERM-160831-62", "ZECO:0000160"));
        highLevelConditionTerms.add(new GenericTerm("ZDB-TERM-160831-82", "ZECO:0000182"));
        highLevelConditionTerms.add(new GenericTerm("ZDB-TERM-160831-108", "ZECO:0000208"));
        highLevelConditionTerms.add(new GenericTerm("ZDB-TERM-160831-122", "ZECO:0000222"));
        highLevelConditionTerms.add(new GenericTerm("ZDB-TERM-160831-129", "ZECO:0000229"));
        highLevelConditionTerms.add(new GenericTerm("ZDB-TERM-171108-6", "ZECO:0000252"));
        highLevelConditionTerms.add(new GenericTerm("ZDB-TERM-160831-3", "ZECO:0000101"));
        highLevelConditionTerms.add(new GenericTerm("ZDB-TERM-160831-5", "ZECO:0000103"));
        // make sure it's the last entry as it is a root term.
        highLevelConditionTerms.add(new GenericTerm("ZDB-TERM-160831-6", "ZECO:0000104"));
    }

    private static void populateHighLevelConditionTerms() {
        highLevelConditionTerms.forEach(genericTerm -> genericTerm.setTermName(getOntologyRepository().getTermByOboID(genericTerm.getOboID()).getTermName()));
    }

}
//test