package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.alliancegenome.curation_api.model.ingest.dto.DataProviderDTO;
import org.alliancegenome.curation_api.model.ingest.dto.IngestDTO;
import org.alliancegenome.curation_api.model.ingest.dto.slotAnnotions.constructSlotAnnotations.ConstructComponentSlotAnnotationDTO;
import org.apache.commons.collections4.CollectionUtils;
import org.zfin.construct.ConstructComponent;
import org.zfin.infrastructure.ActiveData;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerAlias;
import org.zfin.marker.SecondaryMarker;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.util.stream.Collectors;

import static org.zfin.repository.RepositoryFactory.getConstructRepository;

public class ConstructLinkMLInfo extends LinkMLInfo {

    public ConstructLinkMLInfo(int number) {
        super(number);
    }


    public static void main(String[] args) throws IOException {
        int number = 0;
        if (args.length > 0) {
            number = Integer.parseInt(args[0]);
        }
        ConstructLinkMLInfo diseaseInfo = new ConstructLinkMLInfo(number);
        diseaseInfo.init();
        System.exit(0);
    }

    private void init() throws IOException {
        initAll();
        IngestDTO ingestDTO = getIngestDTO();
        List<org.alliancegenome.curation_api.model.ingest.dto.ConstructDTO> allDiseaseDTO = getAllConstructInfo();
        ingestDTO.setLinkMLVersion("v1.9.0");
        ingestDTO.setConstructIngestSet(allDiseaseDTO);
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
        String jsonInString = writer.writeValueAsString(ingestDTO);
        try (PrintStream out = new PrintStream(new FileOutputStream("ZFIN_Construct_ml.json"))) {
            out.print(jsonInString);
        }
    }

