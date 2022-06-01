package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.alliancegenome.curation_api.model.ingest.dto.AlleleDTO;
import org.alliancegenome.curation_api.model.ingest.dto.IngestDTO;
import org.zfin.feature.Feature;
import org.zfin.infrastructure.ActiveData;
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
import static org.zfin.repository.RepositoryFactory.getFeatureRepository;

public class AlleleLinkMLInfo extends AbstractScriptWrapper {

    private int numfOfRecords = 0;

    public AlleleLinkMLInfo(int number) {
        numfOfRecords = number;
    }


    public static void main(String[] args) throws IOException {
        int number = 0;
        if (args.length > 0) {
            number = Integer.valueOf(args[0]);
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
                    dto.setInternal(true);
                    dto.setCreatedBy("ZFIN:CURATOR");
                    dto.setTaxon(ZfinDTO.taxonId);
                    dto.setCurie(feature.getZdbID());
                    if (feature.getFtrEntryDate() != null) {
                        dto.setDateCreated(format(feature.getFtrEntryDate()));
                    } else {
                        GregorianCalendar date = ActiveData.getDateFromId(feature.getZdbID());
                        dto.setDateCreated(format(date));
                    }
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
