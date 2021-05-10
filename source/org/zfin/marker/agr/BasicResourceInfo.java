package org.zfin.marker.agr;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.commons.collections.CollectionUtils;
import org.zfin.marker.Marker;
import org.zfin.infrastructure.SourceAlias;
import org.zfin.marker.SecondaryMarker;
import org.zfin.publication.Journal;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;
import org.zfin.publication.Publication;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.zfin.repository.RepositoryFactory.getPublicationRepository;


public class BasicResourceInfo extends AbstractScriptWrapper {

    private int numfOfRecords = 0;

    public BasicResourceInfo(int number) {
        numfOfRecords = number;
    }

    public static void main(String[] args) throws IOException {
        int number = 0;
        if (args.length > 0) {
            number = Integer.valueOf(args[0]);
        }
        BasicResourceInfo basicResourceInfo = new BasicResourceInfo(number);
        basicResourceInfo.init();
        System.exit(0);
    }

    private void init() throws IOException {
        initAll();
        AllResourceDTO allResourceDTO = getAllResourceInfo();
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());

        String jsonInString = writer.writeValueAsString(allResourceDTO);
        try (PrintStream out = new PrintStream(new FileOutputStream("ZFIN_1.0.1.4_Resource.json"))) {
            out.print(jsonInString);
        }
    }

    public AllResourceDTO getAllResourceInfo() {
        List<Journal> allResources = getPublicationRepository().getAllJournals();
        System.out.println(allResources.size());

        List<ResourceDTO> allResourceDTOList = allResources.stream()
                .map(
                        jrnl -> {
                            ResourceDTO dto = new ResourceDTO();
                            if (jrnl.getNlmID() != null) {
                                dto.setPrimaryId(jrnl.getNlmID());
                            }
                            else {
                                dto.setPrimaryId("ZFIN:" + jrnl.getZdbID());
                            }
                            System.out.println("ZFIN:"+jrnl.getZdbID());
                            dto.setTitle(jrnl.getName());
                            dto.setMedlineAbbreviation(jrnl.getMedAbbrev());
                            dto.setIsoAbbreviation(jrnl.getIsoAbbrev());
                            dto.setPublisher(jrnl.getPublisher());
                            dto.setPrintISSN(jrnl.getPrintIssn());
                            dto.setOnlineISSN(jrnl.getOnlineIssn());
                            dto.setPublisher(jrnl.getPublisher());

//                            if (CollectionUtils.isNotEmpty(jrnl.getAliases())) {
//                                List<String> aliasList = new ArrayList<>(jrnl.getAliases().size());
//                                for (SourceAlias alias : jrnl.getAliases()) {
//                                    aliasList.add(alias.getAlias());
//                                }
//                                dto.setAliases(aliasList);
//                            }
                            List<String> pages = new ArrayList<>();
                            pages.add("journal");
                            pages.add("journal/references");
                            List<CrossReferenceDTO> xRefs = new ArrayList<>();
                            CrossReferenceDTO xref = new CrossReferenceDTO("ZFIN", jrnl.getZdbID(), pages);
                            xRefs.add(xref);
                            dto.setCrossReferences(xRefs);

                            return dto;
                        })
                .collect(Collectors.toList());

        AllResourceDTO allResourceDTO = new AllResourceDTO();
        allResourceDTO.setJrnl(allResourceDTOList);
        String dataProvider = "ZFIN";
        List<String> pages = new ArrayList<>();
        pages.add("homepage");
        MetaDataDTO meta = new MetaDataDTO(new DataProviderDTO("curated", new CrossReferenceDTO(dataProvider, dataProvider, pages)));
        allResourceDTO.setMetaData(meta);

        return allResourceDTO;
    }
}

