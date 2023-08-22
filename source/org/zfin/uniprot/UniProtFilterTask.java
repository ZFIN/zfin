package org.zfin.uniprot;

import lombok.extern.log4j.Log4j2;
import org.biojava.bio.BioException;
import org.biojavax.RankedCrossRef;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.io.RichStreamReader;
import org.biojavax.bio.seq.io.RichStreamWriter;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;

import java.io.*;
import java.sql.SQLException;
import java.util.*;

import static org.zfin.uniprot.UniProtTools.getRichStreamReaderForUniprotDatFile;
import static org.zfin.uniprot.UniProtTools.getRichStreamWriterForUniprotDatFile;

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

    public void runTask() throws IOException, BioException {
        initIO();
        initAll();

        RichStreamReader sr = getRichStreamReaderForUniprotDatFile(filteredInputFileReader, true);

        log.debug("Starting to read file: " );
        List<RichSequence> outputEntries = readAndFilterSequencesFromStream(sr);
        log.debug("Finished reading file: " + outputEntries.size() + " entries read.");

        log.debug("Starting to write file: " );
        writeOutputFile(outputEntries, outputFileWriter);
    }

    private void initIO() throws IOException {
        roughTaxonFilter = new UniProtRoughTaxonFilter(inputFileReader);
        filteredInputFileReader = roughTaxonFilter.getFilteredReader();
    }

    private List<RichSequence> readAndFilterSequencesFromStream(RichStreamReader richStreamReader) throws BioException {
        List<String> xrefsToKeep = List.of("ZFIN", "GeneID", "RefSeq", "EMBL", "GO", "InterPro", "Pfam", "PROSITE", "PDB", "Ensembl");
        RichSequence lastSequence = null;
        int count = 0;
        List<RichSequence> uniProtSequences = new ArrayList<>();
        while (richStreamReader.hasNext()) {
            try {
                RichSequence seq = richStreamReader.nextRichSequence();
                count++;
                if (count % 1000 == 0) {
                    log.debug("Read " + count + " sequences.");
                }

                if (seq.getTaxon().getNCBITaxID() != 7955) {
                    if (!seq.getTaxon().getDisplayName().toLowerCase().contains("danio rerio")) {
                        // seq is not zebrafish, but account for entries like "Danio rerio x Danio aff. kyathit RC0455"
                        continue;
                    }
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
                log.error("Error while processing sequence after " + count + " records. Last sequence read: " + lastSequence.getAccession() + " " + lastSequence.getName(), e);
            }
        }
        roughTaxonFilter.cleanup();
        return uniProtSequences;
    }

    private void writeOutputFile(List<RichSequence> outputEntries, FileOutputStream outfile) {
        try {
            RichStreamWriter sw = getRichStreamWriterForUniprotDatFile(outfile);
            sw.writeStream(new SequenceListIterator(outputEntries), null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
