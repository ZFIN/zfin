package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.alliancegenome.curation_api.model.ingest.dto.ConstructDTO;
import org.alliancegenome.curation_api.model.ingest.dto.CrossReferenceDTO;
import org.alliancegenome.curation_api.model.ingest.dto.DataProviderDTO;
import org.alliancegenome.curation_api.model.ingest.dto.IngestDTO;
import org.alliancegenome.curation_api.model.ingest.dto.associations.AlleleConstructAssociationDTO;
import org.alliancegenome.curation_api.model.ingest.dto.associations.ConstructGenomicEntityAssociationDTO;
import org.alliancegenome.curation_api.model.ingest.dto.slotAnnotions.NameSlotAnnotationDTO;
import org.alliancegenome.curation_api.model.ingest.dto.slotAnnotions.ConstructComponentSlotAnnotationDTO;
import org.apache.commons.collections4.CollectionUtils;
import org.zfin.construct.ConstructComponent;
import org.zfin.gwt.root.util.StringUtils;
import org.zfin.infrastructure.ActiveData;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerAlias;
import org.zfin.marker.SecondaryMarker;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.util.stream.Collectors;

import static org.zfin.repository.RepositoryFactory.getConstructRepository;
import static org.zfin.repository.RepositoryFactory.getInfrastructureRepository;

public class ConstructLinkMLInfo extends LinkMLInfo {

    public ConstructLinkMLInfo(int number) {
        super(number);
    }


    public static void main(String[] args) throws IOException {
        int number = 0;
        mainParent(args);
        ConstructLinkMLInfo diseaseInfo = new ConstructLinkMLInfo(number);
        diseaseInfo.init();
        System.exit(0);
    }

    private void init() throws IOException {
        initAll();
        IngestDTO ingestDTO = getIngestDTO();
        List<org.alliancegenome.curation_api.model.ingest.dto.ConstructDTO> allDiseaseDTO = getAllConstructInfo();
        ingestDTO.setConstructIngestSet(allDiseaseDTO);
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
        String jsonInString = writer.writeValueAsString(ingestDTO);
        try (PrintStream out = new PrintStream(new FileOutputStream("ZFIN_Construct_ml.json"))) {
            out.print(jsonInString);
        }

        IngestDTO ingestDTOGenomicAssociations = getIngestDTO();
        ingestDTOGenomicAssociations.setConstructGenomicEntityAssociationIngestSet(genomicEntityAssociationDTOList);
        jsonInString = writer.writeValueAsString(ingestDTOGenomicAssociations);
        try (PrintStream out = new PrintStream(new FileOutputStream("ZFIN_Construct_Association_ml.json"))) {
            out.print(jsonInString);
        }

        IngestDTO ingestDTOAlleleConstructAssociations = getIngestDTO();
        ingestDTOAlleleConstructAssociations.setAlleleConstructAssociationIngestSet(alleleConstructAssociationDTOList);
        jsonInString = writer.writeValueAsString(ingestDTOAlleleConstructAssociations);
        try (PrintStream out = new PrintStream(new FileOutputStream("ZFIN_Allele_Construct_Association_ml.json"))) {
            out.print(jsonInString);
        }

    }

    private List<ConstructGenomicEntityAssociationDTO> genomicEntityAssociationDTOList = new ArrayList<>();
    private List<AlleleConstructAssociationDTO> alleleConstructAssociationDTOList = new ArrayList<>();

