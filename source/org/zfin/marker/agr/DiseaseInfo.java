package org.zfin.marker.agr;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.zfin.expression.ExperimentCondition;
import org.zfin.mutant.DiseaseAnnotation;
import org.zfin.mutant.DiseaseAnnotationModel;
import org.zfin.mutant.Fish;
import org.zfin.mutant.OmimPhenotype;
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
        try (PrintStream out = new PrintStream(new FileOutputStream("disease-info-zfin.json"))) {
            out.print(jsonInString);
        }
        System.out.println(jsonInString);
        String name = "";
    }

    public AllDiseaseDTO getDiseaseInfo(int numberOrRecords) {
        List<DiseaseDTO> diseaseDTOList = new ArrayList<>();

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
            DiseaseDTO dto = new DiseaseDTO();
            dto.setObjectId(fish.getZdbID());
            dto.setObjectName(fish.getName());
            RelationshipDTO relationship = new RelationshipDTO(RelationshipDTO.IS_MODEL_OF, RelationshipDTO.FISH);
            dto.setObjectRelation(relationship);
            dto.setDataProvider(DataProvider.ZFIN);
            Set<String> inferredSet = new HashSet<>();
            fish.getFishExperiments().forEach(fishExperiment -> {
                fishExperiment.getGeneGenotypeExperiments().forEach(geneGenotypeExperiment ->
                        inferredSet.add(geneGenotypeExperiment.getGene().getZdbID()));
            });
            dto.setInferredGeneAssociation(inferredSet);
            termMap.forEach((disease, diseaseAnnotationEvSet) -> {
                Map<String, List<Publication>> evidenceMap = diseaseAnnotationEvSet
                        .stream()
                        .collect(
                                Collectors.groupingBy(DiseaseAnnotation::getEvidenceCode,
                                        Collectors.mapping(DiseaseAnnotation::getPublication, Collectors.toList())
                                )
                        );
                dto.setDOid(disease.getOboID());
                List<EvidenceDTO> evidenceList = new ArrayList<>();
                dto.setEvidence(evidenceList);
                evidenceMap.forEach((evidence, publicationList) -> {
                    EvidenceDTO evDto = new EvidenceDTO(evidence);
                    List<PublicationAgrDTO> pubDtoList = publicationList.stream()
                            .map(p -> new PublicationAgrDTO(p.getZdbID(), p.getAccessionNumber()))
                            .collect(Collectors.toList());
                    evDto.setPublications(pubDtoList);
                    evidenceList.add(evDto);
                });

            });

            // experimental conditions
            Set<ExperimentCondition> expConditionSet = new HashSet<>();
            fish.getFishExperiments().forEach(fishExperiment -> {
                if (fishExperiment.getExperiment() != null)
                    expConditionSet.addAll(fishExperiment.getExperiment().getExperimentConditions());
            });
            Set<ExperimentalConditionDTO> experimentalConditionDTOS = expConditionSet.stream()
                    .map(expCondition -> new ExperimentalConditionDTO(expCondition.getDisplayName(), expCondition.getZecoTerm().getOboID()))
                    .collect(Collectors.toSet());
            dto.setExperimentalConditions(experimentalConditionDTOS);
            diseaseDTOList.add(dto);
        });

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


        AllDiseaseDTO allDiseaseDTO = new AllDiseaseDTO();
        //allDiseaseDTO.setGenes(allGeneDTOList);
        MetaDataDTO meta = new MetaDataDTO("ZFIN");
        allDiseaseDTO.setMetaData(meta);
        allDiseaseDTO.setDiseaseList(diseaseDTOList);
        return allDiseaseDTO;
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
