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
import org.zfin.marker.TranscriptSequence;

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

        File destFile=new File(ZfinPropertiesEnum.DOWNLOAD_DIRECTORY+"/current/rnaCentral.json");
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
        List<Transcript> allTranscriptsInDb = getMarkerRepository().getTranscriptsForNonCodingGenes();
        List<Transcript> allTranscripts = new ArrayList<>();
        for (Transcript tscript: allTranscriptsInDb){
            if (!tscript.isWithdrawn()){
                if (getMarkerRepository().getTranscriptSequence(tscript)!=null)
                allTranscripts.add(tscript);
            }
        }
        System.out.println(allTranscripts.size());

        List<TranscriptDTO> allTranscriptDTOList = allTranscripts.stream()
                .map(
                        transcript -> {
                            TranscriptDTO dto = new TranscriptDTO();
                            dto.setName(transcript.name);
                            dto.setSymbol(transcript.getAbbreviation());
                            dto.setPrimaryId(transcript.getZdbID());
                            dto.setSoTermId(transcript.getTranscriptType().getSoID());
                            dto.setUrl("http://zfin.org/"+transcript.getZdbID());
                            if (getMarkerRepository().getTranscriptSequence(transcript)!=null){
                                dto.setSequence(getMarkerRepository().getTranscriptSequence(transcript).getSequence());
                            }
                            if (CollectionUtils.isNotEmpty(transcript.getAliases())) {
                                List<String> aliasList = new ArrayList<>(transcript.getAliases().size());
                                for (MarkerAlias alias : transcript.getAliases()) {
                                    aliasList.add(alias.getAlias());
                                }
                                dto.setSymbolSynonyms(aliasList);
                            }
                            if (CollectionUtils.isNotEmpty(transcript.getSecondMarkerRelationships())) {
                                Marker relatedGene = getMarkerRepository().getGeneforTranscript(transcript);
                                GeneTscriptDTO geneDTO = new GeneTscriptDTO();
                                geneDTO.setGeneId("ZFIN:"+relatedGene.getZdbID());
                                geneDTO.setSymbol(relatedGene.getAbbreviation());
                                geneDTO.setName(relatedGene.getName());
                                if (CollectionUtils.isNotEmpty(relatedGene.getAliases())) {
                                    List<String> aliasList = new ArrayList<>(relatedGene.getAliases().size());
                                    for (MarkerAlias alias : relatedGene.getAliases()) {
                                        aliasList.add(alias.getAlias());
                                    }
                                    geneDTO.setSynonyms(aliasList);
                                }
                                dto.setGene(geneDTO);
                            }
                                List<CrossReferenceTranscriptsDTO> dbLinkList = new ArrayList<>(transcript.getTranscriptDBLinks().size() + 1);
                            List<String> dbLinks=new ArrayList<>(transcript.getTranscriptDBLinks().size() + 1);
                                if (CollectionUtils.isNotEmpty(transcript.getTranscriptDBLinks())) {

                                    for (TranscriptDBLink link : transcript.getTranscriptDBLinks()) {

                                        String dbName = DataProvider.getExternalDatabaseName(link.getReferenceDatabase().getForeignDB().getDbName());

                                        if (dbName == null)
                                            continue;
                                        // do not include ENSDARP records
                                        if (dbName.equals(ForeignDB.AvailableName.ENSEMBL.toString()) && link.getAccessionNumber().startsWith("ENSDARP"))
                                            continue;

                                        CrossReferenceTranscriptsDTO xRefDto = new CrossReferenceTranscriptsDTO(dbName, link.getAccessionNumber());
                                        String xRef=dbName.toUpperCase()+":"+link.getAccessionNumber().trim();
                                        xRef=xRef.replace("MIRBASE MATURE","MIRBASE");
                                        dbLinks.add(xRef);
                                        dbLinkList.add(xRefDto);
                                    }
                                }
                               // dto.setCrossReferenceIds(dbLinkList);
                            dto.setCrossReferenceIds(dbLinks);
                                return dto;
                            })
                .collect(Collectors.toList());
                            AllTranscriptDTO AllTranscriptDTO = new AllTranscriptDTO();
                            AllTranscriptDTO.setTranscripts(allTranscriptDTOList);
                            RNACentralMetaDataDTO meta = new RNACentralMetaDataDTO();
                            meta.setDataProvider("ZFIN");
                            meta.setSchemaVersion("0.4.0");
                            //List<String> pubs = new ArrayList<>(List.of("PMID: 30407545"));
        List<String> pubs = new ArrayList<String>();
        pubs.add("PMID: 30407545");


                            meta.setPublications(pubs);
        System.out.println(meta.getPublications().size());
                            AllTranscriptDTO.setMetaData(meta);
                            return AllTranscriptDTO;
                        }

    }
