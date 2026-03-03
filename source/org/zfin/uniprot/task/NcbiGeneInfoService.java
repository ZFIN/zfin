package org.zfin.uniprot.task;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;
import org.zfin.datatransfer.ncbi.NcbiGeneInfo;
import org.zfin.datatransfer.persistence.LoadFileLog;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import static org.zfin.framework.HibernateUtil.currentSession;

/**
 * Shared utilities for tasks that work with the NCBI Danio_rerio.gene_info file.
 * Handles downloading, extracting, and loading gene_info data into the persistent
 * database table (external_resource.ncbi_danio_rerio_gene_info).
 */
@Log4j2
public class NcbiGeneInfoService {

    private static final String DEFAULT_INPUT_FILE_URL =
            "https://ftp.ncbi.nlm.nih.gov/gene/DATA/GENE_INFO/Non-mammalian_vertebrates/Danio_rerio.gene_info.gz";

    /**
     * Resolve the NCBI gene_info file URL from (in order of priority):
     * the provided argument, the NCBI_FILE_URL env var, the ncbiFileUrl system property,
     * or the default NCBI FTP URL.
     */
    public static String resolveInputFileUrl(String inputFileUrl) {
        if (StringUtils.isEmpty(inputFileUrl)) {
            inputFileUrl = System.getenv("NCBI_FILE_URL");
            if (inputFileUrl == null) {
                inputFileUrl = System.getProperty("ncbiFileUrl");
                if (inputFileUrl == null) {
                    log.error("No input file url specified. Please set the environment variable NCBI_FILE_URL or property ncbiFileUrl. Using default url.");
                    inputFileUrl = DEFAULT_INPUT_FILE_URL;
                }
            }
        }
        log.info("Using input file url: " + inputFileUrl);
        return inputFileUrl;
    }

    /**
     * Download the gene_info gzip file from the given URL and extract it to a temporary CSV file.
     *
     * @param url The URL to download from (can be file:// or http(s)://).
     * @return The extracted temporary CSV file (caller is responsible for deleting it).
     */
    public static File downloadAndExtract(String url) throws IOException {
        File tempGzipFile = File.createTempFile("danio_rerio.gene_info.", ".gz");
        File tempOutputCsvFile = File.createTempFile("danio_rerio.gene_info.", ".csv");

        downloadFile(url, tempGzipFile);
        extractGzip(tempGzipFile, tempOutputCsvFile);
        tempGzipFile.delete();
        return tempOutputCsvFile;
    }

    /**
     * Create the dblinks_to_delete temporary table from a map of db_link ZDB IDs
     * that should be treated as already deleted for matching purposes.
     *
     * @param toDelete Map of dblink ZDB IDs to exclude (can be null or empty).
     */
    public static void createDblinksToDeleteTempTable(Map<String, String> toDelete) {
        if (toDelete == null) {
            toDelete = Map.of();
        }

        String query = """
            DROP TABLE IF EXISTS dblinks_to_delete;
            CREATE TEMP TABLE dblinks_to_delete (
                dblink_zdb_id text
            );
        """;
        currentSession().createNativeQuery(query).executeUpdate();

        List<List<String>> toDeleteBatches = ListUtils.partition(toDelete.keySet().stream().toList(), 1000);
        for (List<String> batch : toDeleteBatches) {
            String inClause = batch.stream().map(s -> "'" + s + "'").reduce((s1, s2) -> s1 + "," + s2).orElse("");
            query = """
                INSERT INTO dblinks_to_delete (dblink_zdb_id)
                SELECT dblink_zdb_id FROM db_link
                WHERE  dblink_zdb_id IN (""" + inClause + ")";

            currentSession().createNativeQuery(query).executeUpdate();
        }
    }

    private static final String PERSISTENT_TABLE_NAME = "external_resource.ncbi_danio_rerio_gene_info";
    private static final String LOAD_NAME = "NCBI_gene_info";

