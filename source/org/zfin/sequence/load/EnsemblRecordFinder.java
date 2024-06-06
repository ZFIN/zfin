package org.zfin.sequence.load;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.biojava.bio.BioException;
import org.biojava.bio.seq.SequenceIterator;
import org.biojava.bio.seq.io.SymbolTokenization;
import org.biojavax.Namespace;
import org.biojavax.SimpleNamespace;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequenceIterator;
import org.zfin.Species;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.VocabularyTerm;
import org.zfin.framework.services.VocabularyService;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.marker.*;
import org.zfin.marker.presentation.LinkDisplay;
import org.zfin.marker.presentation.RelatedTranscriptDisplay;
import org.zfin.mutant.Genotype;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;
import org.zfin.profile.Person;
import org.zfin.publication.Publication;
import org.zfin.sequence.*;
import org.zfin.sequence.service.TranscriptService;
import org.zfin.util.FileUtil;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static htsjdk.samtools.util.ftp.FTPClient.READ_TIMEOUT;
import static java.util.stream.Collectors.joining;
import static org.zfin.framework.services.VocabularyEnum.TRANSCRIPT_ANNOTATION_METHOD;
import static org.zfin.marker.Marker.Type.TSCRIPT;
import static org.zfin.marker.TranscriptType.Type.MRNA;
import static org.zfin.repository.RepositoryFactory.*;
import static org.zfin.sequence.DisplayGroup.GroupName.DISPLAYED_NUCLEOTIDE_SEQUENCE;

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

