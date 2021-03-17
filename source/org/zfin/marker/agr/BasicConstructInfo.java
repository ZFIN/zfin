package org.zfin.marker.agr;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.commons.collections.CollectionUtils;
import org.zfin.construct.ConstructComponent;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerAlias;
import org.zfin.marker.SecondaryMarker;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.zfin.repository.RepositoryFactory.getConstructRepository;

public class BasicConstructInfo extends AbstractScriptWrapper {

    private int numfOfRecords = 0;


    public BasicConstructInfo(int number) {
        numfOfRecords = number;
    }

    public static void main(String[] args) throws IOException {
        int number = 0;
        if (args.length > 0) {
            number = Integer.valueOf(args[0]);
        }
        BasicConstructInfo basicConstructInfo = new BasicConstructInfo(number);
        basicConstructInfo.init();
        System.exit(0);
    }

    private void init() throws IOException {
        initAll();
        AllConstructDTO allConstructInfo = getAllConstructInfo();
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());

        String jsonInString = writer.writeValueAsString(allConstructInfo);
        try (PrintStream out = new PrintStream(new FileOutputStream("ZFIN_1.0.1.4_Construct.json"))) {
            out.print(jsonInString);
        }
    }

    public AllConstructDTO getAllConstructInfo() {
        List<Marker> allConstructs = getConstructRepository().getAllConstructs();
        System.out.println(allConstructs.size());

        List<ConstructDTO> allConstructDTOList = allConstructs.stream()
                .map(
                        construct -> {
                            ConstructDTO dto = new ConstructDTO();
                            dto.setPrimaryId(construct.getZdbID());
                            dto.setName(construct.getName());
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
                            if (CollectionUtils.isNotEmpty(construct.getAliases())) {
                                List<String> aliasList = new ArrayList<>(construct.getAliases().size());
                                for (MarkerAlias alias : construct.getAliases()) {
                                    aliasList.add(alias.getAlias());
                                }
                                dto.setSynonyms(aliasList);
                            }
                            if (CollectionUtils.isNotEmpty(construct.getSecondaryMarkerSet())) {
                                Set<String> secondaryDTOs = new HashSet<>();
                                for (SecondaryMarker secMarker : construct.getSecondaryMarkerSet()) {
                                    secondaryDTOs.add(secMarker.getOldID());
                                }
                                dto.setSecondaryIds(secondaryDTOs);
                            }
                            List<String> pages = new ArrayList<>();
                            pages.add("construct");
                            List<CrossReferenceDTO> xRefs = new ArrayList<>();
                            CrossReferenceDTO xref = new CrossReferenceDTO("ZFIN", construct.getZdbID(), pages);
                            xRefs.add(xref);
                            dto.setCrossReferences(xRefs);
                            return dto;
                        })
                .collect(Collectors.toList());

        AllConstructDTO allConstructDTO = new AllConstructDTO();
        allConstructDTO.setConstructs(allConstructDTOList);
        String dataProvider = "ZFIN";
        List<String> pages = new ArrayList<>();
        pages.add("homepage");
        MetaDataDTO meta = new MetaDataDTO(new DataProviderDTO("curated", new CrossReferenceDTO(dataProvider, dataProvider, pages)));
        allConstructDTO.setMetaData(meta);

        return allConstructDTO;
    }
}
