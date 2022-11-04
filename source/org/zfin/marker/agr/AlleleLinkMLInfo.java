package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.alliancegenome.curation_api.model.ingest.dto.AlleleDTO;
import org.alliancegenome.curation_api.model.ingest.dto.IngestDTO;
import org.zfin.feature.Feature;
import org.zfin.infrastructure.ActiveData;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.GregorianCalendar;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.zfin.repository.RepositoryFactory.getFeatureRepository;
import static org.zfin.repository.RepositoryFactory.getPublicationRepository;

public class AlleleLinkMLInfo extends LinkMLInfo {

    public AlleleLinkMLInfo(int number) {
        super(number);
    }


    public static void main(String[] args) throws IOException {
        int number = 0;
        if (args.length > 0) {
            number = Integer.parseInt(args[0]);
        }
        AlleleLinkMLInfo diseaseInfo = new AlleleLinkMLInfo(number);
        diseaseInfo.init();
        System.exit(0);
    }

    private void init() throws IOException {
        initAll();
        IngestDTO ingestDTO = new IngestDTO();
        List<AlleleDTO> allDiseaseDTO = getAllAlleles(numfOfRecords);
        ingestDTO.setAlleleIngestSet(allDiseaseDTO);
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
        String jsonInString = writer.writeValueAsString(ingestDTO);
        try (PrintStream out = new PrintStream(new FileOutputStream("ZFIN_1.0.1.4_Allele_ml.json"))) {
            out.print(jsonInString);
        }
    }

    public List<AlleleDTO> getAllAlleles(int numberOrRecords) {
        List<Feature> features = getFeatureRepository().getAllFeatureList(numberOrRecords);
        return features.stream()
            .map(feature -> {
                AlleleDTO dto = new AlleleDTO();
                dto.setSymbol(feature.getAbbreviation());
                dto.setName(feature.getName());
                dto.setInternal(false);
                dto.setCreatedBy("ZFIN:CURATOR");
                dto.setTaxon(ZfinDTO.taxonId);
                dto.setCurie("ZFIN:" + feature.getZdbID());
                if (feature.getFtrEntryDate() != null) {
                    dto.setDateCreated(format(feature.getFtrEntryDate()));
                } else {
                    GregorianCalendar date = ActiveData.getDateFromId(feature.getZdbID());
                    dto.setDateCreated(format(date));
                }
                dto.setReferences(getReferencesById(feature.getZdbID()));
                if (feature.getSingleRelatedGenotype() != null) {
                    dto.setIsExtinct(feature.getSingleRelatedGenotype().isExtinct());
                }
                return dto;
            })
            .collect(toList());
    }

    private List<String> getReferencesById(String zdbID) {
        return getPublicationRepository().getPubsForDisplay(zdbID).stream()
            .map(this::getSingleReference)
            .collect(toList());
    }

}
