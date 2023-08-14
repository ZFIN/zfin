package org.zfin.uniprot;

import org.apache.commons.collections4.CollectionUtils;
import org.biojava.bio.BioException;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.io.RichStreamReader;
import org.biojavax.bio.seq.io.RichStreamWriter;
import org.zfin.framework.HibernateUtil;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;
import org.zfin.uniprot.diff.RichSequenceDiff;
import org.zfin.uniprot.diff.RichSequenceDiffSerializer;
import org.zfin.uniprot.diff.UniProtDiffSet;
import org.zfin.uniprot.diff.UniProtDiffSetSerializer;

import java.io.*;
import java.sql.SQLException;
import java.util.*;

import static org.zfin.uniprot.UniProtTools.*;

/**
 * This class is used to compare uniprot load files. Provide two files and it will compare them and output the differences.
 *
 * Invoke this class with these environment variables:
 *  - UNIPROT_INPUT_FILE_1 to point to the first input dat file (also accepted as first argument to main).
 *  - UNIPROT_INPUT_FILE_2 to point to the second input dat file (also accepted as second argument to main).
 *  - UNIPROT_OUTPUT_FILE as the output dat file name (also accepted as third argument to main). Default is {UNIPROT_INPUT_FILE_1}-{UNIPROT_INPUT_FILE_2}.out.timestamp
 *
 * Example with bash:
 * $ UNIPROT_INPUT_FILE_1=pre_zfin.dat.a UNIPROT_INPUT_FILE_2=pre_zfin.dat.b UNIPROT_OUTPUT_FILE=pre_zfin.diffs gradle uniprotCompareTask
 *
 */
public class UniProtCompareTask extends AbstractScriptWrapper {
    private String inputFilename1;
    private String inputFilename2;
    private String outputFilename;
    
    private PrintWriter outputWriter;
    
    private Map<String, RichSequence> sequences1 = new HashMap<>();
    private Map<String, RichSequence> sequences2 = new HashMap<>();

    private UniProtDiffSet diffSet = new UniProtDiffSet();

    public UniProtCompareTask(String inputFilename1, String inputFilename2, String outputFilename) {
        this.inputFilename1 = inputFilename1;
        this.inputFilename2 = inputFilename2;
        this.outputFilename = outputFilename;
    }

    public static void main(String[] args) {
        String inputFilename1 = getArgOrEnvironmentVar(args, 0, "UNIPROT_INPUT_FILE_1");
        String inputFilename2 = getArgOrEnvironmentVar(args, 1, "UNIPROT_INPUT_FILE_2");
        String outputFilename = getArgOrEnvironmentVar(args, 2, "OUTPUT_FILE");

        UniProtCompareTask task = new UniProtCompareTask(inputFilename1, inputFilename2, outputFilename);

        try {
            task.runTask();
        } catch (Exception e) {
            System.err.println("Exception Error while running task: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        HibernateUtil.closeSession();
        System.out.println("Task completed successfully.");
        System.exit(0);
    }

    public void runTask() throws IOException, BioException, SQLException {
        initIOFiles();
        initAll();

        System.out.println("Starting to read file: " + inputFilename1);
        populateSequenceMap(getRichStreamReaderForUniprotDatFile(inputFilename1, true), sequences1);
        System.out.println("Finished reading file " + inputFilename1 + ". Found " + sequences1.size() + " entries.");

        System.out.println("Starting to read file: " + inputFilename2);
        populateSequenceMap(getRichStreamReaderForUniprotDatFile(inputFilename2, true), sequences2);
        System.out.println("Finished reading file " + inputFilename2 + ". Found " + sequences2.size() + " entries.");

        System.out.println("Starting to compare files. Writing to file: " + outputFilename);
        populateDiffSetForNewAndRemoved();
        populateDiffSetForChangedRecords();
        populateDates();

        outputWriter.println(UniProtDiffSetSerializer.serializeToString(diffSet));
        outputWriter.close();
    }

    private void populateDates() {
        for(RichSequence seq : sequences1.values()) {
            diffSet.updateLatestDate1(seq);
        }
        for(RichSequence seq : sequences2.values()) {
            diffSet.updateLatestDate2(seq);
        }
    }

    private void populateDiffSetForNewAndRemoved() {
        Collection<String> namesOnlyInSet1 = CollectionUtils.removeAll(sequences1.keySet(), sequences2.keySet());
        Collection<String> namesOnlyInSet2 = CollectionUtils.removeAll(sequences2.keySet(), sequences1.keySet());

        for(String accession : namesOnlyInSet2) {
            RichSequence seq = sequences2.get(accession);
            diffSet.addNewSequence(seq);
        }

        for(String accession : namesOnlyInSet1) {
            RichSequence seq = sequences1.get(accession);
            diffSet.addRemovedSequence(seq);
        }
    }


    private void populateDiffSetForChangedRecords() {
        List<String> sortedCommonList = new ArrayList<>(CollectionUtils.intersection(sequences1.keySet(), sequences2.keySet()));
        Collections.sort(sortedCommonList);

        for(String accession : sortedCommonList) {
            RichSequence seq1 = sequences1.get(accession);
            RichSequence seq2 = sequences2.get(accession);

            RichSequenceDiff diff = RichSequenceDiff.create(seq1, seq2);

            if (!diff.hasChanges()) {
                continue;
            }

            if (!hasChangesWeCareAbout(diff)) {
                continue;
            }

            diffSet.addChangedSequence(diff);
        }

    }

    private boolean hasChangesWeCareAbout(RichSequenceDiff diff) {
        if (diff.hasChangesInDB("RefSeq")) {
            return true;
        }
        if (diff.hasChangesInDB("GeneID")) {
            return true;
        }
        if (diff.hasChangesInDB("ZFIN")) {
            return true;
        }
        return false;
    }


    private void initIOFiles() {
        setOutputStream();
    }

    private void setOutputStream() {
        if (outputFilename == null) {
            return;
        }

        try {
            outputWriter = new PrintWriter(new FileWriter(outputFilename));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void populateSequenceMap(RichStreamReader richStreamReader, Map<String, RichSequence> sequences) throws BioException {
        while (richStreamReader.hasNext()) {
            RichSequence seq = richStreamReader.nextRichSequence();
            sequences.put(seq.getAccession(), seq);
        }
    }

    private void writeOutputFile(List<RichSequence> outputEntries, String outfile) {
        try {
            RichStreamWriter sw = getRichStreamWriterForUniprotDatFile(outfile);
            sw.writeStream(new SequenceListIterator(outputEntries), null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
