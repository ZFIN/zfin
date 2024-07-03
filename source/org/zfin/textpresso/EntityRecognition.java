package org.zfin.textpresso;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.zfin.marker.Marker;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;
import si.mazi.rescu.ClientConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.zfin.repository.RepositoryFactory.getPublicationRepository;

public class EntityRecognition {

    public static void main(String[] args) throws IOException {
        AbstractScriptWrapper wrapper = new AbstractScriptWrapper();
        wrapper.initAll();
        EntityRecognition loader = new EntityRecognition();
        loader.init();
    }

    private static final ClientConfig config = new ClientConfig();

    public void init() throws IOException {
        //getEntityList("PMID:32320444", " ");
        //getEntityList("PMID:34033651", "fgf");
        List<String> pubIDs = new ArrayList<>();
        Map<String, String> pubMedZdbIdMap = new HashMap<>();
        getPubList("fgf").forEach(textpressoDocument -> {
            String[] token = textpressoDocument.getAccession().split(" ");
            Optional<String> pubmedID = Arrays.stream(token).filter(s -> s.startsWith("PMID:")).findFirst();
            pubmedID.ifPresent(pubIDs::add);
            Optional<String> modID = Arrays.stream(token).filter(s -> s.startsWith("ZFIN:")).findFirst();
            modID.ifPresent(modId -> pubMedZdbIdMap.put(pubmedID.get(), modId.replace("ZFIN:", "")));
        });
        System.out.println("Total Publications: " + pubIDs.size());
        AtomicInteger index = new AtomicInteger();
        pubIDs.forEach(pubID -> {
            if (index.incrementAndGet() % 10 == 0) {
                System.out.println(index.get());
            }
            try {
                List<String> geneIDs = getEntityList(pubID, "");
                List<Marker> attributedGenes = getPublicationRepository().getGenesByPublication(pubMedZdbIdMap.get(pubID), false);
                List<String> attributdGeneSymbol = attributedGenes.stream().map(Marker::getAbbreviation).toList();
                if (CollectionUtils.isNotEmpty(attributdGeneSymbol) && !new HashSet<>(geneIDs).containsAll(attributdGeneSymbol)) {
                    System.out.println("Publication: " + pubID + " [" + pubMedZdbIdMap.get(pubID) + "] has genes not identified by TP: " + attributdGeneSymbol.stream().filter(o -> !geneIDs.contains(o)).toList());
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        String n = null;
    }

    public static final String ACCESSION_VAR = "[%accession]";

    public Set<TextpressoDocument> getPubList(String keywords) throws IOException {
        String jsonInputString = """
                        {
                           "token": "DlrmI3M7D1ZN2FSATd3R",
                            "category": "Gene (D. rerio) (tpgdr:0000000)",
                           "query": {
                              "keywords": "[%keywords]",
                              "type": "document",
                              "case_sensitive": false,
                              "sort_by_year": false,
                              "count": 2,
                              "corpora": [
                                            "D. rerio"
                                         ]
                           }
                        }
            """;
        jsonInputString = jsonInputString.replace("[%keywords]", keywords);

        StringBuilder responseBuilder = retrieveResult(jsonInputString, "search_documents");

        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String content = responseBuilder.toString();
        Set<TextpressoDocument> matches = mapper.readValue(content, new TypeReference<>() {
        });

        return matches;
    }

    public List<String> getEntityList(String pubmedID, String keywords) throws IOException {
        String jsonInputString = """
                        {
                           "token": "DlrmI3M7D1ZN2FSATd3R",
                            "category": "Gene (D. rerio) (tpgdr:0000000)",
                           "query": {
                              "keywords": "[%keywords]",
                              "accession": "[%accession]",
                              "type": "document",
                              "case_sensitive": false,
                              "sort_by_year": false,
                              "count": 2,
                              "corpora": [
                                            "D. rerio"
                                         ]
                           }
                        }
            """;
        jsonInputString = jsonInputString.replace(ACCESSION_VAR, pubmedID);
        jsonInputString = jsonInputString.replace("[%keywords]", keywords);

        StringBuilder responseBuilder = retrieveResult(jsonInputString, "get_category_matches_document_fulltext");

        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        String content = responseBuilder.toString();
        // remove enclosing brackets
        String replaceFirst = content.substring(1);
        String substring = replaceFirst.substring(0, replaceFirst.length() - 1);
        //System.out.println(responseBuilder);
        Matches matches = mapper.readValue(substring, Matches.class);
        return matches.getMatchedSentences();
    }

    private static StringBuilder retrieveResult(String jsonInputString, String endpointContext) throws IOException {
        String baseUrl = "https://www.alliancegenome.org";
        URL url = new URL(baseUrl + "/textpresso/zfin/v1/textpresso/api/" + endpointContext);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);
        try (OutputStream os = con.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        StringBuilder responseBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
            new InputStreamReader(con.getInputStream(), "utf-8"))) {
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                responseBuilder.append(responseLine.trim());
            }
        }
        return responseBuilder;
    }

}
