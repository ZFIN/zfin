package org.zfin.datatransfer.webservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.ListUtils;

import java.io.*;
import java.util.*;

@Log4j2
public class NCBIRefSeqFetch {

    public static final int MAX_RESULTS = 200;
    public static final int WAIT_TIME = 2000; //wait 2 seconds between api requests to avoid throttling issues

    public record NCBIRefSeqData(String uid, String caption, String comment, String status, String replacedby) {
    }

    public void setCacheFile(File cacheFile) {
        this.cacheFile = cacheFile;
    }

    private File cacheFile = null;

    public Map<String, NCBIRefSeqData> fetchRefSeqsByID(List<String> refseqs) {
        if (cacheFile == null) {
            return fetchRefSeqsByIdWithAPI(refseqs);
        } else {
            return fetchRefSeqsByIdWithCache(refseqs);
        }
    }

    private Map<String, NCBIRefSeqData> fetchRefSeqsByIdWithCache(List<String> refseqs) {
        try {
            return new ObjectMapper().readValue(cacheFile, new TypeReference<Map<String, NCBIRefSeqData>>() {});
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * Get the refseq data from NCBI based on the list of RefSeq accessions provided.
     * <p>
     * This is similar to (for example) running `curl 'https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=nuccore&term=XM_002664955,XM_005172741,XM_017351574&retmode=json'`
     * And then using the resulting IDs to run `curl 'https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi?db=nuccore&id=1040666677,1207109378,1207123380&retmode=json'`
     *
     * @param refseqs
     * @return
     */
    public Map<String, NCBIRefSeqData> fetchRefSeqsByIdWithAPI(List<String> refseqs) {
        //remove version from refseqs:
        refseqs = refseqs.stream().map(refseq -> {
            if (refseq.contains(".")) {
                return refseq.substring(0, refseq.indexOf("."));
            }
            return refseq;
        }).toList();

        //sort them into protein and nuccore refseqs
        List<String> proteinRefseqs = new ArrayList<>();
        List<String> nucleotideRefseqs = new ArrayList<>();
        for (String refseq : refseqs) {
            if (refseq.startsWith("NM") || refseq.startsWith("XM") || refseq.startsWith("NC")) {
                nucleotideRefseqs.add(refseq);
            } else if (refseq.startsWith("XP") || refseq.startsWith("NP")) {
                proteinRefseqs.add(refseq);
            } else {
                throw new IllegalArgumentException("Invalid refseq (Expecting string starting with NM, XM, XP, or NP: " + refseq);
            }
        }
        List<List<String>> proteinBatches = ListUtils.partition(proteinRefseqs, MAX_RESULTS);
        List<List<String>> nucleotideBatches = ListUtils.partition(nucleotideRefseqs, MAX_RESULTS);

        Map<String, NCBIRefSeqData> dataByRefseq = new HashMap<>();
        for (List<String> batch : proteinBatches) {
            Set<String> tempIDs = getNcbiIDsByAccessions(batch, NCBIEfetch.Type.POLYPEPTIDE);
            sleepBetweenRequests();
            dataByRefseq.putAll(getNcbiSequenceDetailsByIDs(tempIDs, NCBIEfetch.Type.POLYPEPTIDE));
            sleepBetweenRequests();
        }

        for (List<String> batch : nucleotideBatches) {
            Set<String> tempIDs = getNcbiIDsByAccessions(batch, NCBIEfetch.Type.NUCLEOTIDE);
            sleepBetweenRequests();
            dataByRefseq.putAll(getNcbiSequenceDetailsByIDs(tempIDs, NCBIEfetch.Type.NUCLEOTIDE));
            sleepBetweenRequests();
        }

        return dataByRefseq;
    }

    private void sleepBetweenRequests() {
        try {
            Thread.sleep(WAIT_TIME);
        } catch (InterruptedException e) {
            log.error("Thread interrupted while sleeping");
            log.error(e);
        }
    }

    public static void writeCache(File outputFile, Map<String, NCBIRefSeqData> contents) throws JsonProcessingException, FileNotFoundException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
        String jsonInString = writer.writeValueAsString(contents);
        try (PrintStream out = new PrintStream(new FileOutputStream(outputFile))) {
            out.print(jsonInString);
        }
    }

    private static Map<String, NCBIRefSeqData> getNcbiSequenceDetailsByIDs(Set<String> ids, NCBIEfetch.Type type) {
        Map<String, NCBIRefSeqData> returnData = new HashMap<>();
        try {
            String results = NCBIEfetch.getSequenceSummaryJsonByID(String.join(",", ids), type, MAX_RESULTS);
            Map map = (Map) ((new ObjectMapper()).readValue(results, Object.class));
            Map result = (Map) map.get("result");
            Set<String> keys = (Set) result.keySet();
            for (String key : keys) {
                //skip the "uids" list
                if ("uids".equals(key)) continue;
                Map rec = (Map) result.get(key);
                NCBIRefSeqData refSeqData = new NCBIRefSeqData((String) rec.get("uid"), (String) rec.get("caption"), (String) rec.get("comment"), (String) rec.get("status"), (String) rec.get("replacedby"));
                returnData.put(refSeqData.caption, refSeqData);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return returnData;
    }

    private static Set<String> getNcbiIDsByAccessions(List<String> ids, NCBIEfetch.Type sequenceType) {
        String commaSeparatedAccessions = String.join(",", ids);
        String resultsJson = "";
        try {
            resultsJson = NCBIEfetch.searchSequenceJsonForAccession(commaSeparatedAccessions, sequenceType, MAX_RESULTS);
            Map map = (Map) ((new ObjectMapper()).readValue(resultsJson, Object.class));

            //esearchresult.idList
            Map searchResults = (Map) map.get("esearchresult");
            List<String> idlist = (List) searchResults.get("idlist");
            return new HashSet<>(idlist);
        } catch (Exception e) {
            log.error(e.getMessage());
            log.error("Results json:\n\n" + resultsJson);
            throw new RuntimeException(e);
        }
    }
}
