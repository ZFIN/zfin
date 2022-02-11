package org.zfin.publication;


import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.root.util.StringUtils;
import org.zfin.nomenclature.repair.NamingIssuesReportRow;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;
import org.zfin.repository.RepositoryFactory;
import org.zfin.util.FileUtil;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Task for finding matches to journal records with missing abbreviations
 * Run with, eg.:
 *      javac -d /tmp/wd -cp ./home/WEB-INF/lib/commons-csv-1.4.jar source/org/zfin/publication/JournalAbbreviationSyncTask.java
 *      java -cp /tmp/wd:./home/WEB-INF/lib/commons-csv-1.4.jar org.zfin.publication.JournalAbbreviationSyncTask J_Entrez journal.csv #journal.csv is export of journal table
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

        String sourceFileName = System.getenv("NCBI_JOURNAL_FILE");

        if (StringUtils.isEmpty(sourceFileName) || !FileUtil.checkFileExists(sourceFileName) ) {
            System.err.println("Provide source file through environment variable NCBI_JOURNAL_FILE\n (can be downloaded from ftp://ftp.ncbi.nih.gov/pubmed/J_Medline.txt, for example)");
            System.err.println("Other environment variable can be used for configuration:\n" +
                                "  CONVERT_INPUT_TO_CSV (set to 'true' and the input source will be converted to csv in /tmp)\n" +
                                "  FORCE_APPLY_FIXES (set to 'true' and the database will be automatically updated)\n");
            System.err.println("Example bash run:\n" +
                    " NCBI_JOURNAL_FILE=/tmp/pubs/J_Entrez FORCE_APPLY_FIXES=true gradle journalAbbreviationSyncTask\n");

            System.exit(1);
        }

        //get pubmed records
        List<Map<String, String>> pubmedRecords = parseFileRecords(sourceFileName);

        //write to a csv export for easier consumption by other programs
        writePubMedCsvExport(pubmedRecords);

        //get local db records
//        List<CSVRecord> dbRecords = parseDbCsvExport(csvFileName);
        List<Journal> journals = RepositoryFactory.getPublicationRepository().getAllJournals();
        LOG.info("Found " + journals.size() + " journals.");

        List<String> fixes = getSqlFixesForJournalsMissingAbbreviations(pubmedRecords, journals);

        applyFixes(fixes);
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
                    //System.err.println("reached max record count: " + recordCount);
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

    private void writePubMedCsvExport(List<Map<String, String>> pubmedRecords) {
        String convertPubmedToCSV = System.getenv("CONVERT_INPUT_TO_CSV");

        if (!"true".equals(convertPubmedToCSV)) {
            LOG.info("Not converting pubmed input to csv");
            return;
        }
        try {
            String timeStamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new java.util.Date());
            String filename = "/tmp/pubmed_journal_export_" + timeStamp + ".csv";
            LOG.info("Converting pubmed input to csv: " + filename);
            FileWriter outputStream = new FileWriter(filename);
            final CSVPrinter printer = CSVFormat.DEFAULT.withHeader("JrId", "MedAbbr", "ISSN (Online)", "JournalTitle", "NlmId", "IsoAbbr", "ISSN (Print)").print(outputStream);
            for(Map<String,String> record : pubmedRecords) {
                printer.printRecord(
                        record.get("JrId"),
                        record.get("MedAbbr"),
                        record.get("ISSN (Online)"),
                        record.get("JournalTitle"),
                        record.get("NlmId"),
                        record.get("IsoAbbr"),
                        record.get("ISSN (Print)"));
            }
            printer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<CSVRecord> parseDbCsvExport(String csvFileName) throws IOException {
        List<CSVRecord> dbRecords = new ArrayList<>();
        Reader in = new FileReader(csvFileName);
        Iterable<CSVRecord> records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(in);
        for (CSVRecord record : records) {
            dbRecords.add(record);
        }
        return dbRecords;
    }

    private List<String> getSqlFixesForJournalsMissingAbbreviations(List<Map<String, String>> pubmedRecords, List<Journal> dbRecords) {
        List<String> sqlFixes = new ArrayList<>();
        for (Journal dbRecord : dbRecords) {
            String id = dbRecord.getZdbID();
            String name = dbRecord.getName();
            String med = dbRecord.getRawMedAbbrev();
            String iso = dbRecord.getRawIsoAbbrev();

            boolean fixNeeded = false;
            if (med == null || med.equals("")) {
                //System.err.print(".");
                fixNeeded = true;
            }
            if (iso == null || iso.equals("")) {
                //System.err.print(",");
                fixNeeded = true;
            }
            if (!fixNeeded) {
                //System.err.print("-");
                continue;
            }
            LOG.info("Journal found with null or empty abbreviations: " + name);
            Map<String, String> match = getMatch(pubmedRecords, name);
            if (match != null) {
                String newIso = match.get("IsoAbbr");
                String newMed = match.get("MedAbbr");
                if ( (newMed == null || newMed.equals("")) && (newIso == null || newIso.equals("")) ) {
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
//                name = name.replaceAll("'", "''");
//                name = name.replaceAll(":", "\\:");
                String fix = "update journal set jrnl_isoabbrev = '" + newIso + "', " +
                         " jrnl_medabbrev = '" + newMed + "' where jrnl_zdb_id='" + id + "' " +
//                         " and jrnl_name = '" + name + "'" +
                        "; ";
                sqlFixes.add(fix);
            } else {
                LOG.info("No match found in NCBI export for journal: " + name);
            }
        }
        return sqlFixes;
    }

    private void applyFixes(List<String> sqlUpdateStatements) {
        Transaction tx;
        Query query;
        String forceApplyFixes = System.getenv("FORCE_APPLY_FIXES");

        outputSqlFile(sqlUpdateStatements);

        if ("true".equals(forceApplyFixes)) {
            LOG.info("IGNORING APPLYING FIXES FLAG");
        } else {
            LOG.info("NOT APPLYING FIXES");
        }
    }

    private void outputSqlFile(List<String> sqlUpdateStatements) {
        BufferedWriter outputWriter = null;
        String outputPath = DEFAULT_OUTPUT_FILE;

        try {
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
    }


    private Map<String, String> getMatch(List<Map<String, String>> pubmedRecords, String name) {
        for(Map<String, String> record : pubmedRecords) {
            String title = record.get("JournalTitle");
            if (title.equals(name) ) {
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

