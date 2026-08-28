package org.zfin.mutant;

import org.apache.log4j.*;
import org.zfin.framework.exec.ExecProcess;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;
import org.zfin.properties.ZfinPropertiesEnum;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class MarkerGoTermEvidenceCleanupTask extends AbstractScriptWrapper {


    public static void main(String[] args) throws IOException, InterruptedException {
        MarkerGoTermEvidenceCleanupTask task = new MarkerGoTermEvidenceCleanupTask();
        task.exec();
    }

    public void exec() throws IOException, InterruptedException {
        String dbQueryFile1 = ZfinPropertiesEnum.SOURCEROOT.value() + "/server_apps/data_transfer/GO/clean_marker_go_term_evidence.sql";

        runSqlFile(dbQueryFile1);
    }

    public void runSqlFile(String dbQueryFile) {

        System.out.println("Running SQL file: " + dbQueryFile);

        // The SQL ends with four \copy statements writing RELATIVE filenames ("for
        // record-keeping" -- what was deduped and what was deleted). Relative means they land in
        // this process's working directory, which under `gradle cleanMarkerGoTermEvidenceDuplicatesTask`
        // is SOURCEROOT -- i.e. the four CSVs would be dropped into the source tree on every run.
        // \copy cannot interpolate psql variables, so the output location has to be the working
        // directory.
        //
        // One property decides both questions, because they were never really two:
        //
        //   -DcleanupCsvDir=<dir>  write the CSVs there. For runs where the cleanup IS the point
        //                          and the files are the record of what it removed -- the GO load
        //                          jobs pass a directory they archive.
        //   (omitted)              do not write them at all. For runs where the cleanup is a step
        //                          inside something bigger and the files are byproducts nobody
        //                          reads: the legacy-vs-unified load comparison, the weekly GAF
        //                          export.
        //
        // Note there is deliberately no way to ask for "write them to the current directory".
        // That was the old default and it is what put four CSVs in the source tree on every run;
        // nothing wants it, so it is not reachable. Callers who want the files say where.
        String requestedDir = System.getProperty("cleanupCsvDir");
        boolean writeCsvs = requestedDir != null && !requestedDir.isBlank();

        File outputDirectory = null;
        if (writeCsvs) {
            outputDirectory = new File(requestedDir);
            if (!outputDirectory.isDirectory() && !outputDirectory.mkdirs()) {
                // Do NOT silently fall back to the current directory -- that is the litter this
                // property exists to prevent, and a caller who named a directory wants that
                // directory. Skip the CSVs and say so loudly.
                System.out.println("WARNING: could not create cleanupCsvDir " + requestedDir
                        + " -- skipping the record-keeping CSVs. The cleanup itself still runs.");
                outputDirectory = null;
                writeCsvs = false;
            }
        }

        if (writeCsvs) {
            System.out.println("Writing record-keeping CSVs to: " + outputDirectory.getAbsolutePath());
        } else if (requestedDir == null || requestedDir.isBlank()) {
            System.out.println("No cleanupCsvDir set -- not writing the record-keeping CSVs. "
                    + "Pass -DcleanupCsvDir=<dir> to keep them.");
        }
        // the unusable-directory case already printed its own WARNING above

        String commandLine = "psql -f " + dbQueryFile + " -h " + ZfinPropertiesEnum.PGHOST.value()  + " -d " + ZfinPropertiesEnum.DB_NAME.value();
        if (!writeCsvs) {
            commandLine += " -v write_csvs=false";
        }
        ExecProcess execProcess = new ExecProcess(commandLine);
        if (outputDirectory != null) {
            execProcess.setWorkingDirectory(outputDirectory);
        }
        int result = -1;
        try {
            result = execProcess.exec();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Finished");

        System.out.println("stdout: \n" + execProcess.getStandardOutput() + "\n\n");
        System.out.println("stderr: \n" + execProcess.getStandardError() + "\n\n");

        System.out.println("result: " + result);
        for(int exitValue : execProcess.getExitValues()) {
            System.out.println("exit code: " + exitValue);
        }

        if (writeCsvs) {
          // psql wrote the \copy output into its working directory, which is no longer the
          // checkout, so say where the files actually are rather than just naming them
          System.out.println("Check the csv files in " + new File(".").getAbsoluteFile().getParent()
              + " for more information: clean_marker_go_term_evidence.csv, to_delete_marker_go_term_evidence.csv, "
              + "tmp_inference_group_member_updates.csv, tmp_mgte_duplicates.csv");
        }
    }

    public MarkerGoTermEvidenceCleanupTask() {
        initAll();
    }


}