    /**
     * Truncate the persistent external_resource.ncbi_danio_rerio_gene_info table
     * and reload it from the extracted gene_info CSV file.
     * Skips the load if the file's md5 hash matches the most recent load_file_log entry.
     * Logs the load to load_file_log on success.
     * Does NOT commit the transaction — the caller must manage the transaction.
     */
    public static void loadNcbiFileIntoPersistentTable(Session session, File inputFile) {
        String md5;
        try {
            md5 = DigestUtils.md5Hex(FileUtils.openInputStream(inputFile));
        } catch (IOException e) {
            throw new RuntimeException("Failed to compute md5 for file: " + inputFile.getAbsolutePath(), e);
        }

        // Check if we already loaded this exact file
        LoadFileLog existing = session.createQuery(
                        "from LoadFileLog where tableName = :tableName order by processedDate desc",
                        LoadFileLog.class)
                .setParameter("tableName", PERSISTENT_TABLE_NAME)
                .setMaxResults(1)
                .uniqueResult();

        if (existing != null && md5.equals(existing.getMd5())) {
            log.info("NCBI gene_info file md5 matches last load (" + md5 + "). Skipping persistent table reload.");
            return;
        }

        session.createNativeQuery("DELETE FROM " + PERSISTENT_TABLE_NAME).executeUpdate();
        session.flush();

        try (CSVParser parser = CSVParser.parse(inputFile, Charset.defaultCharset(), CSVFormat.TDF)) {
            CSVRecord headerRecord = parser.iterator().next();
            if (!headerRecord.get(0).equals("#tax_id")) {
                throw new RuntimeException("Unexpected header format. Expected #tax_id but found " + headerRecord.get(0));
            }

            log.info("Loading NCBI gene_info into persistent table " + PERSISTENT_TABLE_NAME + "...");
            int recordCount = 0;
            int batchSize = 50;
            for (CSVRecord record : parser) {
                NcbiGeneInfo entity = new NcbiGeneInfo();
                entity.setTaxId(record.get(0));
                entity.setGeneId(record.get(1));
                entity.setSymbol(record.get(2));
                entity.setLocusTag(record.get(3));
                entity.setSynonyms(record.get(4));
                entity.setDbXrefs(record.get(5));
                entity.setChromosome(record.get(6));
                entity.setMapLocation(record.get(7));
                entity.setDescription(record.get(8));
                entity.setTypeOfGene(record.get(9));
                entity.setSymbolFromNomenclatureAuthority(record.get(10));
                entity.setFullNameFromNomenclatureAuthority(record.get(11));
                entity.setNomenclatureStatus(record.get(12));
                entity.setOtherDesignations(record.get(13));
                entity.setModificationDate(record.get(14));
                entity.setFeatureType(record.get(15));
                session.persist(entity);

                recordCount++;
                if (recordCount % batchSize == 0) {
                    session.flush();
                    session.clear();
                }
            }
            session.flush();
            log.info("Loaded " + recordCount + " records into persistent table.");

            // Log the successful load
            LoadFileLog logEntry = new LoadFileLog();
            logEntry.setLoadName(LOAD_NAME);
            logEntry.setFilename(inputFile.getName());
            logEntry.setSource(DEFAULT_INPUT_FILE_URL);
            logEntry.setDate(new Date());
            logEntry.setSize(inputFile.length());
            logEntry.setMd5(md5);
            logEntry.setPath(inputFile.getAbsolutePath());
            logEntry.setProcessedDate(new Date());
            logEntry.setTableName(PERSISTENT_TABLE_NAME);
            logEntry.setNotes("Loaded " + recordCount + " records");
            session.persist(logEntry);

        } catch (IOException e) {
            throw new RuntimeException("Failed to load NCBI gene_info into persistent table", e);
        }
    }

    /**
     * Download a file from a URL to a local file.
     */
    public static void downloadFile(String url, File destination) throws IOException {
        if (destination.exists()) {
            destination.delete();
        }

        URL website = new URL(url);
        try (InputStream in = website.openStream()) {
            Files.copy(in, Paths.get(destination.getAbsolutePath()));
        }
    }

    /**
     * Extract a gzip file to an output file.
     */
    public static void extractGzip(File gzipFile, File outputFile) throws IOException {
        byte[] buffer = new byte[1024];
        try (GZIPInputStream gzis = new GZIPInputStream(new FileInputStream(gzipFile))) {
            try (FileOutputStream out = new FileOutputStream(outputFile)) {
                int len;
                while ((len = gzis.read(buffer)) > 0) {
                    out.write(buffer, 0, len);
                }
            }
        }
    }
}
