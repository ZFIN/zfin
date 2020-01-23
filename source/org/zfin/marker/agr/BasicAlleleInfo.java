package org.zfin.marker.agr;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.commons.collections.CollectionUtils;
import org.zfin.feature.Feature;
import org.zfin.feature.FeatureAlias;
import org.zfin.feature.SecondaryFeature;
import org.zfin.marker.Marker;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.zfin.repository.RepositoryFactory.getFeatureRepository;

public class BasicAlleleInfo extends AbstractScriptWrapper {

    private int numfOfRecords = 0;

    public BasicAlleleInfo(int number) {
        numfOfRecords = number;
    }


    public static void main(String[] args) throws IOException {
        int number = 0;
        if (args.length > 0) {
            number = Integer.valueOf(args[0]);
        }
        BasicAlleleInfo basicAlleleInfo = new BasicAlleleInfo(number);
        basicAlleleInfo.init();
        System.exit(0);
    }

    private void init() throws IOException {
        initAll();
        AllAlleleDTO allAlleleDTO = getAllAlleleInfo();
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());

//Object to JSON in String
        String jsonInString = writer.writeValueAsString(allAlleleDTO);
        try (PrintStream out = new PrintStream(new FileOutputStream("ZFIN_1.0.0.9_allele.json"))) {
            out.print(jsonInString);
        }
    }

    public AllAlleleDTO getAllAlleleInfo() {
        List<Feature> allAlleles = getFeatureRepository().getSingleAffectedGeneAlleles();
        System.out.println(allAlleles.size());

        List<AlleleDTO> allAlleleDTOList = allAlleles.stream()
                .map(
                        feature -> {
                            AlleleDTO dto = new AlleleDTO();
                            dto.setSymbol(feature.getName());
                            dto.setSymbolText(feature.getName());
                            dto.setPrimaryId(feature.getZdbID());
                            Marker gene = feature.getAllelicGene();
                            Marker construct = getFeatureRepository().getSingleConstruct(feature.getZdbID());
                            if (construct != null) {
                                dto.setConstruct("ZFIN:"+construct.getZdbID());
                            }
                            if (gene != null) {
                                dto.setGene(gene.getZdbID());
                            }
                            if (CollectionUtils.isNotEmpty(feature.getAliases())) {
                                List<String> aliasList = new ArrayList<>(feature.getAliases().size());
                                for (FeatureAlias alias : feature.getAliases()) {
                                    aliasList.add(alias.getAlias());
                                }
                                dto.setSynonyms(aliasList);
                            }
                            if (CollectionUtils.isNotEmpty(feature.getSecondaryFeatureSet())) {
                                Set<String> secondaryDTOs = new HashSet<>();
                                for (SecondaryFeature secAllele : feature.getSecondaryFeatureSet()) {
                                    secondaryDTOs.add(secAllele.getOldID());
                                }
                                dto.setSecondaryIds(secondaryDTOs);
                            }
                            List<String> pages = new ArrayList<>();
                            pages.add("allele");
                            List<CrossReferenceDTO> xRefs = new ArrayList<>();
                            CrossReferenceDTO xref = new CrossReferenceDTO("ZFIN", feature.getZdbID(), pages);
                            xRefs.add(xref);
                            dto.setCrossReferences(xRefs);

                            return dto;
                        })
                .collect(Collectors.toList());

        AllAlleleDTO allAlleleDTO = new AllAlleleDTO();
        allAlleleDTO.setAlleles(allAlleleDTOList);
        String dataProvider = "ZFIN";
        List<String> pages = new ArrayList<>();
        pages.add("homepage");
        MetaDataDTO meta = new MetaDataDTO(new DataProviderDTO("curated", new CrossReferenceDTO(dataProvider, dataProvider, pages)));
        allAlleleDTO.setMetaData(meta);
        return allAlleleDTO;
    }
}

