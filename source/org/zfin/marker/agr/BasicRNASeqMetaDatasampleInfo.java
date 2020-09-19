package org.zfin.marker.agr;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import org.apache.commons.collections.CollectionUtils;
import org.zfin.expression.HTPDataset;
import org.zfin.expression.HTPDatasetSample;
import org.zfin.expression.HTPDatasetSampleDetail;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.datatransfer.GenericCronJobReport;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

import org.zfin.anatomy.DevelopmentStage;

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
                            dto.setSampleType(datasample.getSampleType());
                            dto.setAssayType(datasample.getAssayType());
                            ArrayList<String> versions = new ArrayList<>();

                            if (datasample.getAssembly() != null) {
                                versions.add(datasample.getAssembly());
                                dto.setAssemblyVersions(versions);
                            }
                            dto.setAssayType(datasample.getAssayType());
                            dto.setSampleType(datasample.getSampleType());
                            dto.setSex(datasample.getSex());

                            //This lack of pluralization comes from the Alliance schema.
                            ArrayList<String> datasetIds = new ArrayList<>();
                            datasetIds.add("ZFIN:" + datasample.getHtpDataset().getZdbID());
                            dto.setDatasetIds(datasetIds);

                            dto.setDateAssigned(datasample.getHtpDataset().getDateCurated());
                            dto.setSequencingFormat(datasample.getSequencingFormat());

                            // purposefully commented out because the HTP metadata schema is broken and doesn't
                            // allow biosamples to validate -- so leaving out for 3.1.1, but this code works
                            // and should be uncommented when the schema is fixed.

                            HtpGenomicInformationDTO biosample = new HtpGenomicInformationDTO();
                            biosample.setBiosampleId(datasample.getFish().getZdbID());
                            biosample.setIdType("fish");
                            dto.setGenomicInformation(biosample);

                            // for each sample detail - get whenExpressed object (stageid and stagename)
                            // put stageid and stagename into an object called whenExpressed

                            UberonSlimTermDTO stageUberonTerm = new UberonSlimTermDTO("");
                            DevelopmentStage stage = getOntologyRepository().getDevelopmentStageFromTerm(datasample.getStage());
                            if (stage.getName().startsWith("Hatching") || stage.getName().startsWith("Larval") || stage.getName().startsWith("Juvenile")) {
                                stageUberonTerm.setUberonTerm("post embryonic, pre-adult");

                            }
                            if (stage.getName().startsWith("Adult")) {
                                stageUberonTerm.setUberonTerm("UBERON:0000113");

                            }
                            if (stage.getHoursEnd() <= 48.00) {
                                stageUberonTerm.setUberonTerm("UBERON:0000068");
                            }

                            BioSampleAgeDTO sampleAge = new BioSampleAgeDTO();
                            ExpressionStageIdentifiersDTO stageInfo = new ExpressionStageIdentifiersDTO(datasample.getStage().getTermName(),
                                    datasample.getStage().getOboID(), stageUberonTerm);
                            sampleAge.setStage(stageInfo);
                            dto.setSampleAge(sampleAge);
                            dto.setTaxonId("NCBITaxon:7955");


                            ArrayList<HTPDatasetSampleDetail> anatomySampleDetails = getExpressionRepository().getSampleDetail(datasample);
                            ArrayList<ExpressionTermIdentifiersDTO> anatomies = new ArrayList<>();

                            Map<String, List<UberonSlimTermDTO>> zfaUberonMap = getExpressionRepository().getAllZfaUberonMap();
                            for (HTPDatasetSampleDetail anatomyDetail : anatomySampleDetails) {
                                String whereExpressedStatement = null;

                                Set<UberonSlimTermDTO> anatomicalStructureUberonSlimTermIds = new HashSet<>();
                                String superTerm = null;
                                String subTerm = null;
                                String cellularComponent = null;
                                String superQ = null;

                                if (anatomyDetail.getAnatomySuperTerm() != null) {
                                    superTerm = anatomyDetail.getAnatomySuperTerm().getOboID();
                                    whereExpressedStatement = anatomyDetail.getAnatomySuperTerm().getTermName();
                                    if (zfaUberonMap.get(anatomyDetail.getAnatomySuperTerm().getZdbID()) != null) {
                                        anatomicalStructureUberonSlimTermIds.addAll(zfaUberonMap.get(anatomyDetail.getAnatomySuperTerm().getOboID()));
                                    }
                                }
                                if (anatomyDetail.getAnatomySubTerm() != null) {
                                    subTerm = anatomyDetail.getAnatomySubTerm().getOboID();
                                    whereExpressedStatement = whereExpressedStatement + " " + anatomyDetail.getAnatomySubTerm().getTermName();
                                    if (zfaUberonMap.get(anatomyDetail.getAnatomySubTerm().getOboID()) != null) {
                                        anatomicalStructureUberonSlimTermIds.addAll(zfaUberonMap.get(anatomyDetail.getAnatomySubTerm().getOboID()));
                                    }
                                }
                                if (anatomyDetail.getCellularComponentTerm() != null) {
                                    cellularComponent = anatomyDetail.getCellularComponentTerm().getOboID();
                                    whereExpressedStatement = whereExpressedStatement + " " + anatomyDetail.getCellularComponentTerm().getTermName();
                                }
                                if (anatomyDetail.getAnatomySuperQualifierTerm() != null) {
                                    superQ = anatomyDetail.getAnatomySuperQualifierTerm().getOboID();
                                    whereExpressedStatement = whereExpressedStatement + " " + anatomyDetail.getAnatomySuperQualifierTerm().getTermName();
                                }

                                if (CollectionUtils.isEmpty(anatomicalStructureUberonSlimTermIds)) {
                                    anatomicalStructureUberonSlimTermIds.add(new UberonSlimTermDTO("Other"));
                                }
                                ExpressionTermIdentifiersDTO anatomy =
                                        new ExpressionTermIdentifiersDTO(whereExpressedStatement,
                                                cellularComponent,
                                                superTerm,
                                                subTerm,
                                                superQ,
                                                anatomicalStructureUberonSlimTermIds);
                                anatomies.add(anatomy);
                            }
                            dto.setSampleLocations(anatomies);

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
