package org.zfin.marker.agr;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.alliancegenome.curation_api.model.entities.AffectedGenomicModel;
import org.alliancegenome.curation_api.model.entities.ontology.EcoTerm;
import org.alliancegenome.curation_api.model.ingest.dto.AGMDiseaseAnnotationDTO;
import org.alliancegenome.curation_api.model.ingest.dto.ExperimentalConditionDTO;
import org.zfin.alliancegenome.ZfinAllianceConverter;
import org.zfin.expression.ExperimentCondition;
import org.zfin.infrastructure.ActiveData;
import org.zfin.marker.Marker;
import org.zfin.mutant.*;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;
import org.zfin.publication.Publication;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static org.zfin.repository.RepositoryFactory.getMutantRepository;
import static org.zfin.repository.RepositoryFactory.getOntologyRepository;

public class DiseaseAnnotationLinkMLInfo extends AbstractScriptWrapper {

    private int numfOfRecords = 0;

    public DiseaseAnnotationLinkMLInfo(int number) {
        numfOfRecords = number;
    }


    public static void main(String[] args) throws IOException {
        int number = 0;
        if (args.length > 0) {
            number = Integer.valueOf(args[0]);
        }
        DiseaseAnnotationLinkMLInfo diseaseInfo = new DiseaseAnnotationLinkMLInfo(number);
        diseaseInfo.init();
        System.exit(0);
    }

    private void init() throws IOException {
        initAll();
        List<AGMDiseaseAnnotationDTO> allDiseaseDTO = getDiseaseInfo(numfOfRecords);
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
        String jsonInString = writer.writeValueAsString(allDiseaseDTO);
        try (PrintStream out = new PrintStream(new FileOutputStream("ZFIN_1.0.1.4_disease_annotation_ml.json"))) {
            out.print(jsonInString);
        }
    }

    public List<AGMDiseaseAnnotationDTO> getDiseaseInfo(int numberOrRecords) {
        List<AGMDiseaseAnnotationDTO> diseaseDTOList = new ArrayList<>();


        // get all genes from mutant_fast_search table and list their disease info
        List<GeneGenotypeExperiment> geneGenotypeExperiments = getMutantRepository().getGeneDiseaseAnnotationModels(numberOrRecords);

        // group by gene records
        Map<Marker, Set<FishExperiment>> diseaseModelMap =
                geneGenotypeExperiments.stream().collect(
                        Collectors.groupingBy(GeneGenotypeExperiment::getGene,
                                Collectors.mapping(GeneGenotypeExperiment::getFishExperiment, Collectors.toSet())));

        // loop over each gene
        diseaseModelMap.forEach((gene, fishExperimentSet) -> {
            // loop over each FishExperiment

            fishExperimentSet.forEach((FishExperiment fishExperiment) -> {
                Genotype genotype = fishExperiment.getFish().getGenotype();

                Fish fish = fishExperiment.getFish();
                // group the diseaseAnnotation by disease
                // so publications and evidence codes are grouped together
                Map<GenericTerm, Set<DiseaseAnnotation>> termMap = fishExperiment.getDiseaseAnnotationModels()
                        .stream()
                        .collect(
                                Collectors.groupingBy(diseaseAnnotationModel -> diseaseAnnotationModel.getDiseaseAnnotation().getDisease(),
                                        Collectors.mapping(DiseaseAnnotationModel::getDiseaseAnnotation, Collectors.toSet())
                                )
                        );
                // loop over each disease
                termMap.forEach((disease, diseaseAnnotations) -> {
                    // group disease annotations into pubs and their corresponding list of evidence codes
                    Map<Publication, List<String>> evidenceMap = diseaseAnnotations
                            .stream()
                            .collect(
                                    Collectors.groupingBy(DiseaseAnnotation::getPublication,
                                            Collectors.mapping(this::getEvidenceCodeString, toList())
                                    )
                            );
                    Map<Publication, List<String>> publicationDateMap = diseaseAnnotations
                            .stream()
                            .collect(
                                    Collectors.groupingBy(DiseaseAnnotation::getPublication,
                                            Collectors.mapping(DiseaseAnnotation::getZdbID, toList())
                                    )
                            );

                    // Hack: get the date stamp from the ZDB-DAT ID.
                    // Use the earliest one we have per pub
                    Map<Publication, GregorianCalendar> map = new HashMap<>();
                    publicationDateMap.forEach((publication, ids) -> {
                        ids.sort(Comparator.naturalOrder());
                        GregorianCalendar date = ActiveData.getDateFromId(ids.get(0));
                        map.put(publication, date);
                    });
                    // loop over each publication: final loop as each publication should generate a individual record in the file.
                    evidenceMap.forEach((publication, evidenceSet) -> {
                        // Use wildtype fish with STR
                        // treat as purely implicated by a gene
                        AGMDiseaseAnnotationDTO annotation = new AGMDiseaseAnnotationDTO();

                        annotation.setDiseaseRelation(RelationshipDTO.IS_MODEL_OF);
                        AffectedGenomicModel model = getAffectedGenomicModel(fish);
                        annotation.setSubject(model.getCurie());
                        annotation.setObject(disease.getOboID());
                        annotation.setCreatedBy(format(map.get(publication)));

                        List<String> evidenceCodes = evidenceSet.stream()
                                .map(ZfinAllianceConverter::convertEvidenceCodes)
                                .flatMap(Collection::stream)
                                .map(EcoTerm::getCurie)
                                .collect(toList());
                        annotation.setEvidenceCodes(evidenceCodes);
                        annotation.setSingleReference(getSingleReference(publication));

                        if (genotype.isWildtype()) {
                            // inferred Genes
/*
                            Gene inferredGene = new Gene();
                            inferredGene.setCurie("ZFIN:" + gene.getZdbID());
                            annotation.set(inferredGene);
*/
                            diseaseDTOList.add(annotation);
                        }
                        org.alliancegenome.curation_api.model.ingest.dto.ConditionRelationDTO condition = populateExperimentConditions(fishExperiment);
                        List<org.alliancegenome.curation_api.model.ingest.dto.ConditionRelationDTO> conditions = new ArrayList<>();
                        conditions.add(condition);
                        annotation.setConditionRelations(List.of(condition));
                        diseaseDTOList.add(annotation);

                    });
                });

            });
        });
//
//        // get all genes from mutant_fast_search table and list their disease info
        List<DiseaseAnnotationModel> damos = getMutantRepository().getDiseaseAnnotationModelsNoStd(numfOfRecords);
        for (DiseaseAnnotationModel damo : damos) {

            Fish fish = damo.getFishExperiment().getFish();
            AGMDiseaseAnnotationDTO annotation = new AGMDiseaseAnnotationDTO();
            annotation.setDiseaseRelation(RelationshipDTO.IS_MODEL_OF);
            annotation.setSubject(fish.getZdbID());

            annotation.setObject(damo.getDiseaseAnnotation().getDisease().getOboID());

            List<String> ecoTerms = ZfinAllianceConverter.convertEvidenceCodes(damo.getDiseaseAnnotation().getEvidenceCode()).stream()
                    .map(EcoTerm::getCurie).collect(toList());
            annotation.setEvidenceCodes(ecoTerms);
            annotation.setSingleReference(getSingleReference(damo.getDiseaseAnnotation().getPublication()));

            org.alliancegenome.curation_api.model.ingest.dto.ConditionRelationDTO condition = populateExperimentConditions(damo.getFishExperiment());
            List<org.alliancegenome.curation_api.model.ingest.dto.ConditionRelationDTO> conditions = new ArrayList<>();
            conditions.add(condition);
            annotation.setConditionRelations(List.of(condition));
            diseaseDTOList.add(annotation);
        }

        return diseaseDTOList;
    }

