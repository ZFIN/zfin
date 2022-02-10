package org.zfin.publication;


import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.util.*;

/**
 * Task for finding matches to journal records with missing abbreviations
 * Run with, eg.:
 *      javac -d /tmp/wd -cp ./home/WEB-INF/lib/commons-csv-1.4.jar source/org/zfin/publication/JournalAbbreviationSyncTask.java
 *      java -cp /tmp/wd:./home/WEB-INF/lib/commons-csv-1.4.jar org.zfin.publication.JournalAbbreviationSyncTask J_Entrez journal.csv #journal.csv is export of journal table
 */
public class JournalAbbreviationSyncTask  {

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Provide source file for first argument (download from ftp://ftp.ncbi.nih.gov/pubmed/J_Medline.txt)");
            System.err.println("Provide db export second argument (csv export of journal table)");
            System.exit(1);
        }
        String sourceFileName = args[0];
        String csvFileName = args[1];

        //get pubmed records
        List<Map<String, String>> pubmedRecords = parseFileRecords(sourceFileName);

        //write to a csv export for easier consumption by other programs
        writePubMedCsvExport(pubmedRecords);

        //get local db records
        List<CSVRecord> dbRecords = parseDbCsvExport(csvFileName);

        fixMissingAbbreviations(pubmedRecords, dbRecords);
    }

    private static void writePubMedCsvExport(List<Map<String, String>> pubmedRecords) {
        try {
            FileWriter outputStream = new FileWriter("/tmp/pubmed_journal_export.csv");
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

    private static void fixMissingAbbreviations(List<Map<String, String>> pubmedRecords, List<CSVRecord> dbRecords) {
        for (CSVRecord dbRecord : dbRecords) {
            String id = dbRecord.get("jrnl_zdb_id");
            String name = dbRecord.get("jrnl_name");
            String med = dbRecord.get("jrnl_medabbrev");
            String iso = dbRecord.get("jrnl_isoabbrev");
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
            Map<String, String> match = getMatch(pubmedRecords, name);
            if (match != null) {
                String newIso = match.get("IsoAbbr");
                String newMed = match.get("MedAbbr");
                if ( (newMed == null || newMed.equals("")) && (newIso == null || newIso.equals("")) ) {
                    //System.err.println("Found match, but no new abbreviation available");
                    continue;
                }

                //System.err.println("\nSet journal named " + name + " with iso:" + newIso + " med:" + newMed);

                newIso = newIso.replaceAll("'", "''");
                newMed = newMed.replaceAll("'", "''");
                name = name.replaceAll("'", "''");
                System.out.println("\n update journal set jrnl_isoabbrev = '" + newIso + "', " +
                         " jrnl_medabbrev = '" + newMed + "' where jrnl_zdb_id='" + id + "' " +
                         " and jrnl_name = '" + name + "'" +
                        "; ");
            }
        }
    }

    private static Map<String, String> getMatch(List<Map<String, String>> pubmedRecords, String name) {
        for(Map<String, String> record : pubmedRecords) {
            String title = record.get("JournalTitle");
            if (title.equals(name) ) {
                return record;
            }
            if (title.equalsIgnoreCase(name)) {
                //System.err.println("\nCase insensitive match for : '" + title + "' and '" + name + "'");
                return record;
            }
        }
        return null;
    }

    private static List<CSVRecord> parseDbCsvExport(String csvFileName) throws IOException {
        List<CSVRecord> dbRecords = new ArrayList<>();
        Reader in = new FileReader(csvFileName);
        Iterable<CSVRecord> records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(in);
        for (CSVRecord record : records) {
            dbRecords.add(record);
        }
        return dbRecords;
    }

    private static List<Map<String, String>> parseFileRecords(String sourceFileName) {
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
        return records;
    }

}

