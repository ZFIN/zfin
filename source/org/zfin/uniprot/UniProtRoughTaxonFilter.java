package org.zfin.uniprot;

import lombok.extern.log4j.Log4j2;

import java.io.*;
import java.util.LinkedList;

/**
 * This is for making a first pass through the uniprot file to remove the records that aren't 7955.
 * It doesn't have the overhead of the UniProtFilterTask because it doesn't need to parse the records.
 */
@Log4j2
public class UniProtRoughTaxonFilter {

    private static final String SEPARATOR = "//";
    private static final String ZFISH_TAXON_MATCH = "NCBI_TaxID=7955";
    private final BufferedReader delegate;
    private File tempFile;

    private LinkedList<String> lineBuffer = new LinkedList<>();

    public UniProtRoughTaxonFilter(Reader in) {
        delegate = new BufferedReader(in);
    }

    public BufferedReader getFilteredReader() throws IOException {
        tempFile = File.createTempFile("uniprot", ".dat");
        tempFile.deleteOnExit();

        log.debug("Temp file used for quick filtering by taxon: " + tempFile.getAbsolutePath());
        long lineCount = 0;

        long startTime = System.currentTimeMillis();
        long notifyInterval = 30_000;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            String line;
            StringBuilder record = new StringBuilder();
            while ((line = delegate.readLine()) != null) {
                if (SEPARATOR.equals(line.trim())) {
                    if (record.toString().contains(ZFISH_TAXON_MATCH)) {
                        writer.write(record.toString() + SEPARATOR);
                        writer.newLine();
                    }
                    record.setLength(0); // Reset the record
                } else {
                    record.append(line).append("\n");
                }

                //notify of progress
                lineCount++;
                if (lineCount % 10_000_000 == 0) {
                    if (System.currentTimeMillis() - startTime > notifyInterval) {
                        startTime = System.currentTimeMillis();

                        String prettyFormat = String.format("%,d", lineCount);
                        log.info("Taxon Filter Processed Line Count: " + prettyFormat);
                    }
                }
            }
            // Handle the last record if it wasn't followed by a separator
            if (record.length() > 0 && record.toString().contains(ZFISH_TAXON_MATCH)) {
                writer.write(record.toString() + SEPARATOR);
            }
        }

        return new BufferedReader(new FileReader(tempFile));
    }

    public void cleanup() {
        if (tempFile != null && tempFile.exists()) {
            tempFile.delete();
        }
    }
}