    public List<org.alliancegenome.curation_api.model.ingest.dto.ConstructDTO> getAllConstructInfo() {
        List<Marker> allConstructs = getConstructRepository().getAllConstructs();
        System.out.println(allConstructs.size());

        List<org.alliancegenome.curation_api.model.ingest.dto.ConstructDTO> allConstructDTOList = allConstructs.stream()
            .map(
                construct -> {

                    org.alliancegenome.curation_api.model.ingest.dto.ConstructDTO dto = new org.alliancegenome.curation_api.model.ingest.dto.ConstructDTO();
                    dto.setName(construct.getName());
                    org.alliancegenome.curation_api.model.ingest.dto.DataProviderDTO dataProvider = new DataProviderDTO();
                    dataProvider.setSourceOrganizationAbbreviation("ZFIN");
                    dto.setDataProviderDto(dataProvider);
                    dto.setCreatedByCurie("ZFIN:CURATOR");
                    dto.setTaxonCurie(ZfinDTO.taxonId);
                    dto.setModEntityId("ZFIN:" + construct.getZdbID());
                    GregorianCalendar date = ActiveData.getDateFromId(construct.getZdbID());
                    dto.setDateCreated(format(date));
                    dto.setReferenceCuries(construct.getPublications().stream().map(publicationAttribution -> this.getSingleReference(publicationAttribution.getPublication())).toList());

                    List<ConstructComponent> components = getConstructRepository().getConstructComponentsByConstructZdbId(construct.getZdbID());
                    List<ConstructComponentSlotAnnotationDTO> componentDTOs = new ArrayList<>();
                    if (CollectionUtils.isNotEmpty(components)) {
                        for (ConstructComponent component : components) {
                            ConstructComponentSlotAnnotationDTO componentSlotAnnotationDTO = new ConstructComponentSlotAnnotationDTO();
                            componentSlotAnnotationDTO.setComponentSymbol(component.getComponentValue());
/*
                            componentSlotAnnotationDTO.setTaxonCurie(ZfinDTO.taxonId);
                            componentSlotAnnotationDTO.setTaxonText(ZfinDTO.taxonId);
*/

                            ConstructComponentDTO componentDTO = new ConstructComponentDTO();
                            // currently only populate the component ZDB id when we've already submitted the gene in the BGI (ie: no EFGs, regions at this time)
                            if (component.getComponentZdbID() != null) {
                                if ((component.getComponentZdbID().startsWith("ZDB-GENE") || (component.getComponentZdbID().contains("RNAG"))))
                                    componentDTO.setComponentID("ZFIN:" + component.getComponentZdbID());
                            }
                            if (component.getType().equals(ConstructComponent.Type.CODING_SEQUENCE_OF) || component.getType().equals(ConstructComponent.Type.CODING_SEQUENCE_OF_)) {
                                componentDTO.setComponentRelation("expresses");
                            } else if (component.getType().equals(ConstructComponent.Type.PROMOTER_OF) || component.getType().equals(ConstructComponent.Type.PROMOTER_OF_)) {
                                componentDTO.setComponentRelation("is_regulated_by");
                            } else {
                                continue;
                            }
                            componentDTOs.add(componentSlotAnnotationDTO);
                        }
                    }
                    dto.setConstructComponentDtos(componentDTOs);
/*
                    List<ConstructComponent> components = getConstructRepository().getConstructComponentsByConstructZdbId(construct.getZdbID());
                    List<ConstructComponentDTO> componentDTOs = new ArrayList<>();
                    if (CollectionUtils.isNotEmpty(components)) {
                        for (ConstructComponent component : components) {
                            ConstructComponentDTO componentDTO = new ConstructComponentDTO();
                            // currently only populate the component ZDB id when we've already submitted the gene in the BGI (ie: no EFGs, regions at this time)
                            if (component.getComponentZdbID() != null) {
                                if ((component.getComponentZdbID().startsWith("ZDB-GENE")||(component.getComponentZdbID().contains("RNAG"))))
                                    componentDTO.setComponentID("ZFIN:" + component.getComponentZdbID());
                            }
                            if (component.getType().equals(ConstructComponent.Type.CODING_SEQUENCE_OF) || component.getType().equals(ConstructComponent.Type.CODING_SEQUENCE_OF_)) {
                                componentDTO.setComponentRelation("expresses");
                            } else if (component.getType().equals(ConstructComponent.Type.PROMOTER_OF) || component.getType().equals(ConstructComponent.Type.PROMOTER_OF_)) {
                                componentDTO.setComponentRelation("is_regulated_by");
                            } else {
                                continue;
                            }
                            componentDTO.setComponentSymbol(component.getComponentValue());
                            componentDTOs.add(componentDTO);
                        }
                    }
                    if (!componentDTOs.isEmpty()) {
                        dto.setConstructComponents(componentDTOs);
                    }
*/
                    if (CollectionUtils.isNotEmpty(construct.getAliases())) {
                        List<String> aliasList = new ArrayList<>(construct.getAliases().size());
                        for (MarkerAlias alias : construct.getAliases()) {
                            aliasList.add(alias.getAlias());
                        }
                        //dto.set(aliasList);
                    }
                    if (CollectionUtils.isNotEmpty(construct.getSecondaryMarkerSet())) {
                        Set<String> secondaryDTOs = new HashSet<>();
                        for (SecondaryMarker secMarker : construct.getSecondaryMarkerSet()) {
                            secondaryDTOs.add(secMarker.getOldID());
                        }
                        //dto.setSecondaryIds(secondaryDTOs);
                    }
/*
                    List<String> pages = new ArrayList<>();
                    pages.add("construct");
                    List<CrossReferenceDTO> xRefs = new ArrayList<>();
                    CrossReferenceDTO xref = new CrossReferenceDTO("ZFIN", construct.getZdbID(), pages);
                    xRefs.add(xref);
                    dto.setCrossReferences(xRefs);
*/
                    return dto;
                })
            .collect(Collectors.toList());

        return allConstructDTOList;
    }

}
