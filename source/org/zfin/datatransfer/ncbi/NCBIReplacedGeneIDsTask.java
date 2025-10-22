package org.zfin.datatransfer.ncbi;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.ListUtils;
import org.zfin.datatransfer.webservice.NCBIEfetch;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;
import org.zfin.properties.ZfinPropertiesEnum;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Log4j2
public class NCBIReplacedGeneIDsTask extends AbstractScriptWrapper {

    private int BATCH_SIZE = 1000;
    private File deadIDsOutputFile;
    private File mappedIDsOutputFile;
    private Long fetchCount = 0L;
    private List<String> deadIDs = new ArrayList<>();
    private Boolean storeResultsInDatabase = false;
    private Boolean skipFetch = false;

    public static void main(String[] args) throws IOException, InterruptedException {
        NCBIReplacedGeneIDsTask task = new NCBIReplacedGeneIDsTask();
        task.initAll();
        task.config(args);
        task.run();
        task.store();
    }

    private void config(String[] args) throws IOException {
        if (System.getenv("BATCH_SIZE") != null) {
            BATCH_SIZE = Integer.parseInt(System.getenv("BATCH_SIZE"));
            System.out.println("Using BATCH_SIZE from environment: " + BATCH_SIZE);
        } else {
            System.out.println("Using default BATCH_SIZE (override with environment variable): " + BATCH_SIZE);
        }

        if (args.length == 1) {
            String mappedIDsOutputFileString = args[0];
            mappedIDsOutputFile = new File(mappedIDsOutputFileString);
            storeResultsInDatabase = true;
            skipFetch = true;
        } else if (args.length == 2) {
            String deadIDsOutputFileString = args[0];
            String mappedIDsOutputFileString = args[1];
            deadIDsOutputFile = new File(deadIDsOutputFileString);
            mappedIDsOutputFile = new File(mappedIDsOutputFileString);
        } else if (args.length == 0) {
            System.out.println("Using default file names for output.");
            deadIDsOutputFile = Files.createTempFile("ncbi_dead_gene_ids", ".txt").toFile();
            mappedIDsOutputFile = Files.createTempFile("ncbi_mapped_gene_ids", ".csv").toFile();
            System.out.println("Dead IDs output file: " + deadIDsOutputFile.getAbsolutePath());
            System.out.println("Mapped IDs output file: " + mappedIDsOutputFile.getAbsolutePath());
            deadIDsOutputFile.deleteOnExit();
            mappedIDsOutputFile.deleteOnExit();
            storeResultsInDatabase = true;
        } else {
            System.out.println("Usage: NCBIReplacedGeneIDsTask [deadIDsOutputFile mappedIDsOutputFile]");
            System.out.println("If no arguments are provided, temporary files will be created and results will be stored in the database.");
            System.out.println("If one argument is provided, it is assumed to be the mappedIDsOutputFile and results will be stored in the database.");
        }
    }

    @SneakyThrows
    private void run() {
        if (skipFetch) {
            System.out.println("Skipping fetch of dead IDs as per configuration.");
            return;
        }
        //fetch all genes not alive at NCBI
        deadIDs = getDeadIDs();
        List<List<String>> deadIDBatches = ListUtils.partition(deadIDs, BATCH_SIZE);
        Files.writeString(deadIDsOutputFile.toPath(), String.join("\n", deadIDs));
        System.out.println("Found " + deadIDs.size() + " dead gene IDs at NCBI");

        //check if we are continuing from previous run
        Map<String, String> mappedIDs = getMappedIDs();

        try (FileWriter writer = new FileWriter(mappedIDsOutputFile, true)) {
            for (List<String> deadIDBatch : deadIDBatches) {
                List<String> filteredBatch = deadIDBatch.stream().filter(id -> !mappedIDs.containsKey(id)).toList();
                printProgress(filteredBatch.size());
                Map<String, String> replacedIDs = NCBIEfetch.getReplacedGeneID(filteredBatch);
                for(String id : filteredBatch) {
                    writer.write(id + ",");
                    if (replacedIDs.containsKey(id)) {
                        writer.write(replacedIDs.get(id));
                    }
                    writer.write("\n");
                }
                writer.flush();
            }
        }
        System.out.println("Done");
    }

    private void store() throws IOException, InterruptedException {
        if (!storeResultsInDatabase) {
            return;
        }
        String sql = """
                -- TEMPORARY TABLE TO HOLD NEW DATA
                create temp table tmp_ncbi_replaced_id (old_id varchar(20), new_id varchar(20));
                
                -- SET EMPTY NEW_ID TO NULL
                \\copy tmp_ncbi_replaced_id from '%s' with (format csv, header true);
                update tmp_ncbi_replaced_id set new_id = null where new_id = '';

                -- REMOVE ROWS THAT ARE ALREADY PRESENT IN THE MAIN TABLE WITH SAME NEW_ID (NULLSAFE)
                delete from tmp_ncbi_replaced_id t where exists 
                    (select 1 from ncbi_replaced_id n where n.nri_old_id = t.old_id and 
                        n.nri_new_id is not distinct from t.new_id);

                -- INSERT NEW ROWS OR UPDATE EXISTING ONES
                insert into ncbi_replaced_id (nri_old_id, nri_new_id) select old_id, new_id from tmp_ncbi_replaced_id 
                on conflict (nri_old_id) do update set nri_new_id = excluded.nri_new_id;
                
                -- Are we referencing any old ids in db_link
                \\echo 'THE FOLLOWING GENE IDS HAVE BEEN REPLACED AT NCBI AND ARE REFERENCED IN DB_LINK:';
                copy (SELECT dblink_linked_recid AS gene, dblink_acc_num AS old_ncbi_id, nri_new_id AS new_ncbi_id FROM db_link JOIN ncbi_replaced_id ON dblink_acc_num = nri_old_id WHERE dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-1') to stdout with csv header;
                """;
        sql = String.format(sql, mappedIDsOutputFile.getAbsolutePath());
        File sqlFile = Files.createTempFile("ncbi_replaced_gene_id_store", ".sql").toFile();
        Files.writeString(sqlFile.toPath(), sql);
        System.out.println("Storing results in database using temporary SQL file: " + sqlFile.getAbsolutePath());
        sqlFile.deleteOnExit();

        // execute process above but stream the stdout and stderr
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("psql", "-d", ZfinPropertiesEnum.DBNAME.value(), "-h", ZfinPropertiesEnum.PGHOST.value(), "-f", sqlFile.getAbsolutePath());
        Process process = processBuilder.start();

        // Stream stdin
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
                System.out.println(line);
        }

        // Stream stderr
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        while ((line = errorReader.readLine()) != null) {
                System.err.println(line);
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            System.err.println("Error executing psql command. Exit code: " + exitCode);
        } else {
            System.out.println("Successfully stored replaced gene IDs in database.");
        }
    }

    private Map<String, String> getMappedIDs() throws IOException {
        if (mappedIDsOutputFile.exists() && mappedIDsOutputFile.length() > 0) {
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
            Files.writeString(mappedIDsOutputFile.toPath(), "old_id,new_id\n");
        }
        return Map.of();
    }

    /**
     * Fetch from endpoint unless the file already exists. In that case read from file.
     * @return list of dead IDs
     */
    private List<String> getDeadIDs() {
        List<String> tmpIDs;
        if (deadIDsOutputFile.exists() && deadIDsOutputFile.length() > 0) {
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