    private String getSingleReference(Publication publication) {
        return "PMID:" + publication.getAccessionNumber();
    }

    public static String format(GregorianCalendar calendar) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        fmt.setCalendar(calendar);
        String dateFormatted = fmt.format(calendar.getTime());
        return dateFormatted;
    }

    private void populateConditionClass(ExperimentalConditionDTO expcond, ExperimentCondition condition) {
        String oboID = condition.getZecoTerm().getOboID();
        if (highLevelConditionTerms.stream().map(GenericTerm::getOboID).collect(toList()).contains(oboID)) {
            expcond.setConditionClass(oboID);
        } else {
            Optional<GenericTerm> highLevelterm = highLevelConditionTerms.stream().filter(parentTerm -> getOntologyRepository().isParentChildRelationshipExist(parentTerm, condition.getZecoTerm()))
                    .findFirst();
            if (highLevelterm.isPresent()) {
                expcond.setConditionClass(highLevelterm.get().getOboID());
                expcond.setConditionId(oboID);
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
            relation.setConditionRelationType("has_condition");
            List<ExperimentalConditionDTO> expconds = new ArrayList<>();
            for (ExperimentCondition condition : allConditions) {
                ExperimentalConditionDTO expcond = new ExperimentalConditionDTO();
                String conditionStatement = condition.getZecoTerm().getTermName();
                if (condition.getAoTerm() != null) {
                    conditionStatement = conditionStatement + " " + condition.getAoTerm().getTermName();
                    expcond.setConditionAnatomy(condition.getAoTerm().getOboID());
                }
                if (condition.getChebiTerm() != null) {
                    expcond.setConditionChemical(condition.getChebiTerm().getOboID());
                    conditionStatement = conditionStatement + " " + condition.getChebiTerm().getTermName();
                }
                if (condition.getGoCCTerm() != null) {
                    expcond.setConditionGeneOntology(condition.getGoCCTerm().getOboID());
                    conditionStatement = conditionStatement + " " + condition.getGoCCTerm().getTermName();
                }
                if (condition.getTaxaonymTerm() != null) {
                    expcond.setConditionTaxon(condition.getTaxaonymTerm().getOboID());
                    conditionStatement = conditionStatement + " " + condition.getTaxaonymTerm().getTermName();
                }
                populateConditionClass(expcond, condition);
                expcond.setConditionStatement(conditionStatement);
                expconds.add(expcond);
            }
            relation.setConditions(expconds);

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
        return getEvidenceCodeFromString(diseaseAnnotations.getEvidenceCode());
    }

    private String getEvidenceCodeFromString(String ecoValue) {
        if (ecoValue.equals("ZDB-TERM-170419-250"))
            return "ECO:0000304";
        if (ecoValue.equals("ZDB-TERM-170419-251"))
            return "ECO:0000305";
        return "";
    }

    class Item {

        private String name;
        private int qty;
        private BigDecimal price;

        public Item(String name, int qty, BigDecimal price) {
            this.name = name;
            this.qty = qty;
            this.price = price;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getQty() {
            return qty;
        }

        public void setQty(int qty) {
            this.qty = qty;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        //constructors, getter/setters
    }

    // ToDo: This list should be a slim in ZECO to identify those high-level terms.
    private static final List<GenericTerm> highLevelConditionTerms = new ArrayList<>(18);

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

}
//test