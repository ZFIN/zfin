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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.zfin.repository.RepositoryFactory.getMarkerRepository;
import static org.zfin.repository.RepositoryFactory.getSequenceRepository;

public class EnsemblTranscriptFastaReader extends EnsemblTranscriptBase {

    public static void main(String[] args) throws IOException {
        AbstractScriptWrapper wrapper = new AbstractScriptWrapper();
        wrapper.initAll();

        EnsemblTranscriptFastaReader loader = new EnsemblTranscriptFastaReader();
        loader.init();
    }

    public void init() throws IOException {
        super.init();
        List<RichSequence> allFastaRecords = loadSequenceMapFromDownloadFile();

        List<String> allTranscriptIDsInZfin = getAllTranscriptIdsInZFIN();

        List<RichSequence> filteredFastaRecords = allFastaRecords.stream().filter(richSequence -> allTranscriptIDsInZfin.contains(getUnversionedAccession(getGeneId(richSequence)))).toList();
        System.out.println("Total Number of Ensembl Transcripts In ZFIN: " + filteredFastaRecords.size());

        BufferedWriter writer = new BufferedWriter(new FileWriter("Danio_rerio.GRCz11.cdna-ncrna.all.fa", false));
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
        List<MarkerDBLink> ensdargList = getSequenceRepository().getAllEnsemblGenes(ForeignDB.AvailableName.ENSEMBL_GRCZ11_);
        List<LinkDisplay> vegaList = getMarkerRepository().getAllVegaGeneDBLinksTranscript();
        List<MarkerDBLink> genbankList = getSequenceRepository().getAllGenbankGenes();
        List<String> ensdargIDs = new ArrayList<>();
        ensdargIDs.addAll(ensdargList.stream().map(DBLink::getAccessionNumber).toList());
        ensdargIDs.addAll(vegaList.stream().map(LinkDisplay::getAccession).toList());
        ensdargIDs.addAll(genbankList.stream().map(DBLink::getAccessionNumber).toList());
        return ensdargIDs;
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
        StringBuilder builder = new StringBuilder(">");
        builder.append(richSequence.getAccession());
        builder.append(" ");
        builder.append(richSequence.getDescription());
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

