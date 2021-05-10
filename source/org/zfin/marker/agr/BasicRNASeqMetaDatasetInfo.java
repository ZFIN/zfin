package org.zfin.marker.agr;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.commons.collections.CollectionUtils;
import org.zfin.expression.HTPDataset;
import org.zfin.publication.Publication;

import org.zfin.ontology.datatransfer.AbstractScriptWrapper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.zfin.repository.RepositoryFactory.getExpressionRepository;

public class BasicRNASeqMetaDatasetInfo extends AbstractScriptWrapper {

    private int numOfRecords = 0;

    public BasicRNASeqMetaDatasetInfo(int number) {
        numOfRecords = number;
    }

    public static void main(String[] args) throws IOException {
        int number = 0;
        if (args.length > 0) {
            number = Integer.valueOf(args[0]);
        }
        BasicRNASeqMetaDatasetInfo basicRNASeqMetaDataInfo = new BasicRNASeqMetaDatasetInfo(number);
        basicRNASeqMetaDataInfo.init();
        System.exit(0);
    }

    private void init() throws IOException {
        initAll();
        AllRNASeqMetaDatasetDTO allDatasetDTO = getAllDatasetInfo();
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());

        String jsonInString = writer.writeValueAsString(allDatasetDTO);
        try (PrintStream out = new PrintStream(new FileOutputStream("ZFIN_1.0.1.4_HTP_Dataset.json"))) {
            out.print(jsonInString);
        }
    }

    public AllRNASeqMetaDatasetDTO getAllDatasetInfo() {
        ArrayList<HTPDataset> allDatasets = getExpressionRepository().getAllHTPDatasets();
        System.out.println(allDatasets.size());

        List<BasicRNASeqMetaDatasetDTO> allDatasetDTOList = allDatasets.stream()
                .map(
                        dataset -> {
                            BasicRNASeqMetaDatasetDTO dto = new BasicRNASeqMetaDatasetDTO();
                            HtpIDDTO datasetId = new HtpIDDTO();
                            datasetId.setPrimaryId("ZFIN:"+dataset.getZdbID());

                            ArrayList<String> htpSecondaryIds = new ArrayList<>();
                            ArrayList<CrossReferenceDTO> crossReferences = new ArrayList<>();
                            if (CollectionUtils.isNotEmpty(getExpressionRepository().getHTPSecondaryIds(dataset.getZdbID()))){
                                for (String secId : getExpressionRepository().getHTPSecondaryIds(dataset.getZdbID())){
                                    htpSecondaryIds.add(secId);
                                    List<String> xpages = new ArrayList<>();
                                    xpages.add("htp/dataset");
                                    secId = secId.split(":")[1];
                                    String dataProvider;
                                    if (secId.startsWith("ArrayExpress")) {
                                        dataProvider = "ArrayExpress";
                                    }
                                    else {
                                        dataProvider = "GEO";
                                    }

                                    CrossReferenceDTO preferredCrossReference = new CrossReferenceDTO(dataProvider, secId, xpages);
                                    crossReferences.add(preferredCrossReference);
                                    datasetId.setPreferredCrossReference(preferredCrossReference);

                                }
                                datasetId.setSecondaryId(htpSecondaryIds);
                            }
                            dto.setDatasetId(datasetId);

                            dto.setDateAssigned(dataset.getDateCurated());
                            dto.setSummary(dataset.getSummary());
                            dto.setTitle(dataset.getTitle());

                            if (CollectionUtils.isNotEmpty(getExpressionRepository().getCategoryTags(dataset.getZdbID()))){
                                ArrayList<String> categoryTags = new ArrayList<>();
                                for (String ctag : getExpressionRepository().getCategoryTags(dataset.getZdbID())) {
                                    categoryTags.add(ctag);
                                }
                                dto.setCategoryTags(categoryTags);
                            }

                            if (CollectionUtils.isNotEmpty(getExpressionRepository().getHTPPubs(dataset.getZdbID()))){
                                ArrayList<PublicationAgrDTO> datasetPubs = new ArrayList<>();
                                for (Publication pub : getExpressionRepository().getHTPPubs(dataset.getZdbID())) {
                                    PublicationAgrDTO fixedPub = new PublicationAgrDTO();
                                    List<String> pubPages = new ArrayList<>();
                                    pubPages.add("reference");
                                    CrossReferenceDTO pubXref = new CrossReferenceDTO("ZFIN", pub.getZdbID(), pubPages);
                                    if (pub.getAccessionNumber() != null) {
                                        fixedPub.setPublicationId("PMID:"+pub.getAccessionNumber());
                                        fixedPub.setCrossReference(pubXref);
                                    }
                                    else {
                                        fixedPub.setPublicationId("ZFIN:"+pub.getZdbID());
                                    }
                                    datasetPubs.add(fixedPub);
                                }
                                dto.setPublications(datasetPubs);
                            }

                            return dto;
                        })
                .collect(Collectors.toList());


        AllRNASeqMetaDatasetDTO allRNASeqMetaDatasetDTO = new AllRNASeqMetaDatasetDTO();
        allRNASeqMetaDatasetDTO.setDatasetList(allDatasetDTOList);
        String dataProvider = "ZFIN";
        List<String> pages = new ArrayList<>();
        pages.add("homepage");
        MetaDataDTO meta = new MetaDataDTO(new DataProviderDTO("curated", new CrossReferenceDTO(dataProvider, dataProvider, pages)));
        allRNASeqMetaDatasetDTO.setMetaData(meta);
        return allRNASeqMetaDatasetDTO;
    }
}
