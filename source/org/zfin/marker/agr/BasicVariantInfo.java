package org.zfin.marker.agr;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.zfin.mapping.FeatureGenomeLocation;
import org.zfin.feature.Feature;
import org.zfin.feature.FeatureGenomicMutationDetail;
import org.zfin.mapping.FeatureLocation;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;
import org.zfin.sequence.ForeignDB;
import org.zfin.sequence.MarkerDBLink;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Objects;

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
        List<FeatureGenomicMutationDetail> allVariants = getFeatureRepository().getAllFeatureGenomicMutationDetails();

        System.out.println(allVariants.size());

        List<VariantDTO> allVariantDTOList = allVariants.stream()
                .map(
                        variant -> {
                                VariantDTO dto = new VariantDTO();
                                Feature feature = variant.getFeature();
                                FeatureLocation ftrLoc = getFeatureRepository().getAllFeatureLocationsOnGRCz11(feature);

                                if (ftrLoc != null) {
                                    String featureType = variant.getFeature().getType().toString();

                                    if (featureType.equals("POINT_MUTATION") || featureType.equals("INSERTION") || featureType.equals("DELETION")) {
                                        if (featureType == "POINT_MUTATION") {
                                            dto.setType("SO:1000008");
                                            dto.setGenomicReferenceSequence(variant.getFgmdSeqRef());
                                            dto.setGenomicVariantSequence(variant.getFgmdSeqVar());
                                        } else if (featureType == "DELETION") {
                                            dto.setType("SO:0000159");
                                            dto.setGenomicVariantSequence("N/A");
                                            dto.setGenomicReferenceSequence(variant.getFgmdSeqRef());
                                        } else if (featureType == "INSERTION") {
                                            dto.setType("SO:0000667");
                                            dto.setGenomicReferenceSequence("N/A");
                                            dto.setGenomicVariantSequence(variant.getFgmdSeqVar());
                                        } else {
                                            System.out.println("invalid feature type");
                                        }
                                        dto.setSequenceOfReferenceAccessionNumber(ftrLoc.getReferenceSequenceAccessionNumber());
                                        dto.setAlleleId("ZFIN:" + feature.getZdbID());
                                        dto.setAssembly(ftrLoc.getSfclAssembly());
                                        dto.setStart(ftrLoc.getSfclStart());
                                        dto.setEnd(ftrLoc.getSfclEnd());
                                        dto.setChromosome(ftrLoc.getSfclChromosome());
                                    }
                                }
                            return dto;
                                //TODO: filter out empty maps

                        }
                        )

        .collect(Collectors.toList());
        List<VariantDTO> allVariantDTOListNoNulls = new ArrayList<>();

        for (VariantDTO vDto : allVariantDTOList) {
            if (!(vDto == null)) {
                if (!(vDto.getAlleleId() == null)) {
                    allVariantDTOListNoNulls.add(vDto);
                    //System.out.println(vDto.getAlleleId());
                }
            }
        }
	System.out.println(allVariantDTOListNoNulls.size());
        AllVariantDTO allVariantDTO = new AllVariantDTO();
        allVariantDTO.setVariants(allVariantDTOListNoNulls);
        String dataProvider = "ZFIN";
        List<String> pages = new ArrayList<>();
        pages.add("homepage");
        MetaDataDTO meta = new MetaDataDTO(new DataProviderDTO("curated", new CrossReferenceDTO(dataProvider, dataProvider, pages)));
        allVariantDTO.setMetaData(meta);

        return allVariantDTO;

    }
}
