package org.zfin.marker.agr;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerAlias;
import org.zfin.marker.Transcript;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.sequence.ForeignDB;
import org.zfin.sequence.TranscriptDBLink;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.zfin.infrastructure.ant.AbstractValidateDataReportTask.getPropertyFileFromWebroot;
import static org.zfin.repository.RepositoryFactory.getMarkerRepository;

public class BasicTranscriptInfo extends AbstractScriptWrapper {

    private int numfOfRecords = 0;

    public BasicTranscriptInfo(int number) {
        numfOfRecords = number;
    }


    public static void main(String[] args) throws IOException {
        int number = 0;

        String webrootDir = args[0];
        String propertyFileName = getPropertyFileFromWebroot(webrootDir);

        BasicTranscriptInfo basicTranscriptInfo = new BasicTranscriptInfo(number);

        basicTranscriptInfo.initAll(propertyFileName);

        basicTranscriptInfo.init();
        File initFile=new File(ZfinPropertiesEnum.TARGETROOT+"/"+"server_apps/data_transfer"+"/"+"RNACentral"+"/"+"rnaCentral.json");

        File destFile=new File(ZfinPropertiesEnum.FTP_ROOT+"/RNACentral/rnaCentral.json");
        FileUtils.copyFile(initFile,destFile);
        System.exit(0);
    }

    private void init() throws IOException {
        initAll();
        AllTranscriptDTO AllTranscriptDTO = getAllTranscriptInfo();
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
        String jsonInString = writer.writeValueAsString(AllTranscriptDTO);
        try (PrintStream out = new PrintStream(new FileOutputStream("rnaCentral.json"))) {
            out.print(jsonInString);
        }
    }

    public AllTranscriptDTO getAllTranscriptInfo() {
        List<Transcript> allTranscripts = getMarkerRepository().getTranscriptsForNonCodingGenes();
        System.out.println(allTranscripts.size());

        List<TranscriptDTO> allTranscriptDTOList = allTranscripts.stream()
                .map(
                        transcript -> {
                            TranscriptDTO dto = new TranscriptDTO();
                            dto.setName(transcript.name);
                            dto.setSymbol(transcript.getAbbreviation());
                            dto.setPrimaryId(transcript.getZdbID());

                            dto.setSoTermId(transcript.getTranscriptType().getSoID());

                            if (CollectionUtils.isNotEmpty(transcript.getAliases())) {
                                List<String> aliasList = new ArrayList<>(transcript.getAliases().size());
                                for (MarkerAlias alias : transcript.getAliases()) {
                                    aliasList.add(alias.getAlias());
                                }
                                dto.setSynonyms(aliasList);
                            }
                            if (CollectionUtils.isNotEmpty(transcript.getSecondMarkerRelationships())) {
                                List<GeneTscriptDTO> genes = new ArrayList<>(getMarkerRepository().getGenesforTranscript(transcript).size());

                                for (Marker relatedGenes : getMarkerRepository().getGenesforTranscript(transcript)) {
                                    GeneTscriptDTO geneDTO = new GeneTscriptDTO();
                                    geneDTO.setPrimaryId(relatedGenes.getZdbID());
                                    geneDTO.setSymbol(relatedGenes.getAbbreviation());
                                    geneDTO.setName(relatedGenes.getName());
                                    if (CollectionUtils.isNotEmpty(relatedGenes.getAliases())) {
                                        List<String> aliasList = new ArrayList<>(relatedGenes.getAliases().size());
                                        for (MarkerAlias alias : relatedGenes.getAliases()) {
                                            aliasList.add(alias.getAlias());
                                        }
                                        geneDTO.setSynonyms(aliasList);
                                    }
                                    genes.add(geneDTO);
                                }
                                dto.setGenes(genes);
                            }
                            List<CrossReferenceTranscriptsDTO> dbLinkList = new ArrayList<>(transcript.getTranscriptDBLinks().size() + 1);

                            if (CollectionUtils.isNotEmpty(transcript.getTranscriptDBLinks())) {

                                for (TranscriptDBLink link : transcript.getTranscriptDBLinks()) {

                                    String dbName = DataProvider.getExternalDatabaseName(link.getReferenceDatabase().getForeignDB().getDbName());

                                    if (dbName == null)
                                        continue;
                                    // do not include ENSDARP records
                                    if (dbName.equals(ForeignDB.AvailableName.ENSEMBL.toString()) && link.getAccessionNumber().startsWith("ENSDARP"))
                                        continue;

                                    CrossReferenceTranscriptsDTO xRefDto = new CrossReferenceTranscriptsDTO(dbName, link.getAccessionNumber());
                                    dbLinkList.add(xRefDto);
                                }
                            }
                            dto.setCrossReferences(dbLinkList);
                            return dto;
                        })
                .collect(Collectors.toList());
        AllTranscriptDTO AllTranscriptDTO = new AllTranscriptDTO();
        AllTranscriptDTO.setTranscripts(allTranscriptDTOList);
        String dataProvider = "ZFIN";
        List<String> pages = new ArrayList<>();
        pages.add("homepage");
        DataProviderDTO dp = new DataProviderDTO("curated", new CrossReferenceDTO(dataProvider, dataProvider, pages));
        MetaDataDTO meta = new MetaDataDTO(new DataProviderDTO("curated", new CrossReferenceDTO(dataProvider, dataProvider, pages)));
        AllTranscriptDTO.setMetaData(meta);
        return AllTranscriptDTO;
    }
}
