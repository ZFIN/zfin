package org.zfin.sequence.load;

import org.zfin.marker.Marker;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;
import org.zfin.sequence.DBLink;
import org.zfin.sequence.ForeignDB;
import org.zfin.sequence.MarkerDBLink;
import org.zfin.sequence.TranscriptDBLink;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.zfin.repository.RepositoryFactory.getSequenceRepository;

public class EnsemblRecordFinder {

    private static final String baseUrl = "https://rest.ensembl.org";

    public static void main(String[] args) throws IOException {
        AbstractScriptWrapper wrapper = new AbstractScriptWrapper();
        wrapper.initAll();

        EnsemblRecordFinder loader = new EnsemblRecordFinder();
        loader.init();
    }

    Map<Marker, List<TranscriptDBLink>> geneEnsdartMap;
    Map<String, MarkerDBLink> ensdargMap;


    public void init() throws IOException {

        // <ensdargID, DBLink>

        ensdargMap = getMarkerDBLinksWithVegaGenbankEnsemblAccessions(ForeignDB.AvailableName.ENSEMBL);
        geneEnsdartMap = getSequenceRepository().getAllRelevantEnsemblTranscripts();
        Set<String> ensdargsIDs = ensdargMap.keySet();
        System.out.println("Total Number of Genes with Ensembl Transcripts In ZFIN: " + geneEnsdartMap.size());
        System.out.println("Total Number of Genes in ZFIN with ENSDARG accessions: " + ensdargMap.size());

        List<String> invalidEnsdargIDs = new ArrayList<>();

        AtomicInteger index = new AtomicInteger(0);
        List<String> strings = ensdargsIDs.stream().sorted().toList();

        strings
            .forEach(id -> {
                URL url = null;
                try {
                    String spec = baseUrl + "/lookup/id/" + id;
                    url = new URL(spec);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.connect();
                    int code = 0;
                    int loopIndex = 0;
                    while (loopIndex < 5) {
                        try {
                            code = connection.getResponseCode();
                        } catch (IOException e) {
                            System.out.println("Re-run " + id);
                            loopIndex++;
                        }
                        if (loopIndex == 0)
                            break;
                    }
                    if (code >= 400) {
                        invalidEnsdargIDs.add(id);
                    }
                    if (index.incrementAndGet() % 50 == 0) {
                        System.out.print(index.get() + "..");
                    }
                } catch (IOException e) {

                    System.out.println(url);
                    System.out.print("Invalid accessions: " + invalidEnsdargIDs.size());
                    System.out.println(url);
                }
            });

        System.out.println("Number fo ENSDARG IDs in ZFIN that are obsoleted at Ensembl: " + invalidEnsdargIDs.size());
        invalidEnsdargIDs.forEach(System.out::println);
        System.exit(0);
    }

    private Map<String, MarkerDBLink> getMarkerDBLinksWithVegaGenbankEnsemblAccessions(ForeignDB.AvailableName foreignDB) {
        List<MarkerDBLink> ensdargList = getSequenceRepository().getAllEnsemblGenes(foreignDB);
        Map<String, MarkerDBLink> ensdargMap = ensdargList.stream().collect(
            Collectors.toMap(DBLink::getAccessionNumber, Function.identity(), (existing, replacement) -> existing));

        return ensdargMap;
    }

}

