package org.zfin.uniprot;

import org.apache.commons.collections4.CollectionUtils;
import org.biojava.bio.BioException;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.io.RichStreamReader;
import org.biojavax.bio.seq.io.RichStreamWriter;
import org.zfin.framework.HibernateUtil;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.zfin.uniprot.UniProtDatFileTools.getRichStreamReaderForUniprotDatFile;
import static org.zfin.uniprot.UniProtDatFileTools.getRichStreamWriterForUniprotDatFile;

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
    
    private Map<String, RichSequence> sequences1 = new HashMap<>();
    private Map<String, RichSequence> sequences2 = new HashMap<>();

    public UniProtCompareTask(String inputFilename1, String inputFilename2, String outputFilename) {
        this.inputFilename1 = inputFilename1;
        this.inputFilename2 = inputFilename2;
        this.outputFilename = outputFilename;
    }

    public static void main(String[] args) {
        String inputFilename1 = null;
        String inputFilename2 = null;
        String outputFilename = null;
        if (args.length >= 1) {
            inputFilename1 = args[0];
        }
        if (args.length >= 2) {
            inputFilename2 = args[1];
        }
        if (args.length >= 3) {
            outputFilename = args[2];
        }
        UniProtCompareTask task = new UniProtCompareTask(inputFilename1, inputFilename2, outputFilename);

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
        initIOFiles();
        initAll();

        System.out.println("Starting to read file: " + inputFilename1);
        populateSequenceMap(getRichStreamReaderForUniprotDatFile(inputFilename1, true), sequences1);
        System.out.println("Finished reading file " + inputFilename1 + ". Found " + sequences1.size() + " entries.");

        System.out.println("Starting to read file: " + inputFilename2);
        populateSequenceMap(getRichStreamReaderForUniprotDatFile(inputFilename2, true), sequences2);
        System.out.println("Finished reading file " + inputFilename2 + ". Found " + sequences2.size() + " entries.");

        writeAccessionsDiff();
    }

    private void writeAccessionsDiff() {
        Collection<String> namesOnlyInSet1 = CollectionUtils.removeAll(sequences1.keySet(), sequences2.keySet());
        Collection<String> namesOnlyInSet2 = CollectionUtils.removeAll(sequences2.keySet(), sequences1.keySet());

        System.out.println("Found " + namesOnlyInSet1.size() + " entries only in " + inputFilename1);
        System.out.println("============================");
        System.out.println(String.join(", ", namesOnlyInSet1) + "\n");

        System.out.println("Found " + namesOnlyInSet2.size() + " entries only in " + inputFilename2);
        System.out.println("============================");
        System.out.println(String.join(", ", namesOnlyInSet2) + "\n");

    }

    private void initIOFiles() {
        setInputFilename1();
        setInputFilename2();
        setOutputFilename();
    }

    private void populateSequenceMap(RichStreamReader richStreamReader, Map<String, RichSequence> sequences) throws BioException {
        while (richStreamReader.hasNext()) {
            RichSequence seq = richStreamReader.nextRichSequence();
            sequences.put(seq.getName(), seq);
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

    private void setInputFilename1() {
        if (inputFilename1 != null) {
            return;
        }

        inputFilename1 = System.getenv("UNIPROT_INPUT_FILE_1");
        if (inputFilename1 == null) {
            System.err.println("No input file specified. Please set the environment variable UNIPROT_INPUT_FILE_1.");
            System.exit(3);
        }
    }

    private void setInputFilename2() {
        if (inputFilename2 != null) {
            return;
        }

        inputFilename2 = System.getenv("UNIPROT_INPUT_FILE_2");
        if (inputFilename2 == null) {
            System.err.println("No input file specified. Please set the environment variable UNIPROT_INPUT_FILE_2.");
            System.exit(3);
        }
    }

    private void setOutputFilename() {
        if (outputFilename != null) {
            return;
        }

        outputFilename = System.getenv("UNIPROT_OUTPUT_FILE");

        if (outputFilename == null) {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
            outputFilename = inputFilename1 + "-" + inputFilename2 + ".out." + timestamp;
        }
    }

}
