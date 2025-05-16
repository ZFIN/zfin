package org.zfin.datatransfer.ncbi;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.util.*;

/**
 * Utility class to compare two CSV files and generate breakdown reports of their differences.
 * command line args are:
 * CSVDifferenceUtility <outputPrefix> <file1Path> <file2Path> <keyColumn1,keyColumn2,...> [ignoreColumn1,ignoreColumn2,...]
 * The CSV files must have the same headers.
 * This class generates the following output files:
 * 1. PREFIX_retained.csv - rows common to both files (exact match on all columns)
 * 2. PREFIX_updates_ignored_1.csv - rows with matching keys but different values, though the values are in ignore columns (from file1)
 * 3. PREFIX_updates_ignored_2.csv - rows with matching keys but different values, though the values are in ignore columns (from file2)
 * 4. PREFIX_updated_1.csv - rows with matching keys but different values (from file1)
 * 5. PREFIX_updated_2.csv - rows with matching keys but different values (from file2)
 * 6. PREFIX_deletes.csv - rows in file1 but not in file2 (based on key columns)
 * 7. PREFIX_adds.csv - rows in file2 but not in file1 (based on key columns)
 *
 * All files exclude entries from earlier steps
 */
public class CSVDifferenceUtility {
    private String outputPrefix;
    private String[] keyColumns;
    private String[] ignoreColumns;
    private List<CSVRecord> file1Records = new ArrayList<>();
    private List<CSVRecord> file2Records = new ArrayList<>();
    private List<String> headers = new ArrayList<>();

    /**
     * Constructor for the CSV Difference Utility.
     *
     * @param outputPrefix The prefix to use for output files
     * @param keyColumns Array of column names to use as composite key for comparison
     * @param ignoreColumns Array of column names to ignore during comparison (optional)
     */
    public CSVDifferenceUtility(String outputPrefix, String[] keyColumns, String[] ignoreColumns) {
        this.outputPrefix = outputPrefix;
        this.keyColumns = keyColumns;
        this.ignoreColumns = ignoreColumns != null ? ignoreColumns : new String[0];
    }

    /**
     * Process the two CSV files and generate the difference reports.
     *
     * @param file1Path Path to the first CSV file
     * @param file2Path Path to the second CSV file
     * @throws IOException If there is an error reading or writing files
     */
    public void process(String file1Path, String file2Path) throws IOException {
        // Read the CSV files
        readCSVFiles(file1Path, file2Path);

        System.out.println("Loaded " + file1Records.size() + " records from " + file1Path);
        System.out.println("Loaded " + file2Records.size() + " records from " + file2Path);

        // Make working copies of the data
        List<CSVRecord> file1Copy = new ArrayList<>(file1Records);
        List<CSVRecord> file2Copy = new ArrayList<>(file2Records);

        // Find records that are exactly the same (all columns match)
        List<CSVRecord> retainedRecords = findRetainedRecords(file1Copy, file2Copy);
        writeCSVFile(outputPrefix + "_retained.csv", retainedRecords, headers);
        System.out.println("Wrote " + retainedRecords.size() + " records to " + outputPrefix + "_retained.csv");

        // Remove retained records from both sets
        removeRecordsFromLists(retainedRecords, file1Copy, file2Copy, true);

        // Find records with changes only in ignored columns
        Map<String, List<CSVRecord>> ignoredUpdatedRecords = findUpdatedRecordsOnlyInIgnoredColumns(file1Copy, file2Copy);
        List<CSVRecord> ignoredUpdated1 = ignoredUpdatedRecords.get("file1");
        List<CSVRecord> ignoredUpdated2 = ignoredUpdatedRecords.get("file2");

        writeCSVFile(outputPrefix + "_updates_ignored_1.csv", ignoredUpdated1, headers);
        writeCSVFile(outputPrefix + "_updates_ignored_2.csv", ignoredUpdated2, headers);
        System.out.println("Wrote " + ignoredUpdated1.size() + " records to " + outputPrefix + "_updates_ignored_1.csv");
        System.out.println("Wrote " + ignoredUpdated2.size() + " records to " + outputPrefix + "_updates_ignored_2.csv");

        // Remove records with only ignored column changes from both sets
        removeRecordsFromLists(ignoredUpdated1, file1Copy, null, false);
        removeRecordsFromLists(ignoredUpdated2, null, file2Copy, false);

        // Find records with matching keys but different values in non-ignored columns
        Map<String, List<CSVRecord>> updatedRecords = findUpdatedRecords(file1Copy, file2Copy);
        List<CSVRecord> updated1 = updatedRecords.get("file1");
        List<CSVRecord> updated2 = updatedRecords.get("file2");

        writeCSVFile(outputPrefix + "_updated_1.csv", updated1, headers);
        writeCSVFile(outputPrefix + "_updated_2.csv", updated2, headers);
        System.out.println("Wrote " + updated1.size() + " records to " + outputPrefix + "_updated_1.csv");
        System.out.println("Wrote " + updated2.size() + " records to " + outputPrefix + "_updated_2.csv");

        // Remove updated records from both sets
        removeRecordsFromLists(updated1, file1Copy, null, false);
        removeRecordsFromLists(updated2, null, file2Copy, false);

        // Write deletes (in file1 but not in file2)
        writeCSVFile(outputPrefix + "_deletes.csv", file1Copy, headers);
        System.out.println("Wrote " + file1Copy.size() + " records to " + outputPrefix + "_deletes.csv");

        // Write adds (in file2 but not in file1)
        writeCSVFile(outputPrefix + "_adds.csv", file2Copy, headers);
        System.out.println("Wrote " + file2Copy.size() + " records to " + outputPrefix + "_adds.csv");
    }

