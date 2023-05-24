package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.alliancegenome.curation_api.model.ingest.dto.DataProviderDTO;
import org.alliancegenome.curation_api.model.ingest.dto.IngestDTO;
import org.alliancegenome.curation_api.model.ingest.dto.NameSlotAnnotationDTO;
import org.zfin.infrastructure.ActiveData;
import org.zfin.marker.Marker;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static org.zfin.repository.RepositoryFactory.getMarkerRepository;

public class GeneLinkMLInfo extends AbstractScriptWrapper {

    private int numfOfRecords = 0;

    public GeneLinkMLInfo(int number) {
        numfOfRecords = number;
    }


    public static void main(String[] args) throws IOException {
        int number = 0;
        if (args.length > 0) {
            number = Integer.valueOf(args[0]);
        }
        GeneLinkMLInfo diseaseInfo = new GeneLinkMLInfo(number);
        diseaseInfo.init();
        System.exit(0);
    }

    private void init() throws IOException {
        initAll();
        IngestDTO ingestDTO = new IngestDTO();
        List<org.alliancegenome.curation_api.model.ingest.dto.GeneDTO> allDiseaseDTO = getAllGenes(numfOfRecords);
        ingestDTO.setGeneIngestSet(allDiseaseDTO);
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
        String jsonInString = writer.writeValueAsString(ingestDTO);
        try (PrintStream out = new PrintStream(new FileOutputStream("ZFIN_1.0.1.4_Gene_ml.json"))) {
            out.print(jsonInString);
        }
    }

    public List<org.alliancegenome.curation_api.model.ingest.dto.GeneDTO> getAllGenes(int numberOrRecords) {
        List<Marker> genes = getMarkerRepository().getMarkerByGroup(Marker.TypeGroup.GENEDOM, numberOrRecords);
        return genes.stream()
            .map(marker -> {
                org.alliancegenome.curation_api.model.ingest.dto.GeneDTO dto = new org.alliancegenome.curation_api.model.ingest.dto.GeneDTO();
                dto.setGeneSymbolDto(getNameSlotAnnotationDTOAbbrev(marker.getAbbreviation()));
                dto.setGeneFullNameDto(getNameSlotAnnotationDTOName(marker.getAbbreviation()));
                dto.setCreatedByCurie("ZFIN:CURATOR");
                DataProviderDTO dataProvider = new DataProviderDTO();
                dataProvider.setSourceOrganizationAbbreviation("ZFIN");
                dto.setDataProviderDto(dataProvider);
                dto.setTaxonCurie(ZfinDTO.taxonId);
                dto.setCurie("ZFIN:" + marker.getZdbID());
                GregorianCalendar date = ActiveData.getDateFromId(marker.getZdbID());
                dto.setDateCreated(format(date));
                return dto;
            })
            .collect(toList());
    }

    public static List<NameSlotAnnotationDTO> getSynonymSlotAnnotationDTOs(List<String> names) {
        return names.stream().map(name -> {
            NameSlotAnnotationDTO slotAnnotation = new NameSlotAnnotationDTO();
            slotAnnotation.setDisplayText(name);
            slotAnnotation.setFormatText(name);
            slotAnnotation.setNameTypeName("unspecified");
            return slotAnnotation;
        }).toList();
    }

    public static NameSlotAnnotationDTO getNameSlotAnnotationDTOName(String name) {
        NameSlotAnnotationDTO slotAnnotation = new NameSlotAnnotationDTO();
        slotAnnotation.setDisplayText(name);
        slotAnnotation.setFormatText(name);
        slotAnnotation.setNameTypeName("full_name");
        return slotAnnotation;
    }

    public static NameSlotAnnotationDTO getNameSlotAnnotationDTOAbbrev(String abbreviation) {
        NameSlotAnnotationDTO slotAnnotation = new NameSlotAnnotationDTO();
        slotAnnotation.setDisplayText(abbreviation);
        slotAnnotation.setFormatText(abbreviation);
        slotAnnotation.setNameTypeName("nomenclature_symbol");
        return slotAnnotation;
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
