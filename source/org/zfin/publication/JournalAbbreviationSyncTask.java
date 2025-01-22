package org.zfin.publication;


import org.hibernate.Session;
import org.hibernate.Transaction;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.root.util.StringUtils;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;
import org.zfin.repository.RepositoryFactory;
import org.zfin.util.FileUtil;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;

/**
 * Task for finding matches to journal records with missing abbreviations
 * Requires an input file from NCBI: (eg. ftp://ftp.ncbi.nlm.nih.gov/pubmed/J_Medline.txt)
 * Can be run from Jenkins.
 */
public class JournalAbbreviationSyncTask extends AbstractScriptWrapper {

    public static final String DEFAULT_OUTPUT_FILE = "JournalAbbreviationSync.sql";

    public static void main(String[] args) throws IOException {
        JournalAbbreviationSyncTask task = new JournalAbbreviationSyncTask();
        task.runTask();
        System.exit(0);
    }

    public void runTask() {
        initAll();
        String sourceFileName = determineInputFile();

        //get pubmed records
        List<Map<String, String>> pubmedRecords = parseFileRecords(sourceFileName);

        //get local db records
        List<Journal> journals = RepositoryFactory.getPublicationRepository().getAllJournals();
        LOG.info("Found " + journals.size() + " journals.");

        List<String> fixes = getFixesForJournalsMissingAbbreviations(pubmedRecords, journals);
        outputSqlFile(fixes);

        if ("true".equals(System.getProperty("executeSql"))) {
            executeSqlUpdates(fixes);
        }
    }

    private String determineInputFile() {
        String sourceFileName = System.getenv("NCBI_JOURNAL_FILE");
        String uploadedJenkinsFile = System.getenv("NCBI_FILE_UPLOAD");
        String jenkinsWorkspace =  System.getenv("WORKSPACE");
        if (StringUtils.isNotEmpty(uploadedJenkinsFile)
                && StringUtils.isNotEmpty(jenkinsWorkspace)) {
            sourceFileName = Paths.get(jenkinsWorkspace, "NCBI_FILE_UPLOAD").toString();
        }

        if (StringUtils.isEmpty(sourceFileName) || !FileUtil.checkFileExists(sourceFileName) ) {
            System.err.println("Provide source file through environment variable NCBI_JOURNAL_FILE\n (can be downloaded from ftp://ftp.ncbi.nlm.nih.gov/pubmed/J_Medline.txt, for example)");
            System.err.println("The file can also be provided through the Jenkins upload parameter (NCBI_FILE_UPLOAD) if run as jenkins job.");
            System.err.println("Example bash run:\n" +
                    " NCBI_JOURNAL_FILE=/tmp/pubs/J_Entrez gradle journalAbbreviationSync\n");
            System.exit(1);
        }

        return sourceFileName;
    }

