package org.zfin.sequence.load;

import org.biojava.bio.seq.SequenceIterator;
import org.biojavax.Namespace;
import org.biojavax.SimpleNamespace;
import org.biojavax.bio.seq.RichSequence;
import org.zfin.marker.presentation.LinkDisplay;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;
import org.zfin.sequence.DBLink;
import org.zfin.sequence.ForeignDB;
import org.zfin.sequence.MarkerDBLink;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.zfin.repository.RepositoryFactory.getMarkerRepository;
import static org.zfin.repository.RepositoryFactory.getSequenceRepository;

public class EnsemblTranscriptFastaReader extends EnsemblTranscriptBase {

    public static final String ENSEMBL_ZF_FA = "ensembl_zf.fa";
    public static final String ENSEMBL_ZF_ONLY_FA = "ensembl_zf_only.fa";

    private static String fastaDirectory;

    public static void main(String[] args) throws IOException {
        String fastaFileDirectory = args[0];
        System.out.println("Arguments: " + fastaFileDirectory);
        String inputFileName = fastaFileDirectory + "/" + ENSEMBL_ZF_FA;
        File file = new File(inputFileName);
        System.out.println("Fasta File: " + inputFileName + " exists: " + file.exists());

        AbstractScriptWrapper wrapper = new AbstractScriptWrapper();
        wrapper.initAll();

        EnsemblTranscriptFastaReader loader = new EnsemblTranscriptFastaReader();
        fastaDirectory = fastaFileDirectory;
        loader.initCondensed(file);
        loader.init();
    }

    public void init() throws IOException {
        List<RichSequence> allFastaRecords = geneTranscriptMap.values().stream().flatMap(Collection::stream).toList();
        List<String> allTranscriptIDsInZfin = getAllTranscriptIdsInZFIN();

        List<RichSequence> filteredFastaRecords = allFastaRecords.stream()
            .filter(richSequence -> allTranscriptIDsInZfin.contains(getUnversionedAccession(getGeneIdFromZfinDefline(richSequence))))
            .toList();
        System.out.println("Total Number of Ensembl Transcripts In ZFIN FASTA file: " + filteredFastaRecords.size());

        BufferedWriter writer = new BufferedWriter(new FileWriter(fastaDirectory + "/" + ENSEMBL_ZF_ONLY_FA, false));
        filteredFastaRecords.forEach(richSequence -> {
            try {
                writeManually(writer, richSequence);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        writer.close();
        System.exit(0);
    }

    private List<String> getAllTranscriptIdsInZFIN() {
        List<DBLink> transcripts = getSequenceRepository().getAllEnsemblTranscripts();
        return transcripts.stream().map(DBLink::getAccessionNumber).toList();
    }

    private List<RichSequence> loadSequenceMapFromDownloadFile() {

        List<RichSequence> allFastaRecords = getAllFastaRecordsFromFile();
        System.out.println("Total Number of Ensembl transcripts in FASTA file: " + allFastaRecords.size());
        return allFastaRecords;
    }


    private static void write(OutputStream outputStream, SequenceIterator sequenceIterator) throws IOException {
        Namespace namespace = new SimpleNamespace("ZFIN-Ensembl-transcripts");
        RichSequence.IOTools.writeFasta(outputStream, sequenceIterator, namespace);
    }

    private static void writeManually(BufferedWriter writer, RichSequence richSequence) throws IOException {
        StringBuilder builder = new StringBuilder(">tpe|");
        builder.append(richSequence.getAccession());
        builder.append("|");
        String[] token = richSequence.getDescription().split(" ");
        String chromosome = null;
        // loop over all elements in the defline
        for (String element : token) {
            if (element.startsWith("chromosome")) {
                chromosome = element;
            } else {
                builder.append(element);
                builder.append(" ");
            }
        }

        if (chromosome != null) {
            String[] split = chromosome.split(":");
            int index = 0;
            String chromosomeElement = "";
            for (String element : split) {
                if (index++ == split.length - 1) {
                    chromosomeElement += richSequence.seqString().length();
                } else {
                    chromosomeElement += element + ":";
                }
            }
            builder.append(chromosomeElement);
        }
        writer.write(builder.toString());
        writer.newLine();
        String sequenceString = richSequence.seqString().toUpperCase();
        Collection<String> textArray = splitByNumber(sequenceString, 60);
        textArray.forEach(s -> {
            try {
                writer.write(s);
                writer.newLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static Collection<String> splitByNumber(String s, int chunkSize) {
        final AtomicInteger counter = new AtomicInteger(0);
        Collection<String> returnVal = s.chars()
            .mapToObj(i -> String.valueOf((char) i))
            .collect(Collectors.groupingBy(it -> counter.getAndIncrement() / chunkSize
                , Collectors.joining()))
            .values();
        return returnVal;
    }


}

