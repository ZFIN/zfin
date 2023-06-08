package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.alliancegenome.curation_api.model.ingest.dto.AffectedGenomicModelDTO;
import org.alliancegenome.curation_api.model.ingest.dto.DataProviderDTO;
import org.alliancegenome.curation_api.model.ingest.dto.IngestDTO;
import org.zfin.infrastructure.ActiveData;
import org.zfin.mutant.Fish;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.zfin.repository.RepositoryFactory.getFishRepository;

public class FishLinkMLInfo extends LinkMLInfo {

    public FishLinkMLInfo(int number) {
        super(number);
    }


    public static void main(String[] args) throws IOException {
        int number = 0;
        if (args.length > 0) {
            number = Integer.valueOf(args[0]);
        }
        FishLinkMLInfo diseaseInfo = new FishLinkMLInfo(number);
        diseaseInfo.init();
        System.exit(0);
    }

    private void init() throws IOException {
        initAll();
        IngestDTO ingestDTO = getIngestDTO();
        List<AffectedGenomicModelDTO> allDiseaseDTO = getAllFish(numfOfRecords);
        ingestDTO.setAgmIngestSet(allDiseaseDTO);
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
        String jsonInString = writer.writeValueAsString(ingestDTO);
        try (PrintStream out = new PrintStream(new FileOutputStream("ZFIN_1.0.1.4_AGM_ml.json"))) {
            out.print(jsonInString);
        }
    }

    public List<AffectedGenomicModelDTO> getAllFish(int numberOrRecords) {
        List<Fish> allFish = getFishRepository().getAllFish(numberOrRecords);
        return allFish.stream()
                .map(fish -> {
                    AffectedGenomicModelDTO dto = new AffectedGenomicModelDTO();
                    dto.setName(fish.getName());
                    dto.setCreatedByCurie("ZFIN:CURATOR");
                    dto.setSubtypeName("fish");
                    dto.setTaxonCurie(ZfinDTO.taxonId);
                    dto.setCurie("ZFIN:" + fish.getZdbID());
                    GregorianCalendar date = ActiveData.getDateFromId(fish.getZdbID());
                    dto.setDateCreated(format(date));
                    org.alliancegenome.curation_api.model.ingest.dto.DataProviderDTO dataProvider = new DataProviderDTO();
                    dataProvider.setSourceOrganizationAbbreviation("ZFIN");
                    dto.setDataProviderDto(dataProvider);
                    return dto;
                })
                .collect(toList());
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

    public static String format(Date calendar) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        Calendar instance = Calendar.getInstance();
        instance.setTime(calendar);
        fmt.setCalendar(instance);
        return fmt.format(calendar.getTime());
    }

}
