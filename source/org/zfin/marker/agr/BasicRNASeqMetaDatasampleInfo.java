package org.zfin.marker.agr;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import org.zfin.expression.HTPDataset;
import org.zfin.expression.HTPDatasetSample;
import org.zfin.expression.HTPDatasetSampleDetail;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.datatransfer.GenericCronJobReport;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static org.zfin.repository.RepositoryFactory.getExpressionRepository;
import static org.zfin.repository.RepositoryFactory.getOntologyRepository;

public class BasicRNASeqMetaDatasampleInfo extends AbstractScriptWrapper {

    private int numOfRecords = 0;

    public BasicRNASeqMetaDatasampleInfo(int number) {
        numOfRecords = number;
    }

    public static void main(String[] args) throws IOException {
        int number = 0;
        if (args.length > 0) {
            number = Integer.valueOf(args[0]);
        }
        BasicRNASeqMetaDatasampleInfo basicRNASeqMetaDatasampleInfo = new BasicRNASeqMetaDatasampleInfo(number);
        basicRNASeqMetaDatasampleInfo.init();
        System.exit(0);
    }

    private void init() throws IOException {
        initAll();
        ALLRNASeqMetaDatasampleDTO allDatasampleDTO = getAllDatasampleInfo();
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());

        String jsonInString = writer.writeValueAsString(allDatasampleDTO);
        try (PrintStream out = new PrintStream(new FileOutputStream("ZFIN_1.0.1.1_HTP_DatasetSample.json"))) {
            out.print(jsonInString);
        }
    }

    public ALLRNASeqMetaDatasampleDTO getAllDatasampleInfo() {
        ArrayList<HTPDatasetSample> allDataSampleDetails = getExpressionRepository().getAllHTPDatasetSamples();
        System.out.println(allDataSampleDetails.size());

        List<BasicRNASeqMetaDatasampleDTO> allDatasampleDTOList = allDataSampleDetails.stream()
                .map(
                        datasample -> {
                            BasicRNASeqMetaDatasampleDTO dto = new BasicRNASeqMetaDatasampleDTO();
                            HtpIDDTO datasampleId = new HtpIDDTO();
                            datasampleId.setPrimaryId(datasample.getSampleId());
                            dto.setSampleId(datasampleId);
                            dto.setSampleTitle(datasample.getSampleTitle());

                            dto.setAbundance(datasample.getAbundance());
                            dto.setAssayType(datasample.getSampleType());
                            dto.setAssemblyVersion(datasample.getAssembly());

                            dto.setSampleType(datasample.getSampleType());
                            dto.setSex(datasample.getSex());

                            //This lack of pluralization comes from the Alliance schema.
                            ArrayList<String> datasetIds = new ArrayList<>();
                            datasetIds.add("ZFIN:" + datasample.getHtpDataset().getZdbID());
                            dto.setDatasetId(datasetIds);

                            dto.setDateAssigned(datasample.getHtpDataset().getDateCurated());
                            dto.setSequencingFormat(datasample.getSequencingFormat());

                            HtpGenomicInformationDTO biosample = new HtpGenomicInformationDTO();
                            biosample.setBiosampleId(datasample.getFish().getZdbID());
                            biosample.setIdType("fish");
                            dto.setGenomicInformation(biosample);

                            // for each sample detail - get whenExpressed object (stageid and stagename)
                            // put stageid and stagename into an object called whenExpressed
                            UberonSlimTermDTO stageUberonTerm = new UberonSlimTermDTO("");
                            BioSampleAgeDTO sampleAge = new BioSampleAgeDTO();
                            ExpressionStageIdentifiersDTO stageInfo = new ExpressionStageIdentifiersDTO(datasample.getStage().getTermName(),
                                    datasample.getStage().getOboID(), stageUberonTerm);
                            sampleAge.setStage(stageInfo);
                            dto.setSampleAge(sampleAge);


                            ArrayList<HTPDatasetSampleDetail> anatomySampleDetails = getExpressionRepository().getSampleDetail(datasample);

                            ArrayList<ExpressionTermIdentifiersDTO> anatomies = new ArrayList<>();
                            ArrayList<ExpressionTermIdentifiersDTO> sampleLocations = new ArrayList<>();
                            for (HTPDatasetSampleDetail anatomyDetail : anatomySampleDetails) {
                                String whereExpressedStatement = null;
                                HashSet<UberonSlimTermDTO> uberonSlimTermDTOs = new HashSet<>();
                                UberonSlimTermDTO anatomyUberonTerm = new UberonSlimTermDTO("");
                                uberonSlimTermDTOs.add(anatomyUberonTerm);
                                String superTerm = null;
                                String subTerm = null;
                                String cellularComponent = null;
                                String superQ = null;

                                if (anatomyDetail.getAnatomySuperTerm() != null) {
                                    superTerm = anatomyDetail.getAnatomySuperTerm().getOboID();
                                }
                                if (anatomyDetail.getAnatomySubTerm() != null) {
                                    subTerm = anatomyDetail.getAnatomySubTerm().getOboID();
                                }
                                if (anatomyDetail.getCellularComponentTerm() != null) {
                                    cellularComponent = anatomyDetail.getCellularComponentTerm().getOboID();
                                }
                                if (anatomyDetail.getAnatomySuperQualifierTerm() != null) {
                                    superQ = anatomyDetail.getAnatomySuperQualifierTerm().getOboID();
                                }
                                ExpressionTermIdentifiersDTO anatomy =
                                        new ExpressionTermIdentifiersDTO(whereExpressedStatement,
                                                cellularComponent,
                                                superTerm,
                                                subTerm,
                                                superQ,
                                                uberonSlimTermDTOs);
                                anatomies.add(anatomy);
                            }
                            dto.setSampleLocation(anatomies);

                            dto.setSampleAge(sampleAge);
                            return dto;
                        })

                .collect(Collectors.toList());


        ALLRNASeqMetaDatasampleDTO allRNASeqMetaDatasampleDTO = new ALLRNASeqMetaDatasampleDTO();
        allRNASeqMetaDatasampleDTO.setDatasampleList(allDatasampleDTOList);
        String dataProvider = "ZFIN";
        List<String> pages = new ArrayList<>();
        pages.add("homepage");
        MetaDataDTO meta = new MetaDataDTO(new DataProviderDTO("curated", new CrossReferenceDTO(dataProvider, dataProvider, pages)));
        allRNASeqMetaDatasampleDTO.setMetaData(meta);
        return allRNASeqMetaDatasampleDTO;
    }
}
