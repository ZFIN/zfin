package org.zfin.sequence;

import org.hibernate.query.Query;
import org.hibernate.Session;
import org.zfin.framework.HibernateUtil;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.zfin.repository.RepositoryFactory.getSequenceRepository;

public class CheckSequenceValidationRulesTask extends AbstractScriptWrapper {

    public static void main(String[] args) throws IOException {
        CheckSequenceValidationRulesTask task = new CheckSequenceValidationRulesTask();
        task.runTask();
    }

    private void runTask() throws IOException {
        initAll();
        List<DBLink> failedSequences = getSequencesThatFailValidation();
        writeReport(failedSequences);
    }

    private static void writeReport(List<DBLink> failedSequences) throws IOException {
        if (!failedSequences.isEmpty()) {
            String outputDir = ".";
            if (System.getenv("ARTIFACTS_DIR") != null) {
                outputDir = System.getenv("ARTIFACTS_DIR");
            }
            File outputReportFile = new File(outputDir, "sequences_failing_validation.csv");
            BufferedWriter reportFileHandle = new BufferedWriter(new java.io.FileWriter(outputReportFile));
            System.out.println("Writing report to " + outputReportFile.getAbsolutePath());

            String header = "accession,zdb_id,linked_zdb_id,info,length";
            System.out.println(header);
            reportFileHandle.write(header + "\n");
            for (DBLink link : failedSequences) {
                String line = link.getAccessionNumber() + "," + link.getZdbID() + "," + link.getDataZdbID() + "," + link.getLinkInfo() + "," + link.getLength();
                System.out.println(line);
                reportFileHandle.write(line + "\n");
            }
            reportFileHandle.close();
        }
    }

    private List<DBLink> getSequencesThatFailValidation() {
        Session session = HibernateUtil.currentSession();

        //get all dblinks that have validation rules
        String hqlString = "from ReferenceDatabase refDb join fetch refDb.validationRules as rule where rule is not null";
        Query query = session.createQuery(hqlString);
        List<ReferenceDatabase> dbs = query.list();

        List<String> failedAccessionIDs = new ArrayList<>();
        List<DBLink> failedLinkAccessions = new ArrayList<>();
        dbs.forEach(db -> {
            List<DBLink> dblinks = getSequenceRepository().getDBLinks(db.getForeignDB().getDbName());
            dblinks.forEach(dblink -> {
                if (!dblink.isValidAccessionFormat() && !failedAccessionIDs.contains(dblink.getZdbID())) {
                    failedLinkAccessions.add(dblink);
                    failedAccessionIDs.add(dblink.getZdbID());
                }
            });
        });

        return failedLinkAccessions;
    }

}
