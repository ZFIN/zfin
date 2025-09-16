package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.alliancegenome.curation_api.model.ingest.dto.IngestDTO;
import org.alliancegenome.curation_api.model.ingest.dto.associations.AlleleGeneAssociationDTO;
import org.zfin.feature.Feature;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.zfin.repository.RepositoryFactory.getFeatureRepository;

public class AlleleGeneAssociationLinkMLInfo extends LinkMLInfo {

    public AlleleGeneAssociationLinkMLInfo(int number) {
        super(number);
    }


    public static void main(String[] args) throws IOException {
        int number = 0;
        mainParent(args);
        AlleleGeneAssociationLinkMLInfo diseaseInfo = new AlleleGeneAssociationLinkMLInfo(number);
        diseaseInfo.init();
        System.exit(0);
    }

    private void init() throws IOException {
        initAll();
        IngestDTO ingestDTO = getIngestDTO();
        List<AlleleGeneAssociationDTO> allDiseaseDTO = getAlleleGeneAssociations(numfOfRecords);
        ingestDTO.setAlleleGeneAssociationIngestSet(allDiseaseDTO);
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
        String jsonInString = writer.writeValueAsString(ingestDTO);
        try (PrintStream out = new PrintStream(new FileOutputStream("ZFIN_1.0.1.4_AlleleGeneAssociation_ml.json"))) {
            out.print(jsonInString);
        }
    }

    public List<AlleleGeneAssociationDTO> getAlleleGeneAssociations(int numberOrRecords) {
        List<Feature> features = getFeatureRepository().getAllFeatureList(numberOrRecords);
        return features.stream()
            .filter(feature -> feature.getAllelicGene() != null && feature.getAllelicGene().getZdbID() != null)
            .map(feature -> {
                AlleleGeneAssociationDTO dto = new AlleleGeneAssociationDTO();
                dto.setAlleleIdentifier("ZFIN:" + feature.getZdbID());
                dto.setGeneIdentifier("ZFIN:" + feature.getAllelicGene().getZdbID());
                dto.setRelationName("is_allele_of");
                return dto;
            })
            .collect(toList());
    }

}
