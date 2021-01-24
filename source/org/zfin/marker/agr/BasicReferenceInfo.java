
package org.zfin.marker.agr;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.commons.collections.CollectionUtils;
import org.zfin.publication.MeshTerm;
import org.zfin.publication.Publication;
import org.zfin.publication.MeshHeading;
import org.zfin.publication.MeshHeadingTerm;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

import java.util.List;
import java.util.stream.Collectors;

import static org.zfin.repository.RepositoryFactory.getPublicationRepository;

public class BasicReferenceInfo extends AbstractScriptWrapper {

    private int numfOfRecords = 0;

    public BasicReferenceInfo(int number) {
        numfOfRecords = number;
    }

    public static void main(String[] args) throws IOException {
        int number = 0;
        if (args.length > 0) {
            number = Integer.valueOf(args[0]);
        }
        BasicReferenceInfo basicReferenceInfo = new BasicReferenceInfo(number);
        basicReferenceInfo.init();
        System.exit(0);
    }

    private void init() throws IOException {
        initAll();
        AllReferenceDTO allReferenceDTO = getAllReferenceInfo();
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());

        String jsonInString = writer.writeValueAsString(allReferenceDTO);
        try (PrintStream out = new PrintStream(new FileOutputStream("ZFIN_1.0.1.1_Reference.json"))) {
            out.print(jsonInString);
        }
    }

    public AllReferenceDTO getAllReferenceInfo() {
        List<Publication> allReferences = getPublicationRepository().getAllPublications();
        System.out.println(allReferences.size());

        List<ReferenceDTO> allReferenceDTOList = allReferences.stream()
                .map(
                        reference -> {
                            ReferenceDTO dto = new ReferenceDTO();
                            dto.setPrimaryId(reference.getZdbID());
                            dto.setTitle(reference.getTitle());
                            if (CollectionUtils.isNotEmpty(reference.getMeshHeadings())) {
                                List<MESHDetailDTO> meshDetails = new ArrayList<>();
                                for (MeshHeading meshHeading: reference.getMeshHeadings()) {
                                   MESHDetailDTO meshDetail = new MESHDetailDTO();
                                   for (MeshHeadingTerm mtqualifer : meshHeading.getQualifiers()) {
                                       meshDetail.setMeshHeadingTerm(meshHeading.getDescriptor().getTerm().getId());
                                       meshDetail.setMeshQualifierTerm(mtqualifer.getTerm().getId());
                                       meshDetails.add(meshDetail);
                                   }
                                }
                                dto.setMeshTerms(meshDetails);
                            }
                            dto.setAbstractText(reference.getAbstractText());
                            dto.setCitation(reference.getCitation());
                            dto.setDatePublished(reference.getPublicationDate());
                            dto.setDateArrivedInPubMed(reference.getEntryDate());
                            List<String> keywords = new ArrayList<>();
                            keywords.add(reference.getKeywords());
                            dto.setKeywords(keywords);
                            dto.setPages(reference.getPages());
                            dto.setVolume(reference.getVolume());
                            dto.setResourceAbbreviation(reference.getJournal().getAbbreviation());
                            List<AuthorReferenceDTO> authorReferences = new ArrayList<>();
                            

                            return dto;
                        })
                .collect(Collectors.toList());

        AllReferenceDTO allReferenceDTO = new AllReferenceDTO();
        allReferenceDTO.setReferences(allReferenceDTOList);
        String dataProvider = "ZFIN";
        List<String> pages = new ArrayList<>();
        pages.add("homepage");
        MetaDataDTO meta = new MetaDataDTO(new DataProviderDTO("curated", new CrossReferenceDTO(dataProvider, dataProvider, pages)));
        allReferenceDTO.setMetaData(meta);

        return allReferenceDTO;
    }
}
