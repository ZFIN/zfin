package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.alliancegenome.curation_api.model.ingest.dto.DataProviderDTO;
import org.alliancegenome.curation_api.model.ingest.dto.IngestDTO;
import org.alliancegenome.curation_api.model.ingest.dto.associations.agmAssociations.AgmSequenceTargetingReagentAssociationDTO;
import org.alliancegenome.curation_api.model.ingest.dto.slotAnnotions.alleleSlotAnnotations.AlleleMutationTypeSlotAnnotationDTO;
import org.zfin.infrastructure.ActiveData;
import org.zfin.mutant.Fish;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.zfin.repository.RepositoryFactory.getFishRepository;
import static org.zfin.repository.RepositoryFactory.getPublicationRepository;

public class AgmStrAssociationLinkMLInfo extends LinkMLInfo {

    public AgmStrAssociationLinkMLInfo(int number) {
        super(number);
    }


    public static void main(String[] args) throws IOException {
        int number = 0;
        mainParent(args);
        AgmStrAssociationLinkMLInfo diseaseInfo = new AgmStrAssociationLinkMLInfo(number);
        diseaseInfo.init();
        System.exit(0);
    }

    private void init() throws IOException {
        initAll();
        IngestDTO ingestDTO = getIngestDTO();
        List<AgmSequenceTargetingReagentAssociationDTO> allDiseaseDTO = getAllAlleles(numfOfRecords);
        ingestDTO.setAgmStrAssociationIngestSet(allDiseaseDTO);
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
        String jsonInString = writer.writeValueAsString(ingestDTO);
        try (PrintStream out = new PrintStream(new FileOutputStream("allele-str.json"))) {
            out.print(jsonInString);
        }
    }

    public List<AgmSequenceTargetingReagentAssociationDTO> getAllAlleles(int numberOrRecords) {
        List<Fish> allFish = getFishRepository().getAllFish(numberOrRecords);
        return allFish.stream()
            .map(fish ->
                fish.getStrList().stream().map(str -> {
                    AgmSequenceTargetingReagentAssociationDTO dto = new AgmSequenceTargetingReagentAssociationDTO();
                    dto.setAgmSubjectIdentifier("ZFIN:" + fish.getZdbID());
                    dto.setRelationName("contains");
                    dto.setSequenceTargetingReagentIdentifier("ZFIN:" + str.getZdbID());
                    dto.setCreatedByCurie("ZFIN:CURATOR");
                    GregorianCalendar date = ActiveData.getDateFromId(fish.getZdbID());
                    dto.setDateCreated(format(date));
                    org.alliancegenome.curation_api.model.ingest.dto.DataProviderDTO dataProvider = new DataProviderDTO();
                    dataProvider.setSourceOrganizationAbbreviation("ZFIN");
                    return dto;
                }).toList()
            ).flatMap(Collection::stream).toList();
    }

}
