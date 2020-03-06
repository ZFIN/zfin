package org.zfin.marker.agr;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.zfin.feature.Feature;
import org.zfin.marker.Marker;
import org.zfin.mutant.*;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;
import org.zfin.publication.Publication;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.zfin.repository.RepositoryFactory.getMutantRepository;

public class DiseaseInfo extends AbstractScriptWrapper {

    private int numfOfRecords = 0;

    public DiseaseInfo(int number) {
        numfOfRecords = number;
    }


    public static void main(String[] args) throws IOException {
        int number = 0;
        if (args.length > 0) {
            number = Integer.valueOf(args[0]);
        }
        DiseaseInfo diseaseInfo = new DiseaseInfo(number);
        diseaseInfo.init();
        System.exit(0);
    }

    private void init() throws IOException {
        initAll();
        AllDiseaseDTO allDiseaseDTO = getDiseaseInfo(numfOfRecords);
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
        String jsonInString = writer.writeValueAsString(allDiseaseDTO);
        try (PrintStream out = new PrintStream(new FileOutputStream("ZFIN_1.0.1.0_disease.daf.json"))) {
            out.print(jsonInString);
        }
    }

    public AllDiseaseDTO getDiseaseInfo(int numberOrRecords) {
        List<DiseaseDTO> diseaseDTOList = new ArrayList<>();


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
                                            Collectors.mapping(this::getEvidenceCodeString, Collectors.toList())
                                    )
                            );
                    // loop over each publication: final loop as each publication should generate a individual record in the file.
                    evidenceMap.forEach((publication, evidenceSet) -> {
                        // Use wildtype fish with STR
                        // treat as purely implicated by a gene
                        if (genotype.isWildtype()) {
                            DiseaseDTO strDiseaseDto = getBaseDiseaseDTO(gene.getZdbID(), gene.getAbbreviation(), disease);
                            RelationshipDTO relationship = new RelationshipDTO(RelationshipDTO.IS_IMPLICATED_IN, RelationshipDTO.GENE);
                            strDiseaseDto.setObjectRelation(relationship);
                            List<String> geneticEntityIds = new ArrayList<>();
                            geneticEntityIds.add("ZFIN:" + fish.getZdbID());
                            strDiseaseDto.setPrimaryGeneticEntityIDs(geneticEntityIds);
                            // evidence
                            strDiseaseDto.setEvidence(getEvidenceDTO(publication, evidenceSet));
                            strDiseaseDto.setPrimaryGeneticEntityIDs(geneticEntityIds);
                            diseaseDTOList.add(strDiseaseDto);

                            DiseaseDTO fishDiseaseDto = getBaseDiseaseDTO(fish.getZdbID(), fish.getName(), disease);
                            RelationshipDTO fishRelationship = new RelationshipDTO(RelationshipDTO.IS_MODEL_OF, RelationshipDTO.FISH);
                            fishDiseaseDto.setObjectRelation(fishRelationship);
                            fishDiseaseDto.setEvidence(getEvidenceDTO(publication, evidenceSet));
                            diseaseDTOList.add(fishDiseaseDto);

                        } else {
                            genotype.getGenotypeFeatures().forEach(genotypeFeature -> {
                                Feature feature = genotypeFeature.getFeature();
                                // is it a single-allelic feature use it
                                // otherwise discard record
                                if (fish.getFishFunctionalAffectedGeneCount() == 1) {
                                    if (feature.isSingleAlleleOfMarker(gene)) {
                                        DiseaseDTO FeatureDiseaseDto = getBaseDiseaseDTO(feature.getZdbID(), feature.getAbbreviation(), disease);
                                        RelationshipDTO alleleRelationship = new RelationshipDTO(RelationshipDTO.IS_IMPLICATED_IN, RelationshipDTO.ALELLE);
                                        List<String> geneticEntityIds = new ArrayList<>();
                                        geneticEntityIds.add("ZFIN:" + fish.getZdbID());
                                        FeatureDiseaseDto.setPrimaryGeneticEntityIDs(geneticEntityIds);
                                        FeatureDiseaseDto.setObjectRelation(alleleRelationship);
                                        FeatureDiseaseDto.setEvidence(getEvidenceDTO(publication, evidenceSet));
                                        //populateExperimentConditions(fishExperiment, FeatureDiseaseDto);
                                        diseaseDTOList.add(FeatureDiseaseDto);
                                    } else {
                                        DiseaseDTO FeatureDiseaseDto = getBaseDiseaseDTO(gene.getZdbID(), gene.getAbbreviation(), disease);
                                        RelationshipDTO relationship = new RelationshipDTO(RelationshipDTO.IS_IMPLICATED_IN, RelationshipDTO.GENE);
                                        List<String> geneticEntityIds = new ArrayList<>();
                                        geneticEntityIds.add("ZFIN:" + fish.getZdbID());
                                        FeatureDiseaseDto.setPrimaryGeneticEntityIDs(geneticEntityIds);
                                        FeatureDiseaseDto.setObjectRelation(relationship);
                                        FeatureDiseaseDto.setEvidence(getEvidenceDTO(publication, evidenceSet));
                                        //populateExperimentConditions(fishExperiment, FeatureDiseaseDto);
                                        diseaseDTOList.add(FeatureDiseaseDto);

                                    }
                                }
                            });
                            DiseaseDTO fishDiseaseDto = getBaseDiseaseDTO(fish.getZdbID(), fish.getName(), disease);
                            RelationshipDTO fishRelationship = new RelationshipDTO(RelationshipDTO.IS_MODEL_OF, RelationshipDTO.FISH);
                            fishDiseaseDto.setObjectRelation(fishRelationship);
                            fishDiseaseDto.setEvidence(getEvidenceDTO(publication, evidenceSet));
                            diseaseDTOList.add(fishDiseaseDto);
                        }
                    });
                });
            });
        });


        AllDiseaseDTO allDiseaseDTO = new AllDiseaseDTO();
        String dataProvider = "ZFIN";
        List<String> pages = new ArrayList<>();
        pages.add("homepage");
        DataProviderDTO dp = new DataProviderDTO("curated", new CrossReferenceDTO(dataProvider, dataProvider, pages));
        MetaDataDTO meta = new MetaDataDTO(new DataProviderDTO("curated", new CrossReferenceDTO(dataProvider, dataProvider, pages)));
        allDiseaseDTO.setMetaData(meta);
        allDiseaseDTO.setDiseaseList(diseaseDTOList);
        return allDiseaseDTO;
    }

    public DiseaseDTO getBaseDiseaseDTO(String zdbID, String abbreviation, GenericTerm disease) {
        DiseaseDTO strDiseaseDto = new DiseaseDTO();
        strDiseaseDto.setObjectId(zdbID);
        strDiseaseDto.setObjectName(abbreviation);
        List<String> pages = new ArrayList<>();
        pages.add("homepage");
        List<DataProviderDTO> dpList = new ArrayList<>();
        dpList.add(new DataProviderDTO("curated", new CrossReferenceDTO("ZFIN", "ZFIN", pages)));
        strDiseaseDto.setDataProvider(dpList);
        strDiseaseDto.setDoid(disease.getOboID());
        return strDiseaseDto;
    }

    public void populateExperimentConditions(FishExperiment fishExperiment, DiseaseDTO alleleDto) {
        if (fishExperiment.getExperiment() != null) {
            Set<ExperimentalConditionDTO> experimentalConditionDTOS = getExperimentalConditionDTOS(fishExperiment);
            alleleDto.setExperimentalConditions(experimentalConditionDTOS);
            alleleDto.setExperimentalConditions(experimentalConditionDTOS);
        }
    }

    public EvidenceDTO getEvidenceDTO(Publication publication, List<String> evidences) {
        PublicationAgrDTO fixedPub = new PublicationAgrDTO();
        List<String> pubPages = new ArrayList<>();
        pubPages.add("reference");
        CrossReferenceDTO pubXref = new CrossReferenceDTO("ZFIN", publication.getZdbID(), pubPages);
        if (publication.getAccessionNumber() != null) {
            fixedPub.setPublicationId("PMID:"+publication.getAccessionNumber());
            fixedPub.setCrossReference(pubXref);
        }
        else {
            fixedPub.setPublicationId("ZFIN:"+publication.getZdbID());
        }

        EvidenceDTO evDto = new EvidenceDTO(fixedPub);
        evDto.setEvidenceCodes(evidences);
        return evDto;
    }

    public Set<ExperimentalConditionDTO> getExperimentalConditionDTOS(FishExperiment fishExperiment) {
        return fishExperiment.getExperiment().getExperimentConditions().stream()
                .map(expCondition -> new ExperimentalConditionDTO(expCondition.getDisplayName(), expCondition.getZecoTerm().getOboID()))
                .collect(Collectors.toSet());
    }

    // hard-coded for now as the ECO ontology does not provide the codes in
    // abbreviated form easily. The term names are very long and only in the synonym list
    // you can find TAS an IC.
    // Needs to be changed in the future.
    private String getEvidenceCodeString(DiseaseAnnotation diseaseAnnotations) {
        if (diseaseAnnotations.getEvidenceCode().equals("ZDB-TERM-170419-250"))
            return "ECO:0000304";
        if (diseaseAnnotations.getEvidenceCode().equals("ZDB-TERM-170419-251"))
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
