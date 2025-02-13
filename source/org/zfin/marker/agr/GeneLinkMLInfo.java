package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.alliancegenome.curation_api.model.ingest.dto.CrossReferenceDTO;
import org.alliancegenome.curation_api.model.ingest.dto.DataProviderDTO;
import org.alliancegenome.curation_api.model.ingest.dto.IngestDTO;
import org.alliancegenome.curation_api.model.ingest.dto.slotAnnotions.NameSlotAnnotationDTO;
import org.apache.commons.collections4.CollectionUtils;
import org.zfin.gwt.root.util.StringUtils;
import org.zfin.infrastructure.ActiveData;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerAlias;
import org.zfin.sequence.ForeignDB;
import org.zfin.sequence.MarkerDBLink;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static org.zfin.repository.RepositoryFactory.getExpressionRepository;
import static org.zfin.repository.RepositoryFactory.getMarkerRepository;

public class GeneLinkMLInfo extends LinkMLInfo {

    public GeneLinkMLInfo(int number) {
        super(number);
    }


    public static void main(String[] args) throws IOException {
        int number = 0;
        mainParent(args);
        GeneLinkMLInfo diseaseInfo = new GeneLinkMLInfo(number);
        diseaseInfo.init();
        System.exit(0);
    }

    private void init() throws IOException {
        initAll();
        IngestDTO ingestDTO = getIngestDTO();
        List<org.alliancegenome.curation_api.model.ingest.dto.GeneDTO> allDiseaseDTO = getAllGenes(numfOfRecords);
        ingestDTO.setGeneIngestSet(allDiseaseDTO);
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
        String jsonInString = writer.writeValueAsString(ingestDTO);
        try (PrintStream out = new PrintStream(new FileOutputStream("ZFIN_1.0.1.4_Gene_ml.json"))) {
            out.print(jsonInString);
        }
        allPrefixes.forEach(System.out::println);
    }

    public List<org.alliancegenome.curation_api.model.ingest.dto.GeneDTO> getAllGenes(int numberOrRecords) {
        List<Marker> genes = getMarkerRepository().getMarkerByGroup(Marker.TypeGroup.GENEDOM, numberOrRecords);
        return genes.stream()
            .map(marker -> {
                org.alliancegenome.curation_api.model.ingest.dto.GeneDTO dto = new org.alliancegenome.curation_api.model.ingest.dto.GeneDTO();
                dto.setGeneSymbolDto(getNameSlotAnnotationDTOAbbrev(marker.getAbbreviation()));
                dto.setGeneFullNameDto(getNameSlotAnnotationDTOName(marker.getAbbreviation()));
                dto.setGeneSynonymDtos(getNameSlotAnnotationDTOAliases(marker.getAliases()));
                dto.setCreatedByCurie("ZFIN:CURATOR");
                DataProviderDTO dataProvider = new DataProviderDTO();
                dataProvider.setSourceOrganizationAbbreviation("ZFIN");
                dto.setDataProviderDto(dataProvider);
                dto.setTaxonCurie(ZfinDTO.taxonId);
                dto.setPrimaryExternalId("ZFIN:" + marker.getZdbID());
                dto.setGeneTypeCurie(marker.getSoTerm().getOboID());
                GregorianCalendar date = ActiveData.getDateFromId(marker.getZdbID());
                dto.setDateCreated(format(date));

                List<CrossReferenceDTO> dbLinkList = new ArrayList<>(marker.getDbLinks().size() + 1);
                if (CollectionUtils.isNotEmpty(marker.getDbLinks())) {

                    for (MarkerDBLink link : marker.getDbLinks()) {
                        String dbName = DataProvider.getExternalDatabaseName(link.getReferenceDatabase().getForeignDB().getDbName());
                        if (dbName == null)
                            continue;
                        // do not include ENSDARP records
                        if (dbName.equals(ForeignDB.AvailableName.ENSEMBL.toString()) && link.getAccessionNumber().startsWith("ENSDARP"))
                            continue;

                        CrossReferenceDTO xRefDto = getCrossReferenceDTO(link, "default");
                        dbLinkList.add(xRefDto);
                    }
                }
                int hasExpression = getExpressionRepository().getExpressionFigureCountForGene(marker);
                if (hasExpression > 0) {
                    int hasWTExpression = getExpressionRepository().getWtExpressionFigureCountForGene(marker);
                    if (hasWTExpression > 0) {
                        dbLinkList.add(getCrossReferenceDTO(marker.getZdbID(), "gene/wild_type_expression", "ZFIN"));
                    }
                    dbLinkList.add(getCrossReferenceDTO(marker.getZdbID(), "gene/expression", "ZFIN"));
                    dbLinkList.add(getCrossReferenceDTO(marker.getZdbID(), "gene/expression_images", "ZFIN"));
                }
                dbLinkList.add(getCrossReferenceDTO(marker.getZdbID(), "gene", "ZFIN"));
                dbLinkList.add(getCrossReferenceDTO(marker.getZdbID(), "gene/references", "ZFIN"));
                dto.setCrossReferenceDtos(dbLinkList);
                return dto;
            })
            .collect(toList());
    }

    static Set<String> allPrefixes = new HashSet<>();
    static Map<String, String> prefixMap = new HashMap<>();

    static {
        prefixMap.put("ENSEMBL_GRCZ11_", "ENSEMBL");
        prefixMap.put("UNIPROTKB", "UniProtKB");
        prefixMap.put("GENE", "NCBI_Gene");
    }

    private static CrossReferenceDTO getCrossReferenceDTO(MarkerDBLink link, String pageArea) {
        CrossReferenceDTO xRefDto = new CrossReferenceDTO();
        String name = link.getReferenceDatabase().getForeignDB().getDbName().name();
        String prefix = prefixMap.get(name);
        if (prefix == null)
            prefix = name;
        if (prefix.equals("ZFIN"))
            pageArea = "gene";
        xRefDto.setPageArea(pageArea);
        String prefixedCurie = prefix + ":" + link.getAccessionNumber();
        xRefDto.setReferencedCurie(prefixedCurie);
        xRefDto.setDisplayName(prefixedCurie);
        allPrefixes.add(prefix);
        xRefDto.setPrefix(prefix);
        return xRefDto;
    }

    private static CrossReferenceDTO getCrossReferenceDTO(String curie, String pageArea, String prefix) {
        CrossReferenceDTO xRefDto = new CrossReferenceDTO();
        xRefDto.setReferencedCurie(curie);
        xRefDto.setDisplayName(curie);
        xRefDto.setPageArea(pageArea);
        xRefDto.setPrefix(prefix);
        allPrefixes.add(prefix);
        return xRefDto;
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

    public static List<NameSlotAnnotationDTO> getNameSlotAnnotationDTOAliases(Set<MarkerAlias> aliasSet) {
        if (CollectionUtils.isEmpty(aliasSet))
            return null;
        List<NameSlotAnnotationDTO> aliases = new ArrayList<>();
        aliasSet.stream()
            .filter(markerAlias -> StringUtils.isNotEmpty(markerAlias.getAlias())).forEach(markerAlias -> {
                NameSlotAnnotationDTO slotAnnotation = new NameSlotAnnotationDTO();
                slotAnnotation.setDisplayText(markerAlias.getAlias());
                slotAnnotation.setFormatText(markerAlias.getAlias());
                slotAnnotation.setNameTypeName("unspecified");
                aliases.add(slotAnnotation);
            });
        return aliases;
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
