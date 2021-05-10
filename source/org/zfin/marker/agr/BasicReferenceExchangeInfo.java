package org.zfin.marker.agr;


import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.zfin.publication.*;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

import java.util.List;
import java.util.stream.Collectors;

import static org.zfin.repository.RepositoryFactory.getPublicationRepository;


public class BasicReferenceExchangeInfo extends AbstractScriptWrapper {

    private int numfOfRecords = 0;

    public BasicReferenceExchangeInfo(int number) {
        numfOfRecords = number;
    }

    public static void main(String[] args) throws IOException {
        int number = 0;
        if (args.length > 0) {
            number = Integer.valueOf(args[0]);
        }
        BasicReferenceExchangeInfo basicReferenceExchangeInfo = new BasicReferenceExchangeInfo(number);
        basicReferenceExchangeInfo.init();
        System.exit(0);
    }

    private void init() throws IOException {
        initAll();
        AllReferenceExchangeDTO allReferenceExchangeDTO = getAllReferenceExchangeInfo();
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());

        String jsonInString = writer.writeValueAsString(allReferenceExchangeDTO);
        try (PrintStream out = new PrintStream(new FileOutputStream("ZFIN_1.0.1.4_ReferenceExchange.json"))) {
            out.print(jsonInString);
        }
    }

    public AllReferenceExchangeDTO getAllReferenceExchangeInfo() {
        List<Publication> allReferences = getPublicationRepository().getAllPublications();
        System.out.println(allReferences.size());

        List<ReferenceExchangeDTO> allReferenceExchangeDTOList = allReferences.stream()
                .map(
                        reference -> {
                            ReferenceExchangeDTO dto = new ReferenceExchangeDTO();
                            dto.setModId("ZFIN:"+reference.getZdbID());
                            dto.setPubMedId("PMID:"+reference.getAccessionNumber());
                            List<MODReferenceTypeDTO> MODReferenceTypes = new ArrayList<>();
                            MODReferenceTypeDTO pubType = new MODReferenceTypeDTO();
                            pubType.setSource("ZFIN");
                            pubType.setReferenceType(reference.getType().getDisplay());
                            MODReferenceTypes.add(pubType);
                            dto.setMODReferenceTypes(MODReferenceTypes);
                            String allianceCategory = "";
                            String type = reference.getType().getDisplay();
                            if (type.equals("Journal") || type.equals("Abstract")){
                                allianceCategory = "Research Article";
                            }
                            else if (type.equals("Unpublished") ||
                                    type.equals("Curation") ||
                                    type.equals("Active Curation") ){
                                allianceCategory = "Internal Process Reference";
                            }
                            else if (type.equals("Unknown")){
                                allianceCategory = type;
                            }
                            else if (type.equals("Review")){
                                allianceCategory = "Review Article";
                            }
                            else if (type.equals("Book") || type.equals("Chapter")){
                                allianceCategory = "Book";
                            }
                            else if (type.equals("Thesis")){
                                allianceCategory = type;
                            }
                            else {
                                allianceCategory = "Other";
                            }
                            dto.setAllianceCategory(allianceCategory);
                            List<ReferenceTagDTO> tags = new ArrayList<>();
                            ReferenceTagDTO tag = new ReferenceTagDTO();
                            if (reference.getAccessionNumber() != null){
                                dto.setPubMedId("PMID:"+reference.getAccessionNumber());
                                if (reference.isCanShowImages()) {
                                    tag.setReferenceId("PMID:"+reference.getAccessionNumber());
                                    tag.setTagSource("ZFIN");
                                    tag.setTagName("canShowImages");
                                    tags.add(tag);
                                }
                                ReferenceTagDTO incorpusTag = new ReferenceTagDTO();
                                incorpusTag.setTagName("inCorpus");
                                incorpusTag.setReferenceId("PMID:"+reference.getAccessionNumber());
                                incorpusTag.setTagSource("ZFIN");
                                tags.add(incorpusTag);
                            }
                            dto.setTags(tags);


                            return dto;

                        })
                .collect(Collectors.toList());

        AllReferenceExchangeDTO allReferenceExchangeDTO = new AllReferenceExchangeDTO();
        allReferenceExchangeDTO.setData(allReferenceExchangeDTOList);
        String dataProvider = "ZFIN";
        List<String> pages = new ArrayList<>();
        pages.add("homepage");
        MetaDataDTO meta = new MetaDataDTO(new DataProviderDTO("curated", new CrossReferenceDTO(dataProvider, dataProvider, pages)));
        allReferenceExchangeDTO.setMetaData(meta);

        return allReferenceExchangeDTO;
    }
}