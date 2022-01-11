package org.zfin.marker.agr;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.alliancegenome.curation_api.model.entities.AGMDiseaseAnnotation;
import org.alliancegenome.curation_api.model.entities.AffectedGenomicModel;
import org.alliancegenome.curation_api.model.entities.Gene;
import org.alliancegenome.curation_api.model.entities.ontology.DOTerm;
import org.alliancegenome.curation_api.model.entities.ontology.EcoTerm;
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
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static org.zfin.repository.RepositoryFactory.getMutantRepository;

public class DiseaseAnnotationMlInfo extends AbstractScriptWrapper {

    private int numfOfRecords = 0;

    public DiseaseAnnotationMlInfo(int number) {
        numfOfRecords = number;
    }


    public static void main(String[] args) throws IOException {
        int number = 0;
        if (args.length > 0) {
            number = Integer.valueOf(args[0]);
        }
        DiseaseAnnotationMlInfo diseaseInfo = new DiseaseAnnotationMlInfo(number);
        diseaseInfo.init();
        System.exit(0);
    }

    private void init() throws IOException {
        initAll();
        List<AGMDiseaseAnnotation> allDiseaseDTO = getDiseaseInfo(numfOfRecords);
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
        String jsonInString = writer.writeValueAsString(allDiseaseDTO);
        try (PrintStream out = new PrintStream(new FileOutputStream("ZFIN_1.0.1.4_disease_annotation_ml.json"))) {
            out.print(jsonInString);
        }
    }

    public List<AGMDiseaseAnnotation> getDiseaseInfo(int numberOrRecords) {
        List<AGMDiseaseAnnotation> diseaseDTOList = new ArrayList<>();


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
                        AGMDiseaseAnnotation annotation = new AGMDiseaseAnnotation();
                        annotation.setPredicate(RelationshipDTO.IS_MODEL_OF);
                        AffectedGenomicModel model = getAffectedGenomicModel(fish);
                        Random random = new Random();
                        long r = 1000 + (long) (new Random().nextFloat() * (10000 - 1000));
                        annotation.setId(r);
                        annotation.setSubject(model);

                        DOTerm diseaseTerm = new DOTerm();
                        diseaseTerm.setCurie(disease.getOboID());
                        annotation.setObject(diseaseTerm);

                        List<EcoTerm> evidenceCodes = evidenceSet.stream()
                                .map(ZfinAllianceConverter::convertEvidenceCodes)
                                .flatMap(Collection::stream)
                                .collect(toList());
                        annotation.setEvidenceCodes(evidenceCodes);
                        annotation.setReference(ZfinAllianceConverter.convertReference(publication));

                        if (genotype.isWildtype()) {
                            // inferred Genes
                            Gene inferredGene = new Gene();
                            inferredGene.setCurie("ZFIN:" + gene.getZdbID());
                            annotation.setInferredGene(inferredGene);
                            diseaseDTOList.add(annotation);
                        } else {
/*
                            ConditionRelationDTO condition = populateExperimentConditions(fishExperiment, fishDiseaseDto);
                            List<ConditionRelationDTO> conditions = new ArrayList<>();
                            conditions.add(condition);
*/
                        }
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
            AGMDiseaseAnnotation annotation = new AGMDiseaseAnnotation();
            annotation.setPredicate(RelationshipDTO.IS_MODEL_OF);
            AffectedGenomicModel model = getAffectedGenomicModel(fish);
            annotation.setSubject(model);

            DOTerm diseaseTerm = new DOTerm();
            diseaseTerm.setCurie(damo.getDiseaseAnnotation().getDisease().getOboID());
            annotation.setObject(diseaseTerm);

            annotation.setEvidenceCodes(ZfinAllianceConverter.convertEvidenceCodes(damo.getDiseaseAnnotation().getEvidenceCode()));
            annotation.setReference(ZfinAllianceConverter.convertReference(damo.getDiseaseAnnotation().getPublication()));

/*
            ConditionRelationDTO relation = new ConditionRelationDTO();
            List<ExperimentCondition> allConditions = getMutantRepository().getExperimentConditions(damo.getFishExperiment().getExperiment());
            relation.setConditionRelationType("has_condition");
            List<ExperimentConditionDTO> expconds2 = new ArrayList<>();
            for (ExperimentCondition conditionz : allConditions) {


                ExperimentConditionDTO expconda = new ExperimentConditionDTO();
                if (conditionz.getAoTerm() != null) {
                    expconda.setAnatomicalOntologyId(conditionz.getAoTerm().getOboID());
                }
                if (conditionz.getChebiTerm() != null) {
                    expconda.setChemicalOntologyId(conditionz.getChebiTerm().getOboID());
                }
                if (conditionz.getGoCCTerm() != null) {
                    expconda.setGeneOntologyId(conditionz.getGoCCTerm().getOboID());
                }
                if (conditionz.getTaxaonymTerm() != null) {
                    expconda.setNcbiTaxonId(conditionz.getTaxaonymTerm().getOboID());
                }
                expconda.setConditionClassId(conditionz.getZecoTerm().getOboID());
                expconda.setConditionStatement(conditionz.getDisplayName());
                expconds2.add(expconda);
            }
            relation.setConditions(expconds2);

            List<ConditionRelationDTO> conditions = new ArrayList<>();
            conditions.add(relation);
*/
            diseaseDTOList.add(annotation);
        }

        return diseaseDTOList;
    }

    private AffectedGenomicModel getAffectedGenomicModel(Fish fish) {
        AffectedGenomicModel model = new AffectedGenomicModel();
        model.setCurie("ZFIN:" + fish.getZdbID());
        model.setName(fish.getDisplayName());
        return model;
    }

    public ConditionRelationDTO populateExperimentConditions(FishExperiment fishExperiment, DiseaseDTO alleleDto) {
        ConditionRelationDTO relation = new ConditionRelationDTO();
        if (fishExperiment.getExperiment() != null) {
            List<ExperimentCondition> allConditions = getMutantRepository().getExperimentConditions(fishExperiment.getExperiment());
            relation.setConditionRelationType("has_condition");
            List<ExperimentConditionDTO> expconds = new ArrayList<>();
            for (ExperimentCondition condition : allConditions) {
                ExperimentConditionDTO expcond = new ExperimentConditionDTO();
                String conditionStatement = condition.getZecoTerm().getTermName();
                if (condition.getAoTerm() != null) {
                    conditionStatement = conditionStatement + " " + condition.getAoTerm().getTermName();
                    expcond.setAnatomicalOntologyId(condition.getAoTerm().getOboID());
                }
                if (condition.getChebiTerm() != null) {
                    expcond.setChemicalOntologyId(condition.getChebiTerm().getOboID());
                    conditionStatement = conditionStatement + " " + condition.getChebiTerm().getTermName();
                }
                if (condition.getGoCCTerm() != null) {
                    expcond.setGeneOntologyId(condition.getGoCCTerm().getOboID());
                    conditionStatement = conditionStatement + " " + condition.getGoCCTerm().getTermName();
                }
                if (condition.getTaxaonymTerm() != null) {
                    expcond.setNcbiTaxonId(condition.getTaxaonymTerm().getOboID());
                    conditionStatement = conditionStatement + " " + condition.getTaxaonymTerm().getTermName();
                }
                expcond.setConditionClassId(condition.getZecoTerm().getOboID());
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
}
//test