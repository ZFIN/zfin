package org.zfin.datatransfer.ncbi;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.ListUtils;
import org.zfin.datatransfer.webservice.NCBIEfetch;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Log4j2
public class NCBIReplacedGeneIDsTask extends AbstractScriptWrapper {

    public static final int BATCH_SIZE = 1000;
    private File deadIDsOutputFile;
    private File mappedIDsOutputFile;
    private Long fetchCount = 0L;
    private List<String> deadIDs = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        NCBIReplacedGeneIDsTask task = new NCBIReplacedGeneIDsTask();
        task.initAll();
        task.config(args);
        task.run();
    }

    private void config(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: NCBIReplacedGeneIDsTask <deadIDsOutputFile> <mappedIDsOutputFile>");
            System.exit(1);
        }
        String deadIDsOutputFileString = args[0];
        String mappedIDsOutputFileString = args[1];
        deadIDsOutputFile = new File(deadIDsOutputFileString);
        mappedIDsOutputFile = new File(mappedIDsOutputFileString);
    }

    @SneakyThrows
    private void run() {
        //fetch all genes not alive at NCBI
        deadIDs = getDeadIDs();
        List<List<String>> deadIDBatches = ListUtils.partition(deadIDs, BATCH_SIZE);
        System.out.println("Found " + deadIDs.size() + " dead gene IDs at NCBI");
        Files.writeString(deadIDsOutputFile.toPath(), String.join("\n", deadIDs));

        //check if we are continuing from previous run
        Map<String, String> mappedIDs = getMappedIDs();

        try (FileWriter writer = new FileWriter(mappedIDsOutputFile, true)) {
            for (List<String> deadIDBatch : deadIDBatches) {
                List<String> filteredBatch = deadIDBatch.stream().filter(id -> !mappedIDs.containsKey(id)).toList();
                printProgress(filteredBatch.size());
                Map<String, String> replacedIDs = NCBIEfetch.getReplacedGeneID(filteredBatch);
                for(String id : filteredBatch) {
                    writer.write(id);
                    if (replacedIDs.containsKey(id)) {
                        writer.write("," + replacedIDs.get(id));
                    }
                    writer.write("\n");
                }
                writer.flush();
            }
        }
        System.out.println("Done");
    }

    private Map<String, String> getMappedIDs() throws IOException {
        if (mappedIDsOutputFile.exists()) {
            System.out.println("Mapped IDs output file already exists, reading from file: " + mappedIDsOutputFile.getAbsolutePath());
            try {
                List<String> lines = Files.readAllLines(mappedIDsOutputFile.toPath());
                System.out.println("Read " + lines.size() + " mapped IDs from file.");
                //skip header
                lines.remove(0);
                return lines.stream()
                        .map(line -> line.split(","))
                        .collect(java.util.stream.Collectors.toMap(parts -> parts[0], parts -> (parts.length > 1 ? parts[1] : "")));
            } catch (IOException e) {
                System.err.println("Could not read mapped IDs from file, will start fresh.");
            }
        } else {
            Files.writeString(mappedIDsOutputFile.toPath(), "OldID,NewID\n");
        }
        return Map.of();
    }

    /**
     * Fetch from endpoint unless the file already exists. In that case read from file.
     * @return list of dead IDs
     */
    private List<String> getDeadIDs() {
        List<String> tmpIDs;
        if (deadIDsOutputFile.exists()) {
            System.out.println("Dead IDs output file already exists, reading from file: " + deadIDsOutputFile.getAbsolutePath());
            try {
                tmpIDs = Files.readAllLines(deadIDsOutputFile.toPath());
                System.out.println("Read " + tmpIDs.size() + " dead IDs from file.");
                return tmpIDs;
            } catch (IOException e) {
                System.err.println("Could not read dead IDs from file, will fetch from NCBI instead.");
            }
        }
        return NCBIEfetch.fetchGeneIDsNotAlive(100_000);
    }

    private void printProgress(int size) {
        System.out.print(".");
        if (fetchCount % 30 == 0) {
            System.out.println();
        }
        if (fetchCount % 100 == 0) {
            System.out.println("Processed " + fetchCount + " IDs of " + deadIDs.size());
        }
        fetchCount += size;
    }

}