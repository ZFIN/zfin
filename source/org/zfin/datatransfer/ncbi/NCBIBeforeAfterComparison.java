package org.zfin.datatransfer.ncbi;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
/**
 * TODO!!!!!!!!!!!!

I'd like to generalize this class to be a utility that gives a breakdown of a difference between two CSV files
The CSV files must have the same headers.
In addition to the csv files, you must provide a PREFIX and a set of keys (comma separated) to use as a unique identifier (like a composite key).
It will then break down the files into smaller files:

 1. PREFIX_retained.csv (contains all rows that are common to the 2 files based on ALL headers)
 2. PREFIX_updated_1.csv (contains all rows that are common based on the provided unique id headers, but other columns are changed--based on file1)
 3. PREFIX_updated_2.csv (contains all rows that are common based on the provided unique id headers, but other columns are changed--based on file2)
 4. PREFIX_deletes.csv (contains all rows that were in file1, but not file2)
 5. PREFIX_adds.csv (contains all rows that were in file2, but not file1)

 *
 */


/**
 * Utility class to compare NCBI dblink data before and after updates.
 * This class reads data from before_dblink.csv and after_dblink.csv files,
 * compares them, and generates various reports based on differences.
 */
public class NCBIBeforeAfterComparison {
    private static String dirLocation;
    private List<DblinkRecord> beforeData = new ArrayList<>();
    private List<DblinkRecord> afterData = new ArrayList<>();
    private static final String[] HEADERS = {
            "dblink_linked_recid", "dblink_acc_num", "dblink_info", "dblink_zdb_id",
            "dblink_acc_num_display", "dblink_length", "dblink_fdbcont_zdb_id"
    };

    /**
     * Represents a single database link record from the CSV file.
     */
    public static class DblinkRecord {
        private String fullMatchKey;
        private String matchKeyWithLength;
        private String semiMatchKey;
        private String linkedRecId;
        private String accNum;
        private String info;
        private String zdbId;
        private String accNumDisplay;
        private String length;
        private String fdbcontZdbId;
        public List<String> recattribs = new ArrayList<>();

        public DblinkRecord(String linkedRecId, String accNum, String info, String zdbId,
                            String accNumDisplay, String length, String fdbcontZdbId, List<String> strings) {
            this.linkedRecId = linkedRecId;
            this.accNum = accNum;
            this.info = info;
            this.zdbId = zdbId;
            this.accNumDisplay = accNumDisplay;
            this.length = length;
            this.fdbcontZdbId = fdbcontZdbId;
            this.recattribs = strings;

            this.semiMatchKey = linkedRecId + accNum + fdbcontZdbId;
            this.matchKeyWithLength = semiMatchKey + (length.toString());
            this.fullMatchKey = matchKeyWithLength + String.join("|", recattribs);
        }

        /**
         * Convert record to CSV line.
         */
        public String toCSVLine() {
            String[] values = {linkedRecId, accNum, info, zdbId, accNumDisplay, length, fdbcontZdbId};
            for (int i = 0; i < values.length; i++) {
                String value = values[i] != null ? values[i] : "";
                // Escape commas and quotes
                if (value.contains(",") || value.contains("\"")) {
                    value = "\"" + value.replace("\"", "\"\"") + "\"";
                }
                values[i] = value;
            }
            return String.join(",", values);
        }

        /**
         * Check equality based on zdbId only.
         */
        public boolean matchesZdbId(DblinkRecord other) {
            return Objects.equals(this.zdbId, other.zdbId);
        }

        /**
         * Check equality based on linkedRecId, accNum, fdbcontZdbId, and length.
         */
        public boolean matchesRetained(DblinkRecord other) {
            return Objects.equals(this.linkedRecId, other.linkedRecId) &&
                    Objects.equals(this.accNum, other.accNum) &&
                    Objects.equals(this.fdbcontZdbId, other.fdbcontZdbId) &&
                    Objects.equals(this.length, other.length);
        }

        /**
         * Check equality based on linkedRecId, accNum, and fdbcontZdbId.
         */
        public boolean matchesUpdatedLength(DblinkRecord other) {
            return Objects.equals(this.linkedRecId, other.linkedRecId) &&
                    Objects.equals(this.accNum, other.accNum) &&
                    Objects.equals(this.fdbcontZdbId, other.fdbcontZdbId);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DblinkRecord that = (DblinkRecord) o;
            return Objects.equals(linkedRecId, that.linkedRecId) &&
                    Objects.equals(accNum, that.accNum) &&
                    Objects.equals(info, that.info) &&
                    Objects.equals(zdbId, that.zdbId) &&
                    Objects.equals(accNumDisplay, that.accNumDisplay) &&
                    Objects.equals(length, that.length) &&
                    Objects.equals(fdbcontZdbId, that.fdbcontZdbId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(linkedRecId, accNum, info, zdbId, accNumDisplay, length, fdbcontZdbId);
        }
    }
    public static class RecattribRecordsList {
        public Map<String, List<String>> attributionsByDBlink = new LinkedHashMap<>();

