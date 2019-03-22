package org.zfin.marker.agr;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.zfin.feature.FeatureDnaMutationDetail;
import org.zfin.mutant.SequenceTargetingReagent;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static org.zfin.repository.RepositoryFactory.getFeatureRepository;
import static org.zfin.repository.RepositoryFactory.getMutantRepository;

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
        try (PrintStream out = new PrintStream(new FileOutputStream("ZFIN_1.0.0.8_STR.json"))) {
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
                            dto.setAlleleId(variant.getFeature().getZdbID());
                            dto.setAssembly(variant.getAssembly());
                            dto.setStart(variant.getStart());
                            dto.setEnd(variant.getEnd());
                            dto.setChromosome(variant.getChromosome());
                            String featureType = variant.getFeature().getType();
                            if (featureType ))
                            dto.setType(variant.getFeature().get)
                            List<String> pages = new ArrayList<>();
                            pages.add("Feature");
                            CrossReferenceDTO xref = new CrossReferenceDTO("ZFIN", variant.getZdbID(), pages);
                            dto.setCrossReference(xref);
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
