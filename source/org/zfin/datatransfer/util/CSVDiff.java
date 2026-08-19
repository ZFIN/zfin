package org.zfin.datatransfer.util;

import lombok.Setter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import static org.zfin.util.ZfinSystemUtils.envTrue;


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
public class CSVDiff {
    private String outputPrefix;
    private String[] keyColumns;
    private String[] ignoreColumns;
    private List<CSVRecord> file1Records = new ArrayList<>();
    private List<CSVRecord> file2Records = new ArrayList<>();
    private List<String> headers = new ArrayList<>();

    //if true, generate the "retained" and "updates_ignored" files. Otherwise, leave them out as they are not important
    @Setter
    private Boolean keepAllFiles = false;

    /**
     * Constructor for the CSV Difference Utility.
     *
     * @param outputPrefix The prefix to use for output files
     * @param keyColumns Array of column names to use as composite key for comparison
     * @param ignoreColumns Array of column names to ignore during comparison (optional)
     */
    public CSVDiff(String outputPrefix, String[] keyColumns, String[] ignoreColumns) {
        this.outputPrefix = outputPrefix;
        this.keyColumns = keyColumns;
        this.ignoreColumns = ignoreColumns != null ? ignoreColumns : new String[0];
    }

    public Map<String, List<CSVRecord>> processToMap(String file1Path, String file2Path) throws IOException {
        Map<String, List<CSVRecord>> results = new HashMap<>();
        int beforeCount = 0;
        int afterCount = 0;
        int retainedCount = 0;
        int ignoredCount1 = 0;
        int ignoredCount2 = 0;
        int updated1Count = 0;
        int updated2Count = 0;
        int deletedCount = 0;
        int addedCount = 0;

        if (envTrue("KEEP_ALL_FILES")) {
            setKeepAllFiles(true);
        }

        // Read the CSV files
        readCSVFiles(file1Path, file2Path);

        beforeCount = file1Records.size();
        afterCount  = file2Records.size();

        // Make working copies of the data
        List<CSVRecord> file1Copy = new ArrayList<>(file1Records);
        List<CSVRecord> file2Copy = new ArrayList<>(file2Records);

        // Find records that are exactly the same (all columns match)
        Map<String, List<CSVRecord>> retainedRecordPairs = findRetainedRecords(file1Copy, file2Copy);
        List<CSVRecord> retainedRecords = retainedRecordPairs.get("file1");
        List<CSVRecord> retainedRecords2 = retainedRecordPairs.get("file2");
        retainedCount = retainedRecords.size();
        if (keepAllFiles) {
            results.put("retained", retainedRecords);
        }

        // Remove retained records from both sets -- each side removes only the instances it
        // actually matched, so duplicate rows are consumed one-for-one rather than wholesale.
        removeRecordsFromLists(retainedRecords, file1Copy, null);
        removeRecordsFromLists(retainedRecords2, null, file2Copy);

        // Find records with changes only in ignored columns
        Map<String, List<CSVRecord>> ignoredUpdatedRecords = findUpdatedRecordsOnlyInIgnoredColumns(file1Copy, file2Copy);
        List<CSVRecord> ignoredUpdated1 = ignoredUpdatedRecords.get("file1");
        List<CSVRecord> ignoredUpdated2 = ignoredUpdatedRecords.get("file2");

        ignoredCount1 = ignoredUpdated1.size();
        ignoredCount2 = ignoredUpdated2.size();
        if (keepAllFiles) {
            results.put("ignoredUpdated1", ignoredUpdated1);
            results.put("ignoredUpdated2", ignoredUpdated2);
        }

        // Remove records with only ignored column changes from both sets
        removeRecordsFromLists(ignoredUpdated1, file1Copy, null);
        removeRecordsFromLists(ignoredUpdated2, null, file2Copy);

        // Find records with matching keys but different values in non-ignored columns
        Map<String, List<CSVRecord>> updatedRecords = findUpdatedRecords(file1Copy, file2Copy);
        List<CSVRecord> updated1 = updatedRecords.get("file1");
        List<CSVRecord> updated2 = updatedRecords.get("file2");
        results.put("updated1", updated1);
        results.put("updated2", updated2);
        updated1Count = updated1.size();
        updated2Count = updated2.size();

        // Remove updated records from both sets
        removeRecordsFromLists(updated1, file1Copy, null);
        removeRecordsFromLists(updated2, null, file2Copy);

        // Write deletes (in file1 but not in file2)
        results.put("deleted", file1Copy);
        deletedCount = file1Copy.size();

        // Write adds (in file2 but not in file1)
        results.put("added", file2Copy);
        addedCount = file2Copy.size();

        // Add summary data to the map:
        try {
            String summaryHeaderData = "beforeCount,afterCount,retainedCount,ignoredCount1,ignoredCount2,updated1Count,updated2Count,deletedCount,addedCount";
            String summaryContent = List.of(beforeCount,afterCount,retainedCount,ignoredCount1,ignoredCount2,updated1Count,updated2Count,deletedCount,addedCount).stream().map(Object::toString).collect(Collectors.joining(","));
            String csvData = summaryHeaderData + "\n" + summaryContent ;
            CSVFormat format = CSVFormat.DEFAULT.withFirstRecordAsHeader();

            Iterable<CSVRecord> summaryRecords = format.parse(new StringReader(csvData));
            CSVRecord summaryRow = summaryRecords.iterator().next();
            List<CSVRecord> summaryRowAsList = new ArrayList<>();
            summaryRowAsList.add(summaryRow);
            results.put("summary", summaryRowAsList);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return results;
    }


    /**
     * Process the two CSV files and generate the difference reports.
     *
     * @param file1Path Path to the first CSV file
     * @param file2Path Path to the second CSV file
     * @return
     * @throws IOException If there is an error reading or writing files
     */
    public List<File> process(String file1Path, String file2Path) throws IOException {
        if (envTrue("KEEP_ALL_FILES")) {
            setKeepAllFiles(true);
        }
        List<File> generatedFiles = new ArrayList<>();

        // Read the CSV files
        readCSVFiles(file1Path, file2Path);

        System.out.println("Loaded " + file1Records.size() + " records from " + file1Path);
        System.out.println("Loaded " + file2Records.size() + " records from " + file2Path);

        // Make working copies of the data
        List<CSVRecord> file1Copy = new ArrayList<>(file1Records);
        List<CSVRecord> file2Copy = new ArrayList<>(file2Records);

        // Find records that are exactly the same (all columns match)
        Map<String, List<CSVRecord>> retainedRecordPairs = findRetainedRecords(file1Copy, file2Copy);
        List<CSVRecord> retainedRecords = retainedRecordPairs.get("file1");
        List<CSVRecord> retainedRecords2 = retainedRecordPairs.get("file2");
        if (keepAllFiles) {
            writeCSVFile(outputPrefix + "_retained.csv", retainedRecords, headers);
            System.out.println("Wrote " + retainedRecords.size() + " records to " + outputPrefix + "_retained.csv");
            generatedFiles.add(new File(outputPrefix + "_retained.csv"));
        } else {
            System.out.println("Ignoring " + retainedRecords.size() + " records. (use KEEP_ALL_FILES to include these rows in output)");
        }

        // Remove retained records from both sets -- each side removes only the instances it
        // actually matched, so duplicate rows are consumed one-for-one rather than wholesale.
        removeRecordsFromLists(retainedRecords, file1Copy, null);
        removeRecordsFromLists(retainedRecords2, null, file2Copy);

        // Find records with changes only in ignored columns
        Map<String, List<CSVRecord>> ignoredUpdatedRecords = findUpdatedRecordsOnlyInIgnoredColumns(file1Copy, file2Copy);
        List<CSVRecord> ignoredUpdated1 = ignoredUpdatedRecords.get("file1");
        List<CSVRecord> ignoredUpdated2 = ignoredUpdatedRecords.get("file2");

        if (keepAllFiles) {
            writeCSVFile(outputPrefix + "_updates_ignored_1.csv", ignoredUpdated1, headers);
            writeCSVFile(outputPrefix + "_updates_ignored_2.csv", ignoredUpdated2, headers);
            System.out.println("Wrote " + ignoredUpdated1.size() + " records to " + outputPrefix + "_updates_ignored_1.csv");
            System.out.println("Wrote " + ignoredUpdated2.size() + " records to " + outputPrefix + "_updates_ignored_2.csv");
            generatedFiles.add(new File(outputPrefix + "_updates_ignored_1.csv"));
            generatedFiles.add(new File(outputPrefix + "_updates_ignored_2.csv"));
        } else {
            System.out.println("Ignoring " + ignoredUpdated1.size() + " records to " + outputPrefix + "_updates_ignored_1.csv. (use KEEP_ALL_FILES to include these rows in output)");
            System.out.println("Ignoring " + ignoredUpdated2.size() + " records to " + outputPrefix + "_updates_ignored_2.csv. (use KEEP_ALL_FILES to include these rows in output)");
        }

        // Remove records with only ignored column changes from both sets
        removeRecordsFromLists(ignoredUpdated1, file1Copy, null);
        removeRecordsFromLists(ignoredUpdated2, null, file2Copy);

        // Find records with matching keys but different values in non-ignored columns
        Map<String, List<CSVRecord>> updatedRecords = findUpdatedRecords(file1Copy, file2Copy);
        List<CSVRecord> updated1 = updatedRecords.get("file1");
        List<CSVRecord> updated2 = updatedRecords.get("file2");

        writeCSVFile(outputPrefix + "_updated_1.csv", updated1, headers);
        writeCSVFile(outputPrefix + "_updated_2.csv", updated2, headers);
        System.out.println("Wrote " + updated1.size() + " records to " + outputPrefix + "_updated_1.csv");
        System.out.println("Wrote " + updated2.size() + " records to " + outputPrefix + "_updated_2.csv");
        generatedFiles.add(new File(outputPrefix + "_updated_1.csv"));
        generatedFiles.add(new File(outputPrefix + "_updated_2.csv"));

        // Remove updated records from both sets
        removeRecordsFromLists(updated1, file1Copy, null);
        removeRecordsFromLists(updated2, null, file2Copy);

        // Write deletes (in file1 but not in file2)
        writeCSVFile(outputPrefix + "_deletes.csv", file1Copy, headers);
        System.out.println("Wrote " + file1Copy.size() + " records to " + outputPrefix + "_deletes.csv");
        generatedFiles.add(new File(outputPrefix + "_deletes.csv"));

        // Write adds (in file2 but not in file1)
        writeCSVFile(outputPrefix + "_adds.csv", file2Copy, headers);
        System.out.println("Wrote " + file2Copy.size() + " records to " + outputPrefix + "_adds.csv");
        generatedFiles.add(new File(outputPrefix + "_adds.csv"));

        return generatedFiles;
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
    private Map<String, List<CSVRecord>> findRetainedRecords(List<CSVRecord> list1, List<CSVRecord> list2) {
        // Full-key equality already means every column matches, so no extra predicate is needed.
        return pairMatchingRecords(list1, list2, true, (record1, record2) -> true);
    }

    /**
     * Group records by key, preserving input order and MULTIPLICITY.
     *
     * ZFIN-8948: the earlier implementation used Map&lt;String, CSVRecord&gt; throughout, i.e. one
     * record per key, so when a key occurred more than once every occurrence but the last was
     * silently discarded. That was invisible while keys happened to be unique, and corrupted
     * results the moment they were not.
     *
     * @param records    The records to group
     * @param useFullKey Whether to key on every column or only the key columns
     * @return key -&gt; all records carrying that key, in input order
     */
    private Map<String, List<CSVRecord>> groupByKey(List<CSVRecord> records, boolean useFullKey) {
        Map<String, List<CSVRecord>> grouped = new LinkedHashMap<>();
        for (CSVRecord record : records) {
            String key = useFullKey ? generateFullRecordKey(record) : generateCompositeKey(record);
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(record);
        }
        return grouped;
    }

    /**
     * Pair up records that share a key and satisfy {@code matches}.
     *
     * Multiplicity-aware: a key appearing n times on the left and m times on the right yields at
     * most min(n, m) pairs, and no record is claimed twice. Records left unclaimed stay in the
     * caller's working lists and fall through to deletes/adds, which is what preserves the
     * accounting invariant
     *   retained + ignoredUpdated + updated + deleted == beforeCount.
     *
     * @return "file1" -&gt; claimed left-hand records, "file2" -&gt; their matched right-hand records,
     *         positionally aligned
     */
    private Map<String, List<CSVRecord>> pairMatchingRecords(List<CSVRecord> list1, List<CSVRecord> list2,
                                                             boolean useFullKey,
                                                             BiPredicate<CSVRecord, CSVRecord> matches) {
        Map<String, List<CSVRecord>> leftByKey = groupByKey(list1, useFullKey);
        Map<String, List<CSVRecord>> rightByKey = groupByKey(list2, useFullKey);
        List<CSVRecord> matched1 = new ArrayList<>();
        List<CSVRecord> matched2 = new ArrayList<>();

        for (Map.Entry<String, List<CSVRecord>> entry : leftByKey.entrySet()) {
            List<CSVRecord> candidates = rightByKey.get(entry.getKey());
            if (candidates == null) {
                continue;
            }
            // Identity set: CSVRecord does not override equals, and two rows in the same key
            // group can be value-identical, so only reference identity distinguishes them.
            Set<CSVRecord> claimed = Collections.newSetFromMap(new IdentityHashMap<>());
            for (CSVRecord record1 : entry.getValue()) {
                for (CSVRecord record2 : candidates) {
                    if (claimed.contains(record2)) {
                        continue;
                    }
                    if (matches.test(record1, record2)) {
                        claimed.add(record2);
                        matched1.add(record1);
                        matched2.add(record2);
                        break;
                    }
                }
            }
        }

        Map<String, List<CSVRecord>> result = new HashMap<>();
        result.put("file1", matched1);
        result.put("file2", matched2);
        return result;
    }

    /**
     * True when at least one non-key, non-ignored column differs — i.e. a genuine update.
     */
    private boolean hasNonIgnoredDifferences(CSVRecord record1, CSVRecord record2) {
        List<String> ignored = Arrays.asList(ignoreColumns);
        for (String column : headers) {
            if (Arrays.asList(keyColumns).contains(column) || ignored.contains(column)) {
                continue;
            }
            if (!Objects.equals(record1.get(column), record2.get(column))) {
                return true;
            }
        }
        return false;
    }

    /**
     * True when every non-key, non-ignored column matches AND at least one ignored column
     * differs — i.e. churn the caller has declared uninteresting.
     */
    private boolean differsOnlyInIgnoredColumns(CSVRecord record1, CSVRecord record2) {
        if (hasNonIgnoredDifferences(record1, record2)) {
            return false;
        }
        for (String column : ignoreColumns) {
            if (!Objects.equals(record1.get(column), record2.get(column))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Find records that have matching keys but different values in non-ignored columns.
     *
     * @param list1 The first list of records
     * @param list2 The second list of records
     * @return A map containing updated records from both files
     */
    private Map<String, List<CSVRecord>> findUpdatedRecords(List<CSVRecord> list1, List<CSVRecord> list2) {
        return pairMatchingRecords(list1, list2, false, this::hasNonIgnoredDifferences);
    }

    /**
     * Remove records from the specified lists.
     *
     * @param recordsToRemove The records to remove
     * @param list1 The first list (can be null)
     * @param list2 The second list (can be null)
     * @param useFullKey Whether to use the full record key or just the composite key
     */
    private void removeRecordsFromLists(List<CSVRecord> recordsToRemove, List<CSVRecord> list1, List<CSVRecord> list2) {
        if (recordsToRemove.isEmpty()) {
            return;
        }

        // ZFIN-8948: remove the exact record INSTANCES that were claimed, not every row sharing
        // their key. The previous key-based removeIf deleted unclaimed members of a duplicate key
        // group too, so rows that had been reported nowhere still vanished from deletes/adds and
        // the totals silently stopped adding up. Identity is required because CSVRecord does not
        // override equals and two rows in a key group can be value-identical.
        Set<CSVRecord> toRemove = Collections.newSetFromMap(new IdentityHashMap<>());
        toRemove.addAll(recordsToRemove);

        if (list1 != null) {
            list1.removeIf(toRemove::contains);
        }

        if (list2 != null) {
            list2.removeIf(toRemove::contains);
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

    private void writeCSVFile(String filePath, List<CSVRecord> records) throws IOException {
        if (records.isEmpty()) {
            System.out.println("No records to write to " + filePath);
            writeCSVFile(filePath, records, Collections.emptyList());
            return;
        }
        List<String> headers = new ArrayList<>(records.get(0).getParser().getHeaderNames());
        writeCSVFile(filePath, records, headers);
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

        CSVDiff utility = new CSVDiff(outputPrefix, keyColumns, ignoreColumns);

        try {
            List<File> outputs = utility.process(file1Path, file2Path);
            // Combine the output CSVs into a single <outputPrefix>.xlsx workbook
            // (one sheet per output file) for a tidy single-file artifact.
            //   CSVDIFF_XLSX       -> build workbook, keep the intermediate CSVs
            //   CSVDIFF_XLSX_ONLY  -> build workbook, delete the intermediate CSVs
            boolean xlsxOnly = envTrue("CSVDIFF_XLSX_ONLY");
            if (xlsxOnly || envTrue("CSVDIFF_XLSX")) {
                writeCombinedWorkbook(outputPrefix, outputs, xlsxOnly);
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Combine the CSV outputs of a diff run into one {@code <outputPrefix>.xlsx}
     * workbook, one sheet per file (named by the file's suffix, e.g. "deletes",
     * "adds", "updated_1").
     *
     * @param deleteOriginals if true, the intermediate CSVs are removed after the
     *                        workbook is written (leaving the xlsx as the sole output).
     */
    private static void writeCombinedWorkbook(String outputPrefix, List<File> outputs, boolean deleteOriginals) {
        if (outputs == null || outputs.isEmpty()) {
            return;
        }
        String prefixName = new File(outputPrefix).getName();
        List<String> sheetNames = new ArrayList<>();
        for (File f : outputs) {
            String label = f.getName();
            if (label.startsWith(prefixName + "_")) {
                label = label.substring(prefixName.length() + 1);
            }
            if (label.endsWith(".csv")) {
                label = label.substring(0, label.length() - 4);
            }
            sheetNames.add(label);
        }
        File xlsx = new File(outputPrefix + ".xlsx");
        new CSVToXLSXConverter().run(xlsx, outputs, sheetNames, deleteOriginals);
        System.out.println("Wrote combined workbook: " + xlsx.getAbsolutePath()
            + (deleteOriginals ? " (intermediate CSVs removed)" : ""));
    }

    /**
     * Find records that have matching keys but only have differences in the ignored columns.
     *
     * @param list1 The first list of records
     * @param list2 The second list of records
     * @return A map containing updated records from both files with only ignored column differences
     */
    private Map<String, List<CSVRecord>> findUpdatedRecordsOnlyInIgnoredColumns(List<CSVRecord> list1, List<CSVRecord> list2) {
        return pairMatchingRecords(list1, list2, false, this::differsOnlyInIgnoredColumns);
    }

    public List<File> writeMapToCSVs(File workingDir, String prefix, Map<String, List<CSVRecord>> beforeAfterComparison) {
        List<File> outputFiles = new ArrayList<>();
        for (Map.Entry<String, List<CSVRecord>> entry : beforeAfterComparison.entrySet()) {
            System.out.println("Writing " + entry.getValue().size() + " records to file for key " + entry.getKey());
            String fileName = prefix + "_" + entry.getKey() + ".csv";
            File outputFile = new File(workingDir, fileName);
            outputFiles.add(outputFile);
            try {
                writeCSVFile(outputFile.getAbsolutePath(), entry.getValue());
                System.out.println("Wrote " + entry.getValue().size() + " records to " + outputFile.getAbsolutePath());
            } catch (IOException e) {
                System.err.println("Error writing file " + outputFile.getAbsolutePath() + ": " + e.getMessage());
            }
        }
        return outputFiles;
    }
}
