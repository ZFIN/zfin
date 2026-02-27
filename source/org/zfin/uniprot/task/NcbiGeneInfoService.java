package org.zfin.uniprot.task;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import static org.zfin.framework.HibernateUtil.currentSession;

/**
 * Shared utilities for tasks that work with the NCBI Danio_rerio.gene_info file.
 * Handles downloading, extracting, and loading gene_info data into a temporary
 * database table (tmp_ncbi_zebrafish).
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
     * Create the tmp_ncbi_zebrafish temporary table. Drops it first if it already exists.
     * Does NOT commit the transaction — the caller must manage the transaction.
     */
    public static void createNcbiZebrafishTempTable(Session session) {
        session.createNativeQuery("DROP TABLE IF EXISTS tmp_ncbi_zebrafish").executeUpdate();

        String createTable = """
                CREATE TEMP TABLE tmp_ncbi_zebrafish (
                    taxId text,
                    geneId text,
                    symbol text,
                    locusTag text,
                    synonyms text,
                    dbXrefs text,
                    chromosome text,
                    mapLocation text,
                    description text,
                    typeOfGene text,
                    symbolFromNomenclatureAuthority text,
                    fullNameFromNomenclatureAuthority text,
                    nomenclatureStatus text,
                    otherDesignations text,
                    modificationDate text,
                    featureType text);
        """;
        session.createNativeQuery(createTable).executeUpdate();
        session.flush();
    }

    /**
     * Load the contents of the extracted NCBI gene_info file into the tmp_ncbi_zebrafish table.
     * Does NOT commit the transaction — the caller must manage the transaction.
     */
    public static void loadNcbiFileIntoTempTable(Session session, File inputFile) {
        try (CSVParser parser = CSVParser.parse(inputFile, Charset.defaultCharset(), CSVFormat.TDF)) {

            CSVRecord headerRecord = parser.iterator().next();
            if (!headerRecord.get(0).equals("#tax_id")) {
                throw new RuntimeException("Unexpected header format. Expected #tax_id but found " + headerRecord.get(0));
            }

            log.info("Parsing file: " + inputFile.getAbsolutePath() + " and loading into temporary table in database.");
            int recordCount = 0;
            for (CSVRecord record : parser) {
                recordCount++;

                session.createNativeQuery("""
                        INSERT INTO tmp_ncbi_zebrafish
                        (taxId, geneId, symbol, locusTag, synonyms, dbXrefs, chromosome, mapLocation, description, typeOfGene, symbolFromNomenclatureAuthority, fullNameFromNomenclatureAuthority, nomenclatureStatus, otherDesignations, modificationDate, featureType)
                         VALUES
                        (:taxId, :geneId, :symbol, :locusTag, :synonyms, :dbXrefs, :chromosome, :mapLocation, :description, :typeOfGene, :symbolFromNomenclatureAuthority, :fullNameFromNomenclatureAuthority, :nomenclatureStatus, :otherDesignations, :modificationDate, :featureType)
                        """)
                        .setParameter("taxId", record.get(0))
                        .setParameter("geneId", record.get(1))
                        .setParameter("symbol", record.get(2))
                        .setParameter("locusTag", record.get(3))
                        .setParameter("synonyms", record.get(4))
                        .setParameter("dbXrefs", record.get(5))
                        .setParameter("chromosome", record.get(6))
                        .setParameter("mapLocation", record.get(7))
                        .setParameter("description", record.get(8))
                        .setParameter("typeOfGene", record.get(9))
                        .setParameter("symbolFromNomenclatureAuthority", record.get(10))
                        .setParameter("fullNameFromNomenclatureAuthority", record.get(11))
                        .setParameter("nomenclatureStatus", record.get(12))
                        .setParameter("otherDesignations", record.get(13))
                        .setParameter("modificationDate", record.get(14))
                        .setParameter("featureType", record.get(15))
                        .executeUpdate();
            }
            log.info("Finished data load of " + recordCount + " records.");
        } catch (IOException e) {
            e.printStackTrace();
        }
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
