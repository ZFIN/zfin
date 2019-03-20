package org.zfin.marker.agr;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.commons.collections.CollectionUtils;
import org.zfin.marker.MarkerAlias;
import org.zfin.mutant.Fish;
import org.zfin.mutant.GenotypeFeature;
import org.zfin.mutant.SequenceTargetingReagent;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.zfin.repository.RepositoryFactory.getMutantRepository;

public class BasicFishInfo extends AbstractScriptWrapper {


    private int numfOfRecords = 0;

    public BasicFishInfo(int number) {
        numfOfRecords = number;
    }

    public static void main(String[] args) throws IOException {
        int number = 0;
        if (args.length > 0) {
            number = Integer.valueOf(args[0]);
        }
        BasicFishInfo basicFishInfo = new BasicFishInfo(number);
        basicFishInfo.init();
        System.exit(0);
    }

    private void init() throws IOException {
        initAll();
        AllFishDTO allFishDTO = getAllFishInfo();
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());

        String jsonInString = writer.writeValueAsString(allFishDTO);
        try (PrintStream out = new PrintStream(new FileOutputStream("ZFIN_1.0.0.8_Genotype.json"))) {
            out.print(jsonInString);
        }
    }

    public AllFishDTO getAllFishInfo() {
        List<Fish> allFishes = getMutantRepository().getAllFish();
        System.out.println(allFishes.size());

        List<FishDTO> allFishDTOList = allFishes.stream()
                .map(
                        fish -> {
                            FishDTO dto = new FishDTO();
                            dto.setName(fish.getName());
                            dto.setGenotypeID("ZFIN:" + fish.getZdbID());
                            if (CollectionUtils.isNotEmpty(fish.getStrList())) {
                                List<String> strList = new ArrayList<>(fish.getStrList().size());
                                for (SequenceTargetingReagent str : fish.getStrList()) {
                                    strList.add("ZFIN:" + str.getZdbID());
                                }
                                dto.setSequenceTargetingReagents(strList);
                            }
                            if (CollectionUtils.isNotEmpty(fish.getGenotype().getGenotypeFeatures())) {
                                List<String> genoComponents = new ArrayList<>(fish.getGenotype().getGenotypeFeatures().size());
                                for (GenotypeFeature genofeat : fish.getGenotype().getGenotypeFeatures()) {
                                    genoComponents.add("ZFIN:" + genofeat.getFeature().getZdbID());
                                }
                                dto.setSequenceTargetingReagents(genoComponents);
                            }
                            //dto.setSequenceTargetingReagents(fish.getStrList());
                            //dto.setBackgrounds(fish.getGenotype().getAssociatedGenotypes());
                            List<String> pages = new ArrayList<>();
                            pages.add("Fish");
                            CrossReferenceDTO xref = new CrossReferenceDTO("ZFIN", fish.getZdbID(), pages);
                            dto.setCrossReference(xref);

                            return dto;
                        })
                .collect(Collectors.toList());

        AllFishDTO allFishDTO = new AllFishDTO();
        allFishDTO.setFishes(allFishDTOList);
        String dataProvider = "ZFIN";
        List<String> pages = new ArrayList<>();
        pages.add("homepage");
        MetaDataDTO meta = new MetaDataDTO(new DataProviderDTO("curated", new CrossReferenceDTO(dataProvider, dataProvider, pages)));
        allFishDTO.setMetaData(meta);
        return allFishDTO;
    }
}
