package org.zfin.uniprot;

import org.biojava.bio.BioException;
import org.biojava.bio.symbol.AlphabetManager;
import org.biojava.bio.symbol.FiniteAlphabet;
import org.biojavax.RankedCrossRef;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.io.RichStreamReader;
import org.biojavax.bio.seq.io.RichStreamWriter;
import org.zfin.framework.HibernateUtil;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;

import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.zfin.uniprot.UniProtDatFileReader.getRichStreamReaderForUniprotDatFile;

/**
 * This class is used to slim down the uniprot load files.
 * The first step is to filter out all the records that don't apply to zebrafish.
 * The second step is to filter out all the records that don't have one of the following DR lines:
 *
 * 1. ZFIN  (eg. DR   ZFIN; ZDB-GENE-141216-405; si:ch211-193i15.1.)
 * 2. GeneID (NCBI) (eg. DR   GeneID; 100537590; -.)
 * 3. RefSeq (eg. DR   RefSeq; XP_003199568.1; XM_003199520.5.
 *
 * Invoke this class with an environment variable (UNIPROT_INPUT_FILE) to point to the input dat file.
 *  and an environment variable (UNIPROT_OUTPUT_FILE) to point to the output dat file.
 *
 * Example with bash:
 * $ UNIPROT_INPUT_FILE=pre_zfin.dat UNIPROT_OUTPUT_FILE=pre_zfin.filtered.dat gradle uniProtFilterTask
 *
 */
public class UniProtFilterTask extends AbstractScriptWrapper {
    public String inputFilename;
    public String outputFilename;

    public UniProtFilterTask(String inputFilename, String outputFilename) {
        this.inputFilename = inputFilename;
        this.outputFilename = outputFilename;
    }

    public static void main(String[] args) {
        String inputFilename = null;
        String outputFilename = null;
        if (args.length >= 1) {
            inputFilename = args[0];
        }
        if (args.length >= 2) {
            outputFilename = args[1];
        }
        UniProtFilterTask task = new UniProtFilterTask(inputFilename, outputFilename);

        try {
            task.runTask();
        } catch (IOException e) {
            System.err.println("IOException Error while running task: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } catch (BioException e) {
            System.err.println("BioException Error while running task: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        } catch (SQLException e) {
            System.err.println("SQLException Error while running task: " + e.getMessage());
            e.printStackTrace();
            System.exit(3);
        }

        HibernateUtil.closeSession();
        System.out.println("Task completed successfully.");
        System.exit(0);
    }

    public void runTask() throws IOException, BioException, SQLException {
        String infile = getInputFilename();
        String outfile = getOutputFilename();

        initAll();

        RichStreamReader sr = getRichStreamReaderForUniprotDatFile(infile);

        System.out.println("Starting to read file: " + infile);
        List<RichSequence> outputEntries = transformEntriesTest(sr);
        System.out.println("Finished reading file: " + outputEntries.size());

        System.out.println("Starting to write file: " + outfile);
        writeOutputFile(outputEntries, outfile);

    }

    private void writeOutputFile(List<RichSequence> outputEntries, String outfile) {
        try {
            FileOutputStream fos = new FileOutputStream(outfile);
            UniProtFormatZFIN format = new UniProtFormatZFIN();

            FiniteAlphabet alphabet = (FiniteAlphabet) AlphabetManager.alphabetForName("PROTEIN");
            format.setOverrideAlphabet(alphabet);

            RichStreamWriter sw = new RichStreamWriter(fos, format);
            sw.writeStream(new SequenceListIterator(outputEntries), null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private String getInputFilename() {
        if (inputFilename != null) {
            return inputFilename;
        }

        inputFilename = System.getenv("UNIPROT_INPUT_FILE");
        if (inputFilename == null) {
            System.err.println("No input file specified. Please set the environment variable UNIPROT_INPUT_FILE.");
            System.exit(3);
        }
        return inputFilename;
    }

    private String getOutputFilename() {
        if (outputFilename != null) {
            return outputFilename;
        }

        outputFilename = System.getenv("UNIPROT_OUTPUT_FILE");
        if (outputFilename == null) {
            System.err.println("No output file specified. Please set the environment variable UNIPROT_OUTPUT_FILE.");
            System.exit(3);
        }
        return outputFilename;
    }

    private List<RichSequence> transformEntriesTest(RichStreamReader richStreamReader) throws BioException {
        List<String> xrefsToKeep = List.of("ZFIN", "GeneID", "RefSeq", "EMBL", "GO", "InterPro", "Pfam", "PROSITE", "PDB", "Ensembl");

        List<RichSequence> uniProtSequences = new ArrayList<>();
        while (richStreamReader.hasNext()) {
            RichSequence seq = richStreamReader.nextRichSequence();

            Set<RankedCrossRef> uniProtSequenceSet = new HashSet<>(seq.getRankedCrossRefs()
                    .stream()
                    .filter(rankedXref -> xrefsToKeep.contains(rankedXref.getCrossRef().getDbname()))
                    .toList());

            seq.setRankedCrossRefs(uniProtSequenceSet);
            uniProtSequences.add(seq);
        }
        return uniProtSequences;
    }

}