    public List<org.alliancegenome.curation_api.model.ingest.dto.ConstructDTO> getAllConstructInfo() {
        List<Marker> allConstructs = getConstructRepository().getAllConstructs();
        System.out.println(allConstructs.size());

        List<org.alliancegenome.curation_api.model.ingest.dto.ConstructDTO> allConstructDTOList = allConstructs.stream()
            .map(
                construct -> {

                    String primaryExternalId = "ZFIN:" + construct.getZdbID();

                    ConstructDTO dto = new ConstructDTO();
                    NameSlotAnnotationDTO name = new NameSlotAnnotationDTO();
                    name.setDisplayText(construct.getName());
                    name.setFormatText(construct.getName());
                    name.setNameTypeName("full_name");
                    dto.setConstructFullNameDto(name);

                    NameSlotAnnotationDTO symbol = new NameSlotAnnotationDTO();
                    symbol.setDisplayText(construct.getAbbreviation());
                    symbol.setFormatText(construct.getAbbreviation());
                    symbol.setNameTypeName("nomenclature_symbol");
                    dto.setConstructSymbolDto(symbol);

                    DataProviderDTO dataProvider = new DataProviderDTO();
                    dataProvider.setSourceOrganizationAbbreviation("ZFIN");
                    dto.setDataProviderDto(dataProvider);
                    CrossReferenceDTO crossReferenceDTO = new CrossReferenceDTO();
                    crossReferenceDTO.setDisplayName(construct.getZdbID());
                    crossReferenceDTO.setReferencedCurie(primaryExternalId);
                    crossReferenceDTO.setPageArea("construct");
                    crossReferenceDTO.setPrefix("ZFIN");
                    dataProvider.setCrossReferenceDto(crossReferenceDTO);
                    dto.setCreatedByCurie("ZFIN:CURATOR");
                    //dto.setTaxonCurie(ZfinDTO.taxonId);
                    dto.setPrimaryExternalId(primaryExternalId);
                    GregorianCalendar date = ActiveData.getDateFromId(construct.getZdbID());
                    dto.setDateCreated(format(date));
                    List<PublicationAttribution> attributions = getInfrastructureRepository().getPublicationAttributions(construct.zdbID);
                    List<String> referenceCuries = attributions.stream().map(publicationAttribution -> this.getSingleReference(publicationAttribution.getPublication())).toList();
                    dto.setReferenceCuries(referenceCuries);

                    List<ConstructComponent> components = getConstructRepository().getConstructComponentsByConstructZdbId(construct.getZdbID());
                    List<ConstructComponentSlotAnnotationDTO> componentDTOs = new ArrayList<>();

                    construct.getFeatureMarkerRelationships().forEach(featureMarkerRelationship -> {
                        AlleleConstructAssociationDTO alleleConstructAssociationDTO = new AlleleConstructAssociationDTO();
                        alleleConstructAssociationDTO.setConstructIdentifier("ZFIN:" + construct.zdbID);
                        alleleConstructAssociationDTO.setAlleleIdentifier("ZFIN:" + featureMarkerRelationship.getFeature().getZdbID());
                        alleleConstructAssociationDTO.setRelationName("contains");
                        alleleConstructAssociationDTOList.add(alleleConstructAssociationDTO);
                    });

                    if (CollectionUtils.isNotEmpty(components)) {
                        for (ConstructComponent component : components) {
                            String componentZdbID = component.getComponentZdbID();
                            if (componentZdbID != null) {
                                boolean isGeneOrRNAG = componentZdbID.startsWith("ZDB-GENE") || (componentZdbID.contains("RNAG"));
                                String relationName = populateRelationship(component);
                                if(StringUtils.isEmpty(relationName)){
                                    continue;
                                }
                                if (isGeneOrRNAG) {
                                    switch (component.getType()) {
                                        case CODING_SEQUENCE_OF, CODING_SEQUENCE_OF_, PROMOTER_OF, PROMOTER_OF_ -> {
                                            ConstructGenomicEntityAssociationDTO associationDTO = new ConstructGenomicEntityAssociationDTO();
                                            associationDTO.setConstructIdentifier("ZFIN:" + construct.zdbID);
                                            associationDTO.setGenomicEntityIdentifier("ZFIN:" + componentZdbID);
                                            associationDTO.setGenomicEntityRelationName(relationName);
                                            genomicEntityAssociationDTOList.add(associationDTO);
                                        }
                                        default -> {
                                        }
                                    }
                                } else if (!componentZdbID.startsWith("ZDB-CV")) {

                                    ConstructComponentSlotAnnotationDTO componentSlotAnnotationDTO = new ConstructComponentSlotAnnotationDTO();
                                    componentSlotAnnotationDTO.setComponentSymbol(component.getComponentValue());
                                    componentSlotAnnotationDTO.setRelationName(relationName);
                                    componentSlotAnnotationDTO.setTaxonCurie(ZfinDTO.taxonId);
                                    componentSlotAnnotationDTO.setTaxonText(ZfinDTO.taxonId);
                                    componentDTOs.add(componentSlotAnnotationDTO);
                                }
                            }
                        }
                    }
                    dto.setConstructComponentDtos(componentDTOs);
                    if (CollectionUtils.isNotEmpty(construct.getAliases())) {
                        List<String> aliasList = new ArrayList<>(construct.getAliases().size());
                        for (MarkerAlias alias : construct.getAliases()) {
                            aliasList.add(alias.getAlias());
                        }
                    }
                    if (CollectionUtils.isNotEmpty(construct.getSecondaryMarkerSet())) {
                        Set<String> secondaryDTOs = new HashSet<>();
                        for (SecondaryMarker secMarker : construct.getSecondaryMarkerSet()) {
                            secondaryDTOs.add(secMarker.getOldID());
                        }
                    }
                    return dto;
                })
            .collect(Collectors.toList());

        return allConstructDTOList;
    }

    private static String populateRelationship(ConstructComponent component) {
        String value = null;
        switch (component.getType()) {
            case CODING_SEQUENCE_OF, CODING_SEQUENCE_OF_ -> value = "expresses";
            case PROMOTER_OF, PROMOTER_OF_ -> value = "is_regulated_by";
            default -> {
            }
        }
        return value;
    }

}