    /**
     * Read the CSV files and store the records and headers.
     *
     * @param file1Path Path to the first CSV file
     * @param file2Path Path to the second CSV file
     * @throws IOException If there is an error reading the files
     */
    private void readCSVFiles(String file1Path, String file2Path) throws IOException {
        // Read file1
        try (Reader reader = new FileReader(file1Path);
             CSVParser csvParser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader)) {

            headers = new ArrayList<>(csvParser.getHeaderNames());
            validateHeaders(headers);

            for (CSVRecord record : csvParser) {
                file1Records.add(record);
            }
        }

        // Read file2
        try (Reader reader = new FileReader(file2Path);
             CSVParser csvParser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader)) {

            List<String> file2Headers = new ArrayList<>(csvParser.getHeaderNames());
            validateHeaders(file2Headers);

            // Ensure headers are identical between files
            if (!headers.equals(file2Headers)) {
                throw new IllegalArgumentException("Headers in both files must be identical");
            }

            for (CSVRecord record : csvParser) {
                file2Records.add(record);
            }
        }
    }

    /**
     * Validate that all key columns exist in the headers.
     *
     * @param fileHeaders The headers from the CSV file
     * @throws IllegalArgumentException If a key column is not found in the headers
     */
    private void validateHeaders(List<String> fileHeaders) {
        for (String keyColumn : keyColumns) {
            if (!fileHeaders.contains(keyColumn)) {
                throw new IllegalArgumentException("Key column '" + keyColumn + "' not found in file headers");
            }
        }

        for (String ignoreColumn : ignoreColumns) {
            if (!fileHeaders.contains(ignoreColumn)) {
                throw new IllegalArgumentException("Ignore column '" + ignoreColumn + "' not found in file headers");
            }
        }
    }

    /**
     * Generates a composite key for a record based on the specified key columns.
     *
     * @param record The CSV record
     * @return A string representing the composite key
     */
    private String generateCompositeKey(CSVRecord record) {
        StringBuilder key = new StringBuilder();
        for (String keyColumn : keyColumns) {
            key.append(record.get(keyColumn)).append("|");
        }
        return key.toString();
    }

    /**
     * Generates a string representation of all values in the record.
     *
     * @param record The CSV record
     * @return A string representing all values in the record
     */
    private String generateFullRecordKey(CSVRecord record) {
        StringBuilder key = new StringBuilder();
        for (String header : headers) {
            key.append(record.get(header)).append("|");
        }
        return key.toString();
    }

    /**
     * Find records that have identical values across all columns.
     *
     * @param list1 The first list of records
     * @param list2 The second list of records
     * @return A list of records that are identical in both files
     */
    private List<CSVRecord> findRetainedRecords(List<CSVRecord> list1, List<CSVRecord> list2) {
        Map<String, CSVRecord> recordsMap = new HashMap<>();
        List<CSVRecord> retained = new ArrayList<>();

        // Create a map of full record keys to records for the second list
        for (CSVRecord record : list2) {
            recordsMap.put(generateFullRecordKey(record), record);
        }

        // Find matches from the first list
        for (CSVRecord record : list1) {
            String fullKey = generateFullRecordKey(record);
            if (recordsMap.containsKey(fullKey)) {
                retained.add(record);
            }
        }

        return retained;
    }

    /**
     * Find records that have matching keys but different values in non-ignored columns.
     *
     * @param list1 The first list of records
     * @param list2 The second list of records
     * @return A map containing updated records from both files
     */
    private Map<String, List<CSVRecord>> findUpdatedRecords(List<CSVRecord> list1, List<CSVRecord> list2) {
        Map<String, CSVRecord> file1KeyMap = new HashMap<>();
        Map<String, CSVRecord> file2KeyMap = new HashMap<>();
        List<CSVRecord> updated1 = new ArrayList<>();
        List<CSVRecord> updated2 = new ArrayList<>();

        // Create maps of composite keys to records
        for (CSVRecord record : list1) {
            file1KeyMap.put(generateCompositeKey(record), record);
        }

        for (CSVRecord record : list2) {
            file2KeyMap.put(generateCompositeKey(record), record);
        }

        // Get the set of columns to compare (all headers except key columns)
        Set<String> comparisonColumns = new HashSet<>(headers);
        for (String keyColumn : keyColumns) {
            comparisonColumns.remove(keyColumn);
        }

        // For each key that exists in both files
        for (String key : file1KeyMap.keySet()) {
            if (file2KeyMap.containsKey(key)) {
                CSVRecord record1 = file1KeyMap.get(key);
                CSVRecord record2 = file2KeyMap.get(key);

                // Check if there are differences in non-key columns
                boolean hasDifferences = false;
                for (String column : comparisonColumns) {
                    if (!Objects.equals(record1.get(column), record2.get(column))) {
                        // If the column is not in the ignore list, it's a meaningful difference
                        if (!Arrays.asList(ignoreColumns).contains(column)) {
                            hasDifferences = true;
                            break;
                        }
                    }
                }

                if (hasDifferences) {
                    updated1.add(record1);
                    updated2.add(record2);
                }
            }
        }

        Map<String, List<CSVRecord>> result = new HashMap<>();
        result.put("file1", updated1);
        result.put("file2", updated2);
        return result;
    }

    /**
     * Remove records from the specified lists.
     *
     * @param recordsToRemove The records to remove
     * @param list1 The first list (can be null)
     * @param list2 The second list (can be null)
     * @param useFullKey Whether to use the full record key or just the composite key
     */
    private void removeRecordsFromLists(List<CSVRecord> recordsToRemove, List<CSVRecord> list1, List<CSVRecord> list2, boolean useFullKey) {
        if (recordsToRemove.isEmpty()) {
            return;
        }

        Set<String> keys = new HashSet<>();
        for (CSVRecord record : recordsToRemove) {
            if (useFullKey) {
                keys.add(generateFullRecordKey(record));
            } else {
                keys.add(generateCompositeKey(record));
            }
        }

        if (list1 != null) {
            list1.removeIf(record -> {
                String key = useFullKey ? generateFullRecordKey(record) : generateCompositeKey(record);
                return keys.contains(key);
            });
        }

        if (list2 != null) {
            list2.removeIf(record -> {
                String key = useFullKey ? generateFullRecordKey(record) : generateCompositeKey(record);
                return keys.contains(key);
            });
        }
    }

    /**
     * Write records to a CSV file.
     *
     * @param filePath The path to the output file
     * @param records The records to write
     * @param headers The headers for the CSV file
     * @throws IOException If there is an error writing the file
     */
    private void writeCSVFile(String filePath, List<CSVRecord> records, List<String> headers) throws IOException {
        try (Writer writer = new FileWriter(filePath);
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(headers.toArray(new String[0])))) {

            for (CSVRecord record : records) {
                List<String> values = new ArrayList<>();
                for (String header : headers) {
                    values.add(record.get(header));
                }
                csvPrinter.printRecord(values);
            }

            csvPrinter.flush();
        }
    }

    /**
     * Main method to demonstrate usage of the CSVDifferenceUtility.
     *
     * @param args Command line arguments: outputPrefix file1Path file2Path keyColumns [ignoreColumns]
     */
    public static void main(String[] args) {
        if (args.length < 4) {
            System.err.println("Usage: CSVDifferenceUtility <outputPrefix> <file1Path> <file2Path> <keyColumn1,keyColumn2,...> [ignoreColumn1,ignoreColumn2,...]");
            System.exit(1);
        }

        String outputPrefix = args[0];
        String file1Path = args[1];
        String file2Path = args[2];
        String[] keyColumns = args[3].split(",");

        String[] ignoreColumns = new String[0];
        if (args.length >= 5) {
            ignoreColumns = args[4].split(",");
        }

        CSVDifferenceUtility utility = new CSVDifferenceUtility(outputPrefix, keyColumns, ignoreColumns);

        try {
            utility.process(file1Path, file2Path);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Find records that have matching keys but only have differences in the ignored columns.
     *
     * @param list1 The first list of records
     * @param list2 The second list of records
     * @return A map containing updated records from both files with only ignored column differences
     */
    private Map<String, List<CSVRecord>> findUpdatedRecordsOnlyInIgnoredColumns(List<CSVRecord> list1, List<CSVRecord> list2) {
        Map<String, CSVRecord> file1KeyMap = new HashMap<>();
        Map<String, CSVRecord> file2KeyMap = new HashMap<>();
        List<CSVRecord> onlyIgnoredUpdates1 = new ArrayList<>();
        List<CSVRecord> onlyIgnoredUpdates2 = new ArrayList<>();

        // Create maps of composite keys to records
        for (CSVRecord record : list1) {
            file1KeyMap.put(generateCompositeKey(record), record);
        }

        for (CSVRecord record : list2) {
            file2KeyMap.put(generateCompositeKey(record), record);
        }

        // Get the set of columns to compare (all headers except key columns and ignore columns)
        Set<String> comparisonColumns = new HashSet<>(headers);
        for (String keyColumn : keyColumns) {
            comparisonColumns.remove(keyColumn);
        }
        for (String ignoreColumn : ignoreColumns) {
            comparisonColumns.remove(ignoreColumn);
        }

        // For each key that exists in both files
        for (String key : file1KeyMap.keySet()) {
            if (file2KeyMap.containsKey(key)) {
                CSVRecord record1 = file1KeyMap.get(key);
                CSVRecord record2 = file2KeyMap.get(key);

                // Check if all non-key, non-ignored columns match
                boolean allComparisonColumnsMatch = true;
                for (String column : comparisonColumns) {
                    if (!Objects.equals(record1.get(column), record2.get(column))) {
                        allComparisonColumnsMatch = false;
                        break;
                    }
                }

                // Check if at least one ignored column is different
                boolean atLeastOneIgnoredColumnDiffers = false;
                for (String column : ignoreColumns) {
                    if (!Objects.equals(record1.get(column), record2.get(column))) {
                        atLeastOneIgnoredColumnDiffers = true;
                        break;
                    }
                }

                // If only ignored columns differ (and at least one does), add to the result
                if (allComparisonColumnsMatch && atLeastOneIgnoredColumnDiffers) {
                    onlyIgnoredUpdates1.add(record1);
                    onlyIgnoredUpdates2.add(record2);
                }
            }
        }

        Map<String, List<CSVRecord>> result = new HashMap<>();
        result.put("file1", onlyIgnoredUpdates1);
        result.put("file2", onlyIgnoredUpdates2);
        return result;
    }

}