        public RecattribRecordsList(String beforeRecattribFilePath) {
            parseFile(beforeRecattribFilePath);
        }

        public void parseFile(String filePath) {
            readCSVFile(filePath);
        }
        public String[] parseLine(String line) {
            return line.split(",", -1);
        }
        private void readCSVFile(String filePath) {
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                // Skip header line
                String headerLine = reader.readLine();
                if (headerLine == null) {
                    throw new RuntimeException("Empty CSV file: " + filePath);
                }

                String line;
                while ((line = reader.readLine()) != null) {
                    try {
                        String[] record = parseLine(line);
                        String dblinkId = record[1];
                        String sourceId = record[2];
                        this.add(dblinkId, sourceId);
                    } catch (IllegalArgumentException e) {
                        System.err.println("Warning: " + e.getMessage());
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private void add(String dblinkId, String sourceId) {
            List<String> existingList = this.attributionsByDBlink.get(dblinkId);
            if (existingList == null) {
                existingList = new ArrayList<>();
            }
            existingList.add(sourceId);
            this.attributionsByDBlink.put(dblinkId, existingList);
        }
        private List<String> get(String dblinkId) {
            if (!attributionsByDBlink.containsKey(dblinkId)) {
                return Collections.emptyList();
            }
            return this.attributionsByDBlink.get(dblinkId).stream().sorted().collect(Collectors.toList());
        }
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: NCBIBeforeAfterComparison <directory>");
            System.exit(1);
        }
        dirLocation = args[0];
        NCBIBeforeAfterComparison tester = new NCBIBeforeAfterComparison();
        tester.init();
        tester.run();
    }

    /**
     * Initialize the comparison by loading data from CSV files.
     */
    private void init() {
        // Read the before and after CSV files
        String beforeFilePath = Paths.get(dirLocation, "before_db_link.csv").toString();
        String afterFilePath = Paths.get(dirLocation, "after_db_link.csv").toString();
        String beforeRecattribFilePath = Paths.get(dirLocation, "before_recattrib.csv").toString();
        String afterRecattribFilePath = Paths.get(dirLocation, "after_recattrib.csv").toString();

        try {
            RecattribRecordsList beforeRecattrib = new RecattribRecordsList(beforeRecattribFilePath);
            RecattribRecordsList afterRecattrib = new RecattribRecordsList(afterRecattribFilePath);

            beforeData = readCSVFile(beforeFilePath, beforeRecattrib);
            afterData = readCSVFile(afterFilePath, afterRecattrib);
            System.out.println("Loaded " + beforeData.size() + " records from before_dblink.csv");
            System.out.println("Loaded " + afterData.size() + " records from after_dblink.csv");
        } catch (IOException e) {
            System.err.println("Error reading input files: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Run the comparison process and generate reports.
     */
    private void run() {
        // Create copies of data for processing
        List<DblinkRecord> beforeCopy = new ArrayList<>(beforeData);
        List<DblinkRecord> afterCopy = new ArrayList<>(afterData);

        // Step 1: Find and remove common items based on dblink_zdb_id
        int numDeletes = deleteCommonItemsByZdbId(beforeCopy, afterCopy);

        System.out.println("Removed " + numDeletes + " exact matches based on dblink_zdb_id");

        // Step 2: Find retained items (in both before and after based on all attributes beside ID)
        List<DblinkRecord> retainedItems = findRetainedItems(beforeCopy, afterCopy);
        writeCSVFile(Paths.get(dirLocation, "retained.csv").toString(), retainedItems);
        System.out.println("Wrote " + retainedItems.size() + " records to retained.csv");
        System.out.println("Retained means they are identical except for dblink_zdb_id, and info");

        // Step 3: Remove retained items from both sets
        removeRetainedItemsFromLists(retainedItems, beforeCopy, afterCopy);
        System.out.println("Removed retained items from data set -- remaining:" + beforeCopy.size() + "," + afterCopy.size());

        // Step 4: Find items with updated length
        List<DblinkRecord> updatedLengthItems = findUpdatedLengthItems(beforeCopy, afterCopy);
        writeCSVFile(Paths.get(dirLocation, "updated_length.csv").toString(), updatedLengthItems);
        System.out.println("Wrote " + updatedLengthItems.size() + " records to updated_length.csv");

        // Step 6: Write before_only.csv and after_only.csv
        writeCSVFile(Paths.get(dirLocation, "before_only.csv").toString(), beforeCopy);
        writeCSVFile(Paths.get(dirLocation, "after_only.csv").toString(), afterCopy);
        System.out.println("Wrote " + beforeCopy.size() + " records to before_only.csv");
        System.out.println("Wrote " + afterCopy.size() + " records to after_only.csv");
    }

    private static void removeRetainedItemsFromLists(List<DblinkRecord> toRemove, List<DblinkRecord> list1, List<DblinkRecord> list2) {
        HashSet<String> matchKeys = new HashSet<String>();
        matchKeys.addAll(toRemove.stream().map(d -> d.fullMatchKey).collect(Collectors.toSet()));

        list1.removeIf(d -> matchKeys.contains(d.fullMatchKey));
        list2.removeIf(d -> matchKeys.contains(d.fullMatchKey));
    }

    private List<DblinkRecord> readCSVFile(String filePath, RecattribRecordsList recattribs) throws IOException {
        List<DblinkRecord> dblinkRecordsList = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            Iterable<CSVRecord> csvRecords = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader);

            for (CSVRecord record : csvRecords) {
                dblinkRecordsList.add(new DblinkRecord(record.get(0), record.get(1), record.get(2),
                        record.get(3), record.get(4), record.get(5), record.get(6), recattribs.get(record.get(3)) ));
            }
        }
        return dblinkRecordsList;
    }

    /**
     * Writes data to a CSV file.
     *
     * @param filePath Path to the output CSV file
     * @param data List of DblinkRecord objects containing the data to write
     */
    private void writeCSVFile(String filePath, List<DblinkRecord> data) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            // Write header
            writer.println(String.join(",", HEADERS));

            // Write data
            for (DblinkRecord record : data) {
                writer.println(record.toCSVLine());
            }
        } catch (IOException e) {
            System.err.println("Error writing to file " + filePath + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Finds items that are common between two lists based on zdbId.
     */
    private int deleteCommonItemsByZdbId(List<DblinkRecord> list1, List<DblinkRecord> list2) {
        List<String> zdbIds1 = list1.stream().map(i -> i.zdbId).toList();
        List<String> zdbIds2 = list2.stream().map(i -> i.zdbId).toList();
        HashSet<String> commonIDs = new HashSet<>();
        commonIDs.addAll(CollectionUtils.intersection(zdbIds1, zdbIds2));
        list1.removeIf(i -> commonIDs.contains(i.zdbId));
        list2.removeIf(i -> commonIDs.contains(i.zdbId));
        return commonIDs.size();
    }

    private List<DblinkRecord> findCommonItemsByZdbId(List<DblinkRecord> list1, List<DblinkRecord> list2) {
        List<DblinkRecord> commonItems = new ArrayList<>();

        // Create a map of zdbId to items for the second list
        Map<String, DblinkRecord> list2Map = new HashMap<>();
        for (DblinkRecord item : list2) {
            if (item.zdbId != null && !item.zdbId.isEmpty()) {
                list2Map.put(item.zdbId, item);
            }
        }

        // Find matches from the first list
        for (DblinkRecord item : list1) {
            if (item.zdbId != null && !item.zdbId.isEmpty() && list2Map.containsKey(item.zdbId)) {
                commonItems.add(item);
            }
        }

        return commonItems;
    }

    /**
     * Finds items that match the retained criteria (linkedRecId, accNum, fdbcontZdbId, length).
     */
    private List<DblinkRecord> findRetainedItems(List<DblinkRecord> list1, List<DblinkRecord> list2) {
        Set<Object> matchKeys = list2.stream()
                .map(d -> d.fullMatchKey)
                .collect(Collectors.toSet());

        return list1.stream()
                .filter(item -> matchKeys.contains(item.fullMatchKey))
                .collect(Collectors.toList());
    }

    /**
     * Finds items that match the updated length criteria (linkedRecId, accNum, fdbcontZdbId).
     */
    private List<DblinkRecord> findUpdatedLengthItems(List<DblinkRecord> list1, List<DblinkRecord> list2) {
        Set<Object> matchKeys = list2.stream()
                .map(d -> d.semiMatchKey)
                .collect(Collectors.toSet());

        // Filter list1 based on whether their match keys are in the set
        return list1.stream()
                .filter(item -> matchKeys.contains(item.semiMatchKey))
                .collect(Collectors.toList());
    }

}
