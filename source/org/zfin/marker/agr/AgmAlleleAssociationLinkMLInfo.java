package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.alliancegenome.curation_api.model.ingest.dto.IngestDTO;
import org.alliancegenome.curation_api.model.ingest.dto.associations.AgmAlleleAssociationDTO;
import org.zfin.mutant.Fish;
import org.zfin.mutant.GenotypeFeature;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.zfin.repository.RepositoryFactory.getFishRepository;

public class AgmAlleleAssociationLinkMLInfo extends LinkMLInfo {

    public AgmAlleleAssociationLinkMLInfo(int number) {
        super(number);
    }


    public static void main(String[] args) throws IOException {
        int number = 0;
        mainParent(args);
        AgmAlleleAssociationLinkMLInfo linkMLInfo = new AgmAlleleAssociationLinkMLInfo(number);
        linkMLInfo.init();
        System.exit(0);
    }

    private void init() throws IOException {
        initAll();
        IngestDTO ingestDTO = getIngestDTO();
        List<AgmAlleleAssociationDTO> agmAlleleAssociationDtos = getAgmAlleleAssociations();
        ingestDTO.setAgmAlleleAssociationIngestSet(agmAlleleAssociationDtos);
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
        String jsonInString = writer.writeValueAsString(ingestDTO);
        try (PrintStream out = new PrintStream(new FileOutputStream("ZFIN_1.0.1.4_AgmAlleleAssociation_ml.json"))) {
            out.print(jsonInString);
        }
    }

    public List<AgmAlleleAssociationDTO> getAgmAlleleAssociations() {
        return getFishRepository().getAllFish(0)
            .stream()
            .flatMap(fish -> fish.getGenotype().getGenotypeFeatures().stream()
                .map(genoFeature -> createAssociationDTO(fish, genoFeature)))
            .collect(toList());
    }

    private AgmAlleleAssociationDTO createAssociationDTO(Fish fish, GenotypeFeature genoFeature) {
        AgmAlleleAssociationDTO dto = new AgmAlleleAssociationDTO();
        dto.setAlleleIdentifier(getGlobalId(genoFeature.getFeature().getZdbID()));
        dto.setAgmSubjectIdentifier(getGlobalId(fish.getZdbID()));
        dto.setZygosityCurie(genoFeature.getZygosity().getGenoOntologyID());
        dto.setRelationName("contains");
        return dto;
    }

    private String getGlobalId(String zdbID) {
        return "ZFIN:" + zdbID;
    }


}
