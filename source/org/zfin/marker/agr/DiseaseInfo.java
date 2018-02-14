package org.zfin.marker.agr;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.zfin.expression.ExperimentCondition;
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
import java.util.*;
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
        try (PrintStream out = new PrintStream(new FileOutputStream("ZFIN_1.0.4_disease.json"))) {
            out.print(jsonInString);
        }
    }

    public AllDiseaseDTO getDiseaseInfo(int numberOrRecords) {
        List<DiseaseDTO> diseaseDTOList = new ArrayList<>();

        /**
         List<DiseaseAnnotationModel> diseaseModels = getMutantRepository().getDiseaseAnnotationModels(numberOrRecords);

         Map<Fish, Set<DiseaseAnnotation>> modelList =
         diseaseModels.stream().collect(
         Collectors.groupingBy(w -> w.getFishExperiment().getFish(),
         Collectors.mapping(DiseaseAnnotationModel::getDiseaseAnnotation, Collectors.toSet())
         )
         );

         modelList.forEach((fish, diseaseAnnotationSet) -> {
         Map<GenericTerm, Set<DiseaseAnnotation>> termMap = modelList.get(fish)
         .stream()
         .collect(
         Collectors.groupingBy(DiseaseAnnotation::getDisease,
         Collectors.mapping(d -> d, Collectors.toSet())
         )
         );


         termMap.forEach((disease, diseaseAnnotationEvSet) -> {
         Map<Publication, List<String>> evidenceMap = diseaseAnnotationEvSet
         .stream()
         .collect(
         Collectors.groupingBy(DiseaseAnnotation::getPublication,
         Collectors.mapping(DiseaseAnnotation::getEvidenceCode, Collectors.toList())
         )
         );
         evidenceMap.forEach((publication, evidences) -> {
         DiseaseDTO dto = new DiseaseDTO();
         dto.setObjectId(fish.getZdbID());
         dto.setObjectName(fish.getName());

         RelationshipDTO relationship = new RelationshipDTO(RelationshipDTO.IS_MODEL_OF, RelationshipDTO.FISH);
         Set<String> inferredSet = new HashSet<>();
         fish.getFishExperiments().forEach(fishExperiment -> {
         fishExperiment.getGeneGenotypeExperiments()
         .stream()
         .filter(geneGenotypeExperiment -> geneGenotypeExperiment.getGene().isGenedom())
         .forEach(geneGenotypeExperiment ->
         inferredSet.add(ZfinDTO.ZFIN + geneGenotypeExperiment.getGene().getZdbID()));
         });
         relationship.setInferredGeneAssociation(inferredSet);
         dto.setObjectRelation(relationship);

         dto.setDataProvider(DataProvider.ZFIN);

         dto.setDoid(disease.getOboID());

         // Evidence
         PublicationAgrDTO pubDto = new PublicationAgrDTO(publication.getZdbID(), publication.getAccessionNumber());
         EvidenceDTO evDto = new EvidenceDTO(pubDto);
         evDto.setEvidenceCodes(evidences);
         dto.setEvidence(evDto);

         // experimental conditions
         Set<ExperimentCondition> expConditionSet = new HashSet<>();
         diseaseAnnotationEvSet.forEach(diseaseAnnotation -> {
         diseaseAnnotation.getDiseaseAnnotationModel().forEach(diseaseAnnotationModel -> {
         if (diseaseAnnotation.getPublication().equals(publication))
         expConditionSet.addAll(diseaseAnnotationModel.getFishExperiment().getExperiment().getExperimentConditions());
         });
         });
         Set<ExperimentalConditionDTO> experimentalConditionDTOS = expConditionSet.stream()
         .map(expCondition -> {
         ExperimentalConditionDTO condition = new ExperimentalConditionDTO(expCondition.getDisplayName(), expCondition.getZecoTerm().getOboID());
         if (expCondition.getAoTerm() != null)
         condition.setAnatomicalId(expCondition.getAoTerm().getOboID());
         if (expCondition.getGoCCTerm() != null)
         condition.setGeneOntologyId(expCondition.getGoCCTerm().getOboID());
         if (expCondition.getChebiTerm() != null)
         condition.setChebiOntologyId(expCondition.getChebiTerm().getOboID());
         if (expCondition.getTaxaonymTerm() != null)
         condition.setNcbiTaxonIdId(expCondition.getTaxaonymTerm().getOboID());
         return condition;
         })
         .collect(Collectors.toSet());
         dto.setExperimentalConditions(experimentalConditionDTOS);

         diseaseDTOList.add(dto);
         });
         });


         });
         **/

        // get all genes from mutant_fast_search table and list their disease info
        List<GeneGenotypeExperiment> geneGenotypeExperiments = getMutantRepository().getGeneDiseaseAnnotationModels(numberOrRecords);

        // group by gene records
        Map<Marker, Set<DiseaseAnnotation>> geneMap =
                geneGenotypeExperiments.stream().collect(
                        Collectors.toMap(GeneGenotypeExperiment::getGene,
                                (w) -> w.getFishExperiment().getDiseaseAnnotationModels().stream()
                                        .map(DiseaseAnnotationModel::getDiseaseAnnotation)
                                        .collect(Collectors.toSet()), (p1, p2) -> {
                                    p1.addAll(p2);
                                    return p1;
                                }));

        // keep track of the fish experiments for each gene
        // need this to retrieve the experimental conditions later
        Map<Marker, Set<FishExperiment>> diseaseModelMap =
                geneGenotypeExperiments.stream().collect(
                        Collectors.groupingBy(GeneGenotypeExperiment::getGene,
                                Collectors.mapping(GeneGenotypeExperiment::getFishExperiment, Collectors.toSet())));

        // loop over all genes
        geneMap.forEach((gene, diseaseAnnotationSet) -> {
            Map<GenericTerm, Set<DiseaseAnnotation>> termMap = geneMap.get(gene)
                    .stream()
                    .collect(
                            Collectors.groupingBy(DiseaseAnnotation::getDisease,
                                    Collectors.mapping(d -> d, Collectors.toSet())
                            )
                    );
            termMap.forEach((disease, diseaseAnnotationEvSet) -> {
                Map<Publication, List<String>> evidenceMap = diseaseAnnotationEvSet
                        .stream()
                        .collect(
                                Collectors.groupingBy(DiseaseAnnotation::getPublication,
                                        Collectors.mapping(diseaseAnnotations -> {
                                            if (getEvidenceCodeString(diseaseAnnotations).equals("TAS"))
                                                return "IC";
                                            return getEvidenceCodeString(diseaseAnnotations);
                                        }, Collectors.toList())
                                )
                        );
                evidenceMap.forEach((publication, evidences) -> {
                    // loop over all fixExperiments and check if feature or morphant.
                    diseaseModelMap.get(gene).forEach(fishExperiment -> {
                        Genotype genotype = fishExperiment.getFish().getGenotype();
                        // Use wildtype fish with STR
                        // treat as purely implicated by a gene
                        if (genotype.isWildtype()) {
                            DiseaseDTO strDiseaseDto = getBaseDiseaseDTO(gene.getZdbID(), gene.getAbbreviation(), disease);
                            RelationshipDTO relationship = new RelationshipDTO(RelationshipDTO.IS_IMPLICATED_IN, RelationshipDTO.GENE);
                            strDiseaseDto.setObjectRelation(relationship);
                            // evidence
                            strDiseaseDto.setEvidence(getEvidenceDTO(publication, evidences));
                            populateExperimentConditions(fishExperiment, strDiseaseDto);
                            diseaseDTOList.add(strDiseaseDto);

                        } // otherwise it is a single-allelic feature
                        else {
                            genotype.getGenotypeFeatures().forEach(genotypeFeature -> {
                                Feature feature = genotypeFeature.getFeature();
                                if (feature.isSingleAlleleOfMarker(gene)) {
                                    DiseaseDTO FeatureDiseaseDto = getBaseDiseaseDTO(feature.getZdbID(), feature.getAbbreviation(), disease);
                                    RelationshipDTO alleleRelationship = new RelationshipDTO(RelationshipDTO.IS_IMPLICATED_IN, RelationshipDTO.ALELLE);
                                    FeatureDiseaseDto.setObjectRelation(alleleRelationship);
                                    FeatureDiseaseDto.setEvidence(getEvidenceDTO(publication, evidences));
                                    populateExperimentConditions(fishExperiment, FeatureDiseaseDto);
                                    diseaseDTOList.add(FeatureDiseaseDto);
                                }
                            });
                        }
                    });
                });


            });

        });


/*
 * might ceom from an external source fro AGR
        List<OmimPhenotype> geneModels = getMutantRepository().getDiseaseModelsFromGenes(numberOrRecords);
        geneModels.forEach((OmimPhenotype omimPhenotype) -> {
            omimPhenotype.getExternalReferences().forEach(termReference -> {
                DiseaseDTO dto = new DiseaseDTO();
                dto.setObjectId(omimPhenotype.getOrtholog().getZebrafishGene().getZdbID());
                dto.setObjectName(omimPhenotype.getOrtholog().getZebrafishGene().getAbbreviation());
                RelationshipDTO relationship = new RelationshipDTO(RelationshipDTO.IS_MARKER_OF, RelationshipDTO.GENE);
                dto.setObjectRelation(relationship);
                dto.setDataProvider(DataProvider.ZFIN);
                dto.setDOid(termReference.getTerm().getOboID());
                EvidenceDTO evidenceDTO = new EvidenceDTO("ISS");
                evidenceDTO.setPublications(Collections.singletonList(new PublicationAgrDTO("ZDB-PUB-170210-12", "")));
                dto.setEvidence(Collections.singletonList(evidenceDTO));
                dto.setInferredGeneAssociation(Collections.singleton(omimPhenotype.getOrtholog().getZebrafishGene().getZdbID()));
                diseaseDTOList.add(dto);
            });
        });
*/


        AllDiseaseDTO allDiseaseDTO = new AllDiseaseDTO();
        //allDiseaseDTO.setGenes(allGeneDTOList);
        MetaDataDTO meta = new MetaDataDTO("ZFIN");
        allDiseaseDTO.setMetaData(meta);
        allDiseaseDTO.setDiseaseList(diseaseDTOList);
        return allDiseaseDTO;
    }

    public DiseaseDTO getBaseDiseaseDTO(String zdbID, String abbreviation, GenericTerm disease) {
        DiseaseDTO strDiseaseDto = new DiseaseDTO();
        strDiseaseDto.setObjectId(zdbID);
        strDiseaseDto.setObjectName(abbreviation);
        strDiseaseDto.setDataProvider(DataProvider.ZFIN);
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
        PublicationAgrDTO fixedPub = new PublicationAgrDTO(publication.getZdbID(), publication.getAccessionNumber());
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
            return "TAS";
        if (diseaseAnnotations.getEvidenceCode().equals("ZDB-TERM-170419-251"))
            return "IC";
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
