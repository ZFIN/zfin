package org.zfin.marker.agr;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.zfin.mapping.FeatureGenomeLocation;
import org.zfin.feature.FeatureGenomicMutationDetail;
import org.zfin.mapping.FeatureLocation;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.zfin.repository.RepositoryFactory.getFeatureRepository;

public class BasicVariantInfo extends AbstractScriptWrapper {

    private int numfOfRecords = 0;

    public BasicVariantInfo(int number) {
        numfOfRecords = number;
    }

    public static void main(String[] args) throws IOException {
        int number = 0;
        if (args.length > 0) {
            number = Integer.valueOf(args[0]);
        }
        BasicVariantInfo basicVariantInfo = new BasicVariantInfo(number);
        basicVariantInfo.init();
        System.exit(0);
    }

    private void init() throws IOException {
        initAll();
        AllVariantDTO allVariantDTO = getAllVariantInfo();
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());

        String jsonInString = writer.writeValueAsString(allVariantDTO);
        try (PrintStream out = new PrintStream(new FileOutputStream("ZFIN_1.0.0.8_variant.json"))) {
            out.print(jsonInString);
        }
    }

    public AllVariantDTO getAllVariantInfo() {
        List<FeatureLocation> allVariants = getFeatureRepository().getAllFeatureLocationsOnGRCz11();
        System.out.println(allVariants.size());

        List<VariantDTO> allVariantDTOList = allVariants.stream()
                .map(
                        variant -> {
                            VariantDTO dto = new VariantDTO();
                            String featureType = variant.getFeature().getType().toString();
                            if (featureType.equals("POINT_MUTATION") || featureType.equals("INSERTION") || featureType.equals("DELETION")) {
                                if (featureType == "POINT_MUTATION") {
                                    dto.setType("SO:1000008");
                                } else if (featureType == "DELETION") {
                                    dto.setType("SO:0000159");
                                } else if (featureType == "INSERTION") {
                                    dto.setType("SO:0000667");
                                } else {
                                    System.out.println("invalid feature type");
                                }
//                                FeatureGenomicMutationDetail fgmd = getFeatureRepository().getFeatureGenomicDetail(variant.getFeature());
//                                dto.setGenomicReferenceSequence(fgmd.getFgmdSeqRef());
//                                dto.setGenomicVariantSequence(fgmd);
//                                dto.setAlleleId("ZFIN:" + variant.getFeature().getZdbID());
//                                dto.setAssembly(variant.getSfclAssembly());
//                                dto.setStart(variant.getSfclStart());
//                                dto.setEnd(variant.getSfclEnd());
//                                dto.setChromosome(variant.getSfclChromosome());
                            }
                            return dto;


                        })
                .collect(Collectors.toList());

        AllVariantDTO allVariantDTO = new AllVariantDTO();
        allVariantDTO.setVariants(allVariantDTOList);
        String dataProvider = "ZFIN";
        List<String> pages = new ArrayList<>();
        pages.add("homepage");
        MetaDataDTO meta = new MetaDataDTO(new DataProviderDTO("curated", new CrossReferenceDTO(dataProvider, dataProvider, pages)));
        allVariantDTO.setMetaData(meta);

        return allVariantDTO;

    }
}
