package org.zfin.uniprot;

import lombok.extern.log4j.Log4j2;
import org.biojava.bio.BioException;
import org.biojavax.RankedCrossRef;
import org.biojavax.bio.seq.io.RichStreamWriter;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;
import org.zfin.uniprot.adapter.RichSequenceAdapter;
import org.zfin.uniprot.adapter.RichStreamReaderAdapter;
import org.zfin.uniprot.datfiles.UniprotReleaseRecords;

import java.io.*;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static org.zfin.uniprot.datfiles.DatFileReader.getRichStreamReaderForUniprotDatFile;
import static org.zfin.uniprot.datfiles.DatFileWriter.getRichStreamWriterForUniprotDatFile;

/**
 * This class is used to slim down the uniprot load files.
 * The first step is to filter out all the records that don't apply to zebrafish.
 * The second step is to filter out all the records that don't have one of the following DR lines:
 *
 * 1. ZFIN  (eg. DR   ZFIN; ZDB-GENE-141216-405; si:ch211-193i15.1.)
 * 2. GeneID (NCBI) (eg. DR   GeneID; 100537590; -.)
 * 3. RefSeq (eg. DR   RefSeq; XP_003199568.1; XM_003199520.5.
 *
 */
@Log4j2
public class UniProtFilterTask extends AbstractScriptWrapper {
    private BufferedReader inputFileReader;
    private BufferedReader filteredInputFileReader = null;
    private FileOutputStream outputFileWriter;

    private UniProtRoughTaxonFilter roughTaxonFilter = null;

    public UniProtFilterTask(BufferedReader bufferedReader, FileOutputStream fileOutputStream) {
        this.inputFileReader = bufferedReader;
        this.outputFileWriter = fileOutputStream;
    }

    public void runTask() throws IOException, BioException, SQLException {
        List<RichSequenceAdapter> outputEntries = getFilteredRichSequences();

        if (outputFileWriter != null) {
            log.info("Starting to write file: ");
            writeOutputFile(outputEntries, outputFileWriter);
        }
    }

    public List<RichSequenceAdapter> getFilteredRichSequences() throws IOException, BioException {
        initIO();
        initAll();

        log.info("Starting to read file: " );
        List<RichSequenceAdapter> outputEntries = readAndFilterSequencesFromStream();
        log.info("Finished reading file: " + outputEntries.size() + " entries read.");
        return outputEntries;
    }

    private void initIO() throws IOException {
        roughTaxonFilter = new UniProtRoughTaxonFilter(inputFileReader);
        filteredInputFileReader = roughTaxonFilter.getFilteredReader();
    }

    private List<RichSequenceAdapter> readAndFilterSequencesFromStream() throws BioException {
        RichStreamReaderAdapter richStreamReader = getRichStreamReaderForUniprotDatFile(filteredInputFileReader, true);

        List<String> xrefsToKeep = List.of("ZFIN", "GeneID", "RefSeq", "EMBL", "GO", "InterPro", "Pfam", "PROSITE", "PDB", "Ensembl", "EC");
        RichSequenceAdapter lastSequence = null;
        int count = 0;
        List<RichSequenceAdapter> uniProtSequences = new ArrayList<>();
        while (richStreamReader.hasNext()) {
            try {
                RichSequenceAdapter seq = richStreamReader.nextRichSequence();
                count++;
                if (count % 1000 == 0) {
                    log.info("Read " + count + " sequences.");
                }

                if (!seq.isDanioRerioOrRelated()) {
                    continue;
                }

                TreeSet<RankedCrossRef> sortedRankedCrossRefs = new TreeSet<>();

                for (RankedCrossRef rankedCrossRef : (Set<RankedCrossRef>)seq.getRankedCrossRefs() ) {
                    if (xrefsToKeep.contains(rankedCrossRef.getCrossRef().getDbname())) {
                        sortedRankedCrossRefs.add(rankedCrossRef);
                    }
                }

                seq.setRankedCrossRefs(sortedRankedCrossRefs);
                uniProtSequences.add(seq);

                lastSequence = seq;
            } catch (Exception e) {
                if (lastSequence == null) {
                    throw e;
                }
                System.err.println("Error while processing sequence after " + count + " records. Last sequence read: " + lastSequence.getAccession() + " " + lastSequence.getName());
            }
        }
        roughTaxonFilter.cleanup();
        return uniProtSequences;
    }

    private void writeOutputFile(List<RichSequenceAdapter> outputEntries, FileOutputStream outfile) {
        try {
            RichStreamWriter sw = getRichStreamWriterForUniprotDatFile(outfile);
            sw.writeStream(new SequenceListIterator(outputEntries), null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<RichSequenceAdapter> readAllZebrafishEntriesFromSource(BufferedReader reader) throws BioException, IOException {
        UniProtFilterTask filterTask = new UniProtFilterTask(reader, null);
        return filterTask.getFilteredRichSequences();
    }
    public static Map<String, RichSequenceAdapter> readAllZebrafishEntriesFromSourceIntoMap(BufferedReader reader) throws BioException, IOException {
        return readAllZebrafishEntriesFromSource(reader).stream().collect(Collectors.toMap(RichSequenceAdapter::getAccession, entry -> entry));
    }

    public static UniprotReleaseRecords readAllZebrafishEntriesFromSourceIntoRecords(BufferedReader reader) throws BioException, IOException {
        return new UniprotReleaseRecords(readAllZebrafishEntriesFromSourceIntoMap(reader));
    }

}
