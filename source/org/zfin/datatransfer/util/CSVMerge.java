package org.zfin.datatransfer.util;
import org.apache.commons.csv.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

/**
 * Utility class for merging two CSV files based on join columns.
 *
 * This class merges data from two CSV files where values from the second file
 * are added to the first file based on matching keys in specified join columns.
 * The values from the second file are aggregated and joined with a pipe (|) delimiter.
 *
 */
public class CSVMerge {
    private final File file1;
    private final File file2;
    private final String joinCol1;
    private final String joinCol2;
    private final String valueCol2;
    private final File outputFile;

    /**
     * Constructs a new CSVMerge instance with specified parameters.
     *
     * @param file1      The primary CSV file whose structure forms the basis of the merged file
     * @param file2      The secondary CSV file containing values to be merged into the primary file
     * @param joinCol1   The column name in file1 used for joining
     * @param joinCol2   The column name in file2 used for joining
     * @param valueCol2  The column name in file2 whose values will be merged into the output
     * @param outputFile The destination file where the merged CSV will be written
     */
    public CSVMerge(File file1, File file2, String joinCol1, String joinCol2, String valueCol2, File outputFile) {
        this.file1 = file1;
        this.file2 = file2;
        this.joinCol1 = joinCol1;
        this.joinCol2 = joinCol2;
        this.valueCol2 = valueCol2;
        this.outputFile = outputFile;
    }

    /**
     * Main entry point for the CSVMerge utility.
     *
     * Processes command line arguments and executes the merge operation.
     *
     * @param args Command line arguments in the following order:
     *             1. file1 - Path to the primary CSV file
     *             2. file2 - Path to the secondary CSV file
     *             3. joinCol1 - Column name in file1 for joining
     *             4. joinCol2 - Column name in file2 for joining
     *             5. valueCol2 - Column name in file2 whose values will be merged
     *             6. outputFile - Path where the merged CSV will be written
     * @throws IOException If an I/O error occurs during file operations
     */
    public static void main(String[] args) throws IOException {
        if (args.length < 6) {
            System.out.println(args.length);
            System.err.println("Usage: java CSVMerger <file1> <file2> <joinCol1> <joinCol2> <valueCol2> <outputFile>");
            System.exit(1);
        }

        String file1 = args[0];
        String file2 = args[1];

        String joinCol1 = args[2];
        String joinCol2 = args[3];
        String valueCol2 = args[4];
        String outputFile = args[5];

        CSVMerge merger = new CSVMerge(new File(file1), new File(file2), joinCol1, joinCol2, valueCol2, new File(outputFile));
        merger.run();
        System.out.println("Merged CSV written to " + outputFile);
    }

    /**
     * Executes the CSV merge operation.
     *
     * This method performs the following steps:
     * 1. Creates a mapping from join keys in file2 to lists of corresponding values
     * 2. Reads records from file1
     * 3. For each record in file1, looks up matching values from file2 using the join key
     * 4. Appends the joined values (sorted and pipe-delimited) to the output
     *
     * @throws IOException If an error occurs while reading from or writing to files
     */
    public void run() throws IOException {
        // Build map from file2: joinCol2 -> list of valueCol2
        Map<String, List<String>> joinMap = new HashMap<>();
        try (
                Reader in2 = Files.newBufferedReader(file2.toPath());
                CSVParser parser2 = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(in2)
        ) {
            for (CSVRecord record : parser2) {
                String joinKey = record.get(joinCol2);
                String value = record.get(valueCol2);
                joinMap.computeIfAbsent(joinKey, k -> new ArrayList<>()).add(value);
            }
        }

        // Read file1 and write merged output
        try (
                Reader in1 = Files.newBufferedReader(file1.toPath());
                CSVParser parser1 = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(in1);
                Writer out = Files.newBufferedWriter(outputFile.toPath());
                CSVPrinter printer = CSVFormat.DEFAULT
                        .withHeader(Stream.concat(parser1.getHeaderNames().stream(), Stream.of(valueCol2 )).toArray(String[]::new))
                        .print(out)
        ) {
            List<String> headers = parser1.getHeaderNames();
            for (CSVRecord record : parser1) {
                String joinKey = record.get(joinCol1);
                List<String> values = joinMap.getOrDefault(joinKey, Collections.emptyList());
                values.sort(Comparator.naturalOrder());
                String joinedValues = String.join("|", values);

                List<String> row = new ArrayList<>();
                for (String col : headers) {
                    row.add(record.get(col));
                }
                row.add(joinedValues);

                printer.printRecord(row);
            }
        }
    }
}