
package org.zfin.marker.agr;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.commons.collections.CollectionUtils;
import org.zfin.publication.*;
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
        try (PrintStream out = new PrintStream(new FileOutputStream("ZFIN_1.0.1.4_Reference.json"))) {
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
                            if (CollectionUtils.isNotEmpty(reference.getAuthorPubs())){
                                for (PubmedPublicationAuthor authorPub: reference.getAuthorPubs()){
                                    AuthorReferenceDTO authorRef = new AuthorReferenceDTO();
                                    authorRef.setFirstName(authorPub.getFirstName());
                                    authorRef.setLastName(authorPub.getLastName());
                                    authorRef.setName(authorPub.getLastName()+","+authorPub.getFirstName().charAt(0)+".");
                                    authorReferences.add(authorRef);
                                }
                                dto.setAuthors(authorReferences);
                            }
                            else {
                                AuthorReferenceDTO nonPubMedAuthors = new AuthorReferenceDTO();
                                nonPubMedAuthors.setName(reference.getAuthors());
                            }
                            List<MODReferenceTypeDTO> MODreferenceTypes = new ArrayList<>();
                            MODReferenceTypeDTO pubType = new MODReferenceTypeDTO();
                            pubType.setSource("ZFIN");
                            pubType.setReferenceType(reference.getType().getDisplay());
                            MODreferenceTypes.add(pubType);
                            dto.setMODReferenceTypes(MODreferenceTypes);
                            String allianceCategory = "";
                            String type = reference.getType().getDisplay();
                            if (type.equals("Journal")){
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
                            else if (type.equals("Other") || type.equals("Movie") || type.equals("Abstract")){
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
                            else if (type.equals("Movie")){

                            }
                            else {
                                allianceCategory = "Other";
                            }
                            dto.setAllianceCategory(allianceCategory);
                            List<String> pages = new ArrayList<>();
                            pages.add("reference");
                            List<CrossReferenceDTO> xrefs = new ArrayList<>();
                            List<ReferenceTagDTO> tags = new ArrayList<>();
                            ReferenceTagDTO tag = new ReferenceTagDTO();
                            if (reference.getAccessionNumber() != null){
                                dto.setPrimaryId("PMID:"+reference.getAccessionNumber());
                                CrossReferenceDTO crossReference = new CrossReferenceDTO("ZFIN",reference.getZdbID(),pages);
                                if (reference.isCanShowImages()) {
                                    tag.setReferenceId("PMID:"+reference.getAccessionNumber());
                                    tag.setSource("ZFIN");
                                    tag.setTagName("canShowImages");
                                    tags.add(tag);
                                }
                                ReferenceTagDTO incorpusTag = new ReferenceTagDTO();
                                incorpusTag.setTagName("inCorpus");
                                incorpusTag.setReferenceId("PMID:"+reference.getAccessionNumber());
                                tags.add(incorpusTag);
                                xrefs.add(crossReference);
                            }
                            else {
                                dto.setPrimaryId("ZFIN:"+reference.getZdbID());
                                CrossReferenceDTO crossReference = new CrossReferenceDTO("ZFIN",reference.getZdbID(),pages);
                                if (reference.isCanShowImages()) {
                                    tag.setReferenceId("ZFIN:"+reference.getZdbID());
                                    tag.setTagName("canShowImages");
                                    tag.setSource("ZFIN");
                                    tags.add(tag);
                                }
                                ReferenceTagDTO incorpusTag = new ReferenceTagDTO();
                                incorpusTag.setTagName("inCorpus");
                                incorpusTag.setReferenceId("ZFIN:"+reference.getZdbID());
                                tags.add(incorpusTag);
                                xrefs.add(crossReference);
                            }
                            if (reference.getDoi() != null){
                                CrossReferenceDTO crossReference = new CrossReferenceDTO("DOI",reference.getDoi(),pages);
                                xrefs.add(crossReference);
                            }
                            dto.setCrossReferences(xrefs);

                            dto.setTags(tags);
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