    private List<Map<String, String>> parseFileRecords(String sourceFileName) {
        BufferedReader bufferedReader = null;
        List<Map<String, String>> records = new ArrayList<>();
        Set<String> headers = new HashSet<>();

        int max = Integer.MAX_VALUE;
        int recordCount = 0;
        String recordSeparator = "--------------------------------------------------------";
        Map<String, String> currentRecord = null;

        try {
            String currentLine;
            bufferedReader = new BufferedReader(new FileReader(sourceFileName));
            while ((currentLine = bufferedReader.readLine()) != null) {

                //each match of record separator
                if (currentLine.equals(recordSeparator)) {
                    recordCount++;
                    if (currentRecord != null) {
                        records.add(currentRecord);
                    }
                    currentRecord = new HashMap<>();
                    continue;
                }

                String[] keyValuePair = currentLine.split(":", 2);
                if (keyValuePair.length != 2) {
                    //System.err.println("No ':' found in record");
                    System.exit(2);
                }

                String key = keyValuePair[0].trim();
                String value = keyValuePair[1].trim();

                if (currentRecord == null) {
                    System.err.println("Error in input file. Should have first line of hyphens for record separator.");
                    System.exit(5);
                }
                currentRecord.put(key, value);

                headers.add(key);

                if (recordCount > max) {
                    System.err.println("reached max record count: " + recordCount);
                    System.exit(1);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        LOG.info("Parsed " + recordCount + " journal records from NCBI export file: " + sourceFileName);
        return records;
    }

    private List<String> getFixesForJournalsMissingAbbreviations(List<Map<String, String>> pubmedRecords, List<Journal> dbRecords) {

        Session session = HibernateUtil.currentSession();
        List<String> sqlFixes = new ArrayList<>();
        for (Journal dbRecord : dbRecords) {
            String id = dbRecord.getZdbID();
            String name = dbRecord.getName();
            String med = dbRecord.getRawMedAbbrev();
            String iso = dbRecord.getRawIsoAbbrev();

            boolean fixNeeded = StringUtils.isEmpty(med) || StringUtils.isEmpty(iso);
            if (!fixNeeded) {
                continue;
            }
            LOG.info("Journal found with null or empty abbreviations: " + name);
            Map<String, String> match = getMatch(pubmedRecords, dbRecord);
            if (match == null) {
                LOG.info("No match found in NCBI export for journal: " + name);
            } else {
                String newIso = match.get("IsoAbbr");
                String newMed = match.get("MedAbbr");
                if ( StringUtils.isEmpty(newMed) && StringUtils.isEmpty(newIso) ) {
                    LOG.info("Found match, but no new abbreviation available");
                    continue;
                }

                LOG.info("Set " + id + ": '" + name + "' journal name with iso: '" + newIso + "', med: '" + newMed + "'");
                assert newMed != null;
                assert newIso != null;

                newIso = newIso.replaceAll("'", "''");
                newIso = newIso.replaceAll(":", "\\:");
                newMed = newMed.replaceAll("'", "''");
                newMed = newMed.replaceAll(":", "\\:");
                String fix = "update journal set jrnl_isoabbrev = '" + newIso + "', " +
                         " jrnl_medabbrev = '" + newMed + "' where jrnl_zdb_id='" + id + "' ; ";
                sqlFixes.add(fix);
            }
        }
        return sqlFixes;
    }

    private void outputSqlFile(List<String> sqlUpdateStatements) {
        BufferedWriter outputWriter = null;
        String outputPath = System.getenv("OUTPUT_SQL_FILE");
        if (StringUtils.isEmpty(outputPath)) {
            outputPath = DEFAULT_OUTPUT_FILE;
        }

        try {
            LOG.info("Writing SQL fixes to " + outputPath);
            outputWriter = new BufferedWriter(new FileWriter(outputPath));
            for (String sql : sqlUpdateStatements) {
                outputWriter.write(sql + "\n");
            }
        } catch (IOException ioe) {
            LOG.error("Error writing to file: " + outputPath);
        } finally {
            try {
                if (outputWriter != null) {
                    outputWriter.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        LOG.info("Wrote " + sqlUpdateStatements.size() + " update statements to sql file");
    }

    private void executeSqlUpdates(List<String> fixes) {
        Session session = HibernateUtil.currentSession();
        Transaction transaction = session.getTransaction();
        transaction.begin();
        for(String fix : fixes) {
            LOG.info("Executing: " + fix);
            session.createNativeQuery(fix).executeUpdate();
        }
        transaction.commit();
    }

    private Map<String, String> getMatch(List<Map<String, String>> pubmedRecords, Journal journal) {
        String name = journal.getName();
        for(Map<String, String> record : pubmedRecords) {
            String title = record.get("JournalTitle");
            String printIssn = record.get("ISSN (Print)");
            String onlineIssn = record.get("ISSN (Online)");
            if (StringUtils.isNotEmpty(printIssn) && StringUtils.equals(printIssn, journal.getPrintIssn())) {
                LOG.info("Print ISSN match for : '" + title + "' and '" + printIssn + "'");
                return record;
            }
            if (StringUtils.isNotEmpty(onlineIssn) && StringUtils.equals(onlineIssn, journal.getOnlineIssn())) {
                LOG.info("Online ISSN match for : '" + title + "' and '" + onlineIssn + "'");
                return record;
            }
            if (title.equals(name) ) {
                LOG.info("Title match for : '" + title + "' and '" + name + "'");
                return record;
            }
            if (title.equalsIgnoreCase(name)) {
                LOG.info("Case insensitive match for : '" + title + "' and '" + name + "'");
                return record;
            }
        }
        return null;
    }

}

