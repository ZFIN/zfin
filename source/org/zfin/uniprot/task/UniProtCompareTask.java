package org.zfin.uniprot.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.biojava.bio.BioException;
import org.zfin.framework.HibernateUtil;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.uniprot.adapter.RichSequenceAdapter;
import org.zfin.uniprot.diff.RichSequenceDiff;
import org.zfin.uniprot.diff.UniProtDiffSet;
import org.zfin.uniprot.dto.UniProtDiffSetDTO;

import java.io.*;
import java.sql.SQLException;
import java.util.*;

import static org.zfin.uniprot.UniProtTools.*;
import static org.zfin.uniprot.datfiles.DatFileReader.getMapOfAccessionsToSequencesFromStreamReader;
import static org.zfin.uniprot.datfiles.DatFileReader.getRichStreamReaderForUniprotDatFile;

/**
 * This class is used to compare uniprot load files. Provide two files and it will compare them and output the differences.
 *
 * Invoke this class with these environment variables:
 *  - UNIPROT_INPUT_FILE_1 to point to the first input dat file (also accepted as first argument to main).
 *  - UNIPROT_INPUT_FILE_2 to point to the second input dat file (also accepted as second argument to main).
 *  - UNIPROT_OUTPUT_FILE as the output dat file name (also accepted as third argument to main). Default is {UNIPROT_INPUT_FILE_1}-{UNIPROT_INPUT_FILE_2}.out.timestamp
 *
 * In addition to UNIPROT_OUTPUT_FILE, this generates a report file named UNIPROT_OUTPUT_FILE.report.html (ignoring original extension)
 *
 * Example with bash:
 * $ UNIPROT_INPUT_FILE_1=pre_zfin.dat.a UNIPROT_INPUT_FILE_2=pre_zfin.dat.b UNIPROT_OUTPUT_FILE=pre_zfin.diffs.json gradle uniprotCompareTask
 *
 */
@Log4j2
public class UniProtCompareTask extends AbstractScriptWrapper {
    private final String inputFilename1;
    private final String inputFilename2;
    private final String outputFilename;
    
    private PrintWriter outputWriter;
    
    private Map<String, RichSequenceAdapter> sequences1 = new HashMap<>();
    private Map<String, RichSequenceAdapter> sequences2 = new HashMap<>();

    private final UniProtDiffSet diffSet = new UniProtDiffSet();

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
            log.error("Exception Error while running task: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        HibernateUtil.closeSession();
        log.debug("Task completed successfully.");
        System.exit(0);
    }

    public void runTask() throws IOException, BioException, SQLException {
        initIOFiles();
        initAll();

        log.debug("Starting to read file: " + inputFilename1);
        sequences1 = getMapOfAccessionsToSequencesFromStreamReader(getRichStreamReaderForUniprotDatFile(inputFilename1, true));
        log.debug("Finished reading file " + inputFilename1 + ". Found " + sequences1.size() + " entries.");

        log.debug("Starting to read file: " + inputFilename2);
        sequences2 = getMapOfAccessionsToSequencesFromStreamReader(getRichStreamReaderForUniprotDatFile(inputFilename2, true));
        log.debug("Finished reading file " + inputFilename2 + ". Found " + sequences2.size() + " entries.");

        log.debug("Starting to compare files. Writing to file: " + outputFilename);
        populateDiffSetForNewAndRemoved();
        populateDiffSetForChangedRecords();
        populateDates();

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(UniProtDiffSetDTO.from(diffSet));

        outputWriter.println(json);
        outputWriter.close();

        writeOutputReportFile();
    }

    private void populateDates() {
        for(RichSequenceAdapter seq : sequences1.values()) {
            diffSet.updateLatestDate1(seq);
        }
        for(RichSequenceAdapter seq : sequences2.values()) {
            diffSet.updateLatestDate2(seq);
        }
    }

    private void populateDiffSetForNewAndRemoved() {
        Collection<String> namesOnlyInSet1 = CollectionUtils.removeAll(sequences1.keySet(), sequences2.keySet());
        Collection<String> namesOnlyInSet2 = CollectionUtils.removeAll(sequences2.keySet(), sequences1.keySet());

        for(String accession : namesOnlyInSet2) {
            RichSequenceAdapter seq = sequences2.get(accession);
            diffSet.addNewSequence(seq);
        }

        for(String accession : namesOnlyInSet1) {
            RichSequenceAdapter seq = sequences1.get(accession);
            diffSet.addRemovedSequence(seq);
        }
    }


    private void populateDiffSetForChangedRecords() {
        List<String> sortedCommonList = new ArrayList<>(CollectionUtils.intersection(sequences1.keySet(), sequences2.keySet()));
        Collections.sort(sortedCommonList);

        for(String accession : sortedCommonList) {
            RichSequenceAdapter seq1 = sequences1.get(accession);
            RichSequenceAdapter seq2 = sequences2.get(accession);

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
        if (diff.hasChangesInDB("RefSeq") ||
            diff.hasChangesInDB("ZFIN") ||
            diff.hasChangesInDB("GeneID")) {
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

    private void writeOutputReportFile() {
        if (outputFilename == null) {
            return;
        }

        //remove extension
        String outputFilenameWithoutExtension = !outputFilename.contains(".") ?
                outputFilename :
                outputFilename.substring(0, outputFilename.lastIndexOf("."));

        String reportfile = outputFilenameWithoutExtension + ".report.html";
        log.debug("Creating report file: " + reportfile);
        try {
            String outfileContents = FileUtils.readFileToString(new File(outputFilename));
            String template = ZfinPropertiesEnum.SOURCEROOT.value() + "/home/uniprot/uniprot-diff-report.html";
            String templateContents = FileUtils.readFileToString(new File(template));
            String filledTemplate = templateContents.replace("JSON_GOES_HERE", outfileContents);
            FileUtils.writeStringToFile(new File(reportfile), filledTemplate);
        } catch (IOException e) {
            System.err.println("Error creating report (" + reportfile + ") from template (" + outputFilename + ")\n" + e.getMessage());
        }
    }

}
