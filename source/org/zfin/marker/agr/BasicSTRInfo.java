package org.zfin.marker.agr;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.commons.collections.CollectionUtils;
import org.zfin.marker.MarkerAlias;
import org.zfin.marker.SecondaryMarker;
import org.zfin.mutant.SequenceTargetingReagent;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.zfin.marker.Marker;

import java.util.stream.Collectors;

import static org.zfin.repository.RepositoryFactory.getMutantRepository;

public class BasicSTRInfo extends AbstractScriptWrapper {

    private int numfOfRecords = 0;

    public BasicSTRInfo(int number) {
        numfOfRecords = number;
    }

    public static void main(String[] args) throws IOException {
        int number = 0;
        if (args.length > 0) {
            number = Integer.valueOf(args[0]);
        }
        BasicSTRInfo basicSTRInfo = new BasicSTRInfo(number);
        basicSTRInfo.init();
        System.exit(0);
    }

    private void init() throws IOException {
        initAll();
        AllSTRDTO allSTRDTO = getAllSTRInfo();
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());

        String jsonInString = writer.writeValueAsString(allSTRDTO);
        try (PrintStream out = new PrintStream(new FileOutputStream("ZFIN_1.0.0.8_STR.json"))) {
            out.print(jsonInString);
        }
    }

    public AllSTRDTO getAllSTRInfo() {
        List<SequenceTargetingReagent> allSTRs = getMutantRepository().getAllSTRs();
        System.out.println(allSTRs.size());

        List<STRDTO> allSTRDTOList = allSTRs.stream()
                .map(
                        str -> {
                            STRDTO dto = new STRDTO();
                            dto.setPrimaryId(str.getZdbID());
                            dto.setName(str.getName());
                            //TODO: get target gene ids
                            if (CollectionUtils.isNotEmpty(str.getTargetGenes())) {
                                List<String> geneList = new ArrayList<>(str.getTargetGenes().size());
                                for (Marker gene : str.getTargetGenes()) {
                                    geneList.add("ZFIN:" + gene.getZdbID());
                                }
                                dto.setTargetGeneIds(geneList);
                            }
                            if (CollectionUtils.isNotEmpty(str.getAliases())) {
                                List<String> aliasList = new ArrayList<>(str.getAliases().size());
                                for (MarkerAlias alias : str.getAliases()) {
                                    aliasList.add(alias.getAlias());
                                }
                                dto.setSynonyms(aliasList);
                            }
                            if (CollectionUtils.isNotEmpty(str.getSecondaryMarkerSet())) {
                                Set<String> secondaryDTOs = new HashSet<>();
                                for (SecondaryMarker secMarker : str.getSecondaryMarkerSet()) {
                                    secondaryDTOs.add(secMarker.getOldID());
                                }
                                dto.setSecondaryIds(secondaryDTOs);
                            }
                            dto.setTaxonId(dto.getTaxonId());
                            List<String> pages = new ArrayList<>();
                            pages.add("STR");
                            CrossReferenceDTO xref = new CrossReferenceDTO("ZFIN", str.getZdbID(), pages);
                            dto.setCrossReference(xref);
                            return dto;
                        })
                .collect(Collectors.toList());

        AllSTRDTO allSTRDTO = new AllSTRDTO();
        allSTRDTO.setSTRs(allSTRDTOList);
        String dataProvider = "ZFIN";
        List<String> pages = new ArrayList<>();
        pages.add("homepage");
        MetaDataDTO meta = new MetaDataDTO(new DataProviderDTO("curated", new CrossReferenceDTO(dataProvider, dataProvider, pages)));
        allSTRDTO.setMetaData(meta);
        return allSTRDTO;
    }
}

