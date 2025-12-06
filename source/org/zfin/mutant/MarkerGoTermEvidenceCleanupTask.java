package org.zfin.mutant;

import org.apache.log4j.*;
import org.zfin.framework.exec.ExecProcess;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;
import org.zfin.properties.ZfinPropertiesEnum;

import java.io.BufferedWriter;
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

        String commandLine = "psql -f " + dbQueryFile + " -h " + ZfinPropertiesEnum.PGHOST.value()  + " -d " + ZfinPropertiesEnum.DB_NAME.value();
        ExecProcess execProcess = new ExecProcess(commandLine);
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

        System.out.println("Check the csv files for more information: clean_marker_go_term_evidence.csv, to_delete_marker_go_term_evidence.csv, tmp_inference_group_member_updates.csv" );
    }

    public MarkerGoTermEvidenceCleanupTask() {
        initAll();
    }


}
