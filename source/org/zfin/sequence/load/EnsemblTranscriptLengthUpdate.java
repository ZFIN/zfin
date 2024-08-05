package org.zfin.sequence.load;

import org.biojavax.bio.seq.RichSequence;
import org.zfin.framework.HibernateUtil;
import org.zfin.marker.Marker;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;
import org.zfin.sequence.DBLink;
import org.zfin.sequence.TranscriptDBLink;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.zfin.repository.RepositoryFactory.getSequenceRepository;

public class EnsemblTranscriptLengthUpdate extends EnsemblTranscriptBase {

    public static void main(String[] args) throws IOException {
        AbstractScriptWrapper wrapper = new AbstractScriptWrapper();
        wrapper.initAll();

        EnsemblTranscriptLengthUpdate loader = new EnsemblTranscriptLengthUpdate();
        loader.init();
    }

    private Map<String, List<RichSequence>> allEnsemblProvidedGeneMap;

    Map<Marker, List<TranscriptDBLink>> geneEnsdartMap;

    public void init() throws IOException {
        downloadFile(cdnaFileName);
        loadSequenceMapFromDownloadFile();
        System.exit(0);
    }

    private void loadSequenceMapFromDownloadFile() {

        // <ensdargID, List<RichSequence>>
        Map<String, List<RichSequence>> geneTranscriptMap = getGeneTranscriptMap(cdnaFileName);
        Map<String, List<RichSequence>> sortedGeneTranscriptMap = geneTranscriptMap.entrySet().stream()
            .sorted((e1, e2) -> Integer.compare(e2.getValue().size(), e1.getValue().size()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        // remove version number from accession ID
        allEnsemblProvidedGeneMap = new LinkedHashMap<>();
        sortedGeneTranscriptMap.forEach((versionedAccession, richSequences) -> allEnsemblProvidedGeneMap.put(getUnversionedAccession(versionedAccession), richSequences));

        geneEnsdartMap = getSequenceRepository().getAllRelevantEnsemblTranscripts();
        List<TranscriptDBLink> ensemblTranscripts = geneEnsdartMap.values().stream().flatMap(Collection::stream).toList();
        List<RichSequence> richSequences = sortedGeneTranscriptMap.values().stream().flatMap(Collection::stream).toList();
        int txDifference = richSequences.size() - ensemblTranscripts.size();

        Map<String, TranscriptDBLink> transcriptMap = ensemblTranscripts.stream().collect(Collectors.toMap(DBLink::getAccessionNumber, o -> o,
            (db1, db2) -> db1
        ));
        System.out.println("Total Number of Ensembl transcripts in FASTA file: " + richSequences.size());
        System.out.println("Total Number of Ensembl transcripts in ZFIN: " + ensemblTranscripts.size());
        System.out.println("Difference:" + txDifference + " [" + 100 * txDifference / richSequences.size() + "%]");

        HibernateUtil.createTransaction();
        try {
            List<TranscriptDBLink> modifiedTx = new ArrayList<>();
            AtomicInteger numberOfNonNullLengthAltered = new AtomicInteger();
            AtomicInteger numberOfNullLengthAdded = new AtomicInteger();
            richSequences.forEach(richSequence -> {
                String cleanedID = getString(richSequence);
                int length = richSequence.length();
                TranscriptDBLink link = transcriptMap.get(cleanedID);
                if (link == null) {
                    return;
                }
                if (link.getLength() == null) {
                    numberOfNullLengthAdded.incrementAndGet();
                    link.setLength(length);
                }
                if (link.getLength() != length) {
                    numberOfNonNullLengthAltered.incrementAndGet();
                    modifiedTx.add(link);
                    link.setLength(length);
                }
            });
            HibernateUtil.flushAndCommitCurrentSession();
            System.out.println("Number of ZFIN transcripts in ZFIN with no length: " + numberOfNullLengthAdded);
            System.out.println("Number of ZFIN transcripts in ZFIN with length adjusted: " + numberOfNonNullLengthAltered);
            System.out.println("Adjusted Tx: " + modifiedTx);

        } catch (Exception e) {
            HibernateUtil.rollbackTransaction();
            System.out.println(e.getMessage());
        }

    }

}

