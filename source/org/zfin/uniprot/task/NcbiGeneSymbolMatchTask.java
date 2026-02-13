package org.zfin.uniprot.task;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.zfin.datatransfer.webservice.NCBIEfetch;
import org.zfin.framework.HibernateUtil;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;
import org.zfin.uniprot.NcbiGeneSymbolMatchRow;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.*;
import java.util.zip.GZIPInputStream;

import static org.zfin.framework.HibernateUtil.currentSession;

/**
 * Standalone task that reports NCBI genes whose symbol matches a ZFIN marker
 * abbreviation but which lack a reciprocal db_link in ZFIN.
 *
 * The report includes:
 * - NCBI gene type (from gene_info file)
 * - ZFIN marker type (from marker table)
 * - RefSeq RNA accessions (from gene2accession file, optional)
 * - Whether the gene is "not in current annotation release" (from NCBI eSearch API)
 *
 * Input files:
 *   gene_info:              set via arg, NCBI_FILE_URL env var, or ncbiFileUrl system property
 *   gene2accession:         set via NCBI_GENE2ACCESSION_URL env var or ncbiGene2AccessionUrl system property (optional)
 *   notInAnnotationRelease: set via NCBI_NOT_IN_ANNOTATION_URL env var or ncbiNotInAnnotationUrl system property (optional, falls back to NCBI API)
 */
@Log4j2
public class NcbiGeneSymbolMatchTask extends AbstractScriptWrapper {
    private static final String GENE_SYMBOL_MATCH_CSV_FILE = "ncbi_gene_symbol_matches.csv";
    private static final String DEFAULT_INPUT_FILE_URL = "https://ftp.ncbi.nlm.nih.gov/gene/DATA/GENE_INFO/Non-mammalian_vertebrates/Danio_rerio.gene_info.gz";
    private String inputFileUrl;
    private String gene2AccessionUrl;
    private String notInCurrentAnnotationReleaseUrl;

    public static void main(String[] args) {
        NcbiGeneSymbolMatchTask task = new NcbiGeneSymbolMatchTask();

        try {
            String inputFileUrl = null;
            if (args.length > 0) {
                inputFileUrl = args[0];
            }
            task.runTask(inputFileUrl);
        } catch (IOException | SQLException e) {
            log.error("Exception Error while running task: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        HibernateUtil.closeSession();
        log.info("Task completed successfully.");
        System.exit(0);
    }

    public void runTask(String inputFileUrl) throws IOException, SQLException {
        runTask(inputFileUrl, null);
    }

    public void runTask(String inputFileUrl, Map<String, String> toDelete) throws IOException, SQLException {
        initAll();

        setInputFileUrlFromEnvironment(inputFileUrl);
        setGene2AccessionUrlFromEnvironment();
        setNotInCurrentAnnotationReleaseUrlFromEnvironment();
        File extractedCsvFile = downloadAndExtract();

        // Parse gene2accession for RefSeq RNA accessions (optional)
        Map<String, String> geneIdToRefSeqs = parseGene2AccessionFile();

        // Fetch "not in current annotation release" gene IDs from NCBI
        Set<String> notInCurrentAnnotationRelease = fetchNotInCurrentAnnotationRelease();

        Session session = currentSession();
        Transaction transaction = HibernateUtil.createTransaction();
        try {
            createTemporaryNcbiTable(session);
            copyFromNcbiFileIntoDatabase(session, extractedCsvFile);
            createTemporaryTableOfNcbiDblinksToDelete(toDelete);
            createNcbi2ZfinTable(session);

            List<NcbiGeneSymbolMatchRow> symbolMatches = getGeneSymbolMatches(session);

            transaction.commit();

            // Enrich rows with RefSeq and annotation release data
            enrichRows(symbolMatches, geneIdToRefSeqs, notInCurrentAnnotationRelease);

            writeGeneSymbolMatchCsv(symbolMatches);

            dropTemporaryTables();
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw e;
        }
        extractedCsvFile.delete();
    }

    private void setInputFileUrlFromEnvironment(String inputFileUrl) {
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
        this.inputFileUrl = inputFileUrl;
    }

    public void setGene2AccessionUrl(String url) {
        this.gene2AccessionUrl = url;
    }

    public void setNotInCurrentAnnotationReleaseUrl(String url) {
        this.notInCurrentAnnotationReleaseUrl = url;
    }

    private void setGene2AccessionUrlFromEnvironment() {
        if (gene2AccessionUrl != null) {
            log.info("Using gene2accession file url (set via caller): " + gene2AccessionUrl);
            return;
        }
        gene2AccessionUrl = System.getenv("NCBI_GENE2ACCESSION_URL");
        if (gene2AccessionUrl == null) {
            gene2AccessionUrl = System.getProperty("ncbiGene2AccessionUrl");
        }
        if (gene2AccessionUrl != null) {
            log.info("Using gene2accession file url: " + gene2AccessionUrl);
        } else {
            log.info("No gene2accession URL provided (set NCBI_GENE2ACCESSION_URL or ncbiGene2AccessionUrl). RefSeq column will be empty.");
        }
    }

    /**
     * Parse the gene2accession gzip file and build a map of NCBI gene ID to
     * semicolon-separated RefSeq RNA accessions (NM_, XM_, NR_, XR_).
     *
     * The file may be the full multi-species gene2accession.gz or a pre-filtered
     * zebrafish-only file. Lines for taxon 7955 are processed; all others are skipped.
     *
     * @return map of geneId -> "NM_001;XM_002;..." (empty map if no URL configured)
     */
    private Map<String, String> parseGene2AccessionFile() throws IOException {
        if (gene2AccessionUrl == null) {
            return Map.of();
        }

        log.info("Downloading gene2accession file from: " + gene2AccessionUrl);
        File tempGzipFile = File.createTempFile("gene2accession.", ".gz");
        try {
            downloadFile(gene2AccessionUrl, tempGzipFile);
        } catch (IOException e) {
            log.error("Failed to download gene2accession file: " + e.getMessage());
            tempGzipFile.delete();
            return Map.of();
        }

        // Stream through gzip, filter for zebrafish, collect RefSeq RNA accessions per gene
        Map<String, Set<String>> geneIdToRefSeqSet = new HashMap<>();
        int lineCount = 0;

        log.info("Parsing gene2accession file for RefSeq RNA accessions...");
        try (FileInputStream fis = new FileInputStream(tempGzipFile);
             GZIPInputStream gzis = new GZIPInputStream(fis);
             InputStreamReader reader = new InputStreamReader(gzis);
             BufferedReader br = new BufferedReader(reader)) {

            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("#")) {
                    continue;
                }

                String[] fields = line.split("\t", -1);
                if (fields.length < 8) {
                    continue;
                }

                String taxId = fields[0];
                if (!"7955".equals(taxId)) {
                    continue;
                }
                lineCount++;

                String ncbiGeneId = fields[1];
                String status = fields[2];
                if ("SUPPRESSED".equals(status)) {
                    continue;
                }

                // Only collect RefSeq accessions (status != "-")
                if ("-".equals(status)) {
                    continue;
                }

                String rnaAccVersion = fields[3];
                if (rnaAccVersion != null && !rnaAccVersion.isEmpty() && Character.isLetter(rnaAccVersion.charAt(0))) {
                    String rnaAcc = rnaAccVersion.replaceFirst("\\.\\d+$", "");
                    if (rnaAcc.matches("^(NM_|XM_|NR_|XR_).*")) {
                        geneIdToRefSeqSet.computeIfAbsent(ncbiGeneId, k -> new LinkedHashSet<>()).add(rnaAcc);
                    }
                }
            }
        } finally {
            tempGzipFile.delete();
        }

        log.info("Parsed " + lineCount + " zebrafish lines from gene2accession. Found RefSeq RNA accessions for " + geneIdToRefSeqSet.size() + " genes.");

        // Convert Set<String> to semicolon-separated String
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, Set<String>> entry : geneIdToRefSeqSet.entrySet()) {
            result.put(entry.getKey(), String.join("; ", entry.getValue()));
        }
        return result;
    }

    private void setNotInCurrentAnnotationReleaseUrlFromEnvironment() {
        if (notInCurrentAnnotationReleaseUrl != null) {
            log.info("Using 'not in current annotation release' URL (set via caller): " + notInCurrentAnnotationReleaseUrl);
            return;
        }
        notInCurrentAnnotationReleaseUrl = System.getenv("NCBI_NOT_IN_ANNOTATION_URL");
        if (notInCurrentAnnotationReleaseUrl == null) {
            notInCurrentAnnotationReleaseUrl = System.getProperty("ncbiNotInAnnotationUrl");
        }
        if (notInCurrentAnnotationReleaseUrl != null) {
            log.info("Using 'not in current annotation release' URL: " + notInCurrentAnnotationReleaseUrl);
        } else {
            log.info("No 'not in current annotation release' URL provided (set NCBI_NOT_IN_ANNOTATION_URL or ncbiNotInAnnotationUrl). Will fetch from NCBI API.");
        }
    }

    /**
     * Load NCBI gene IDs that are not in the current annotation release.
     * Prefers a URL (one gene ID per line) if configured; otherwise fetches from NCBI eSearch API.
     * Returns an empty set if both fail.
     */
    private Set<String> fetchNotInCurrentAnnotationRelease() {
        if (notInCurrentAnnotationReleaseUrl != null) {
            try {
                URL url = new URL(notInCurrentAnnotationReleaseUrl);
                try (BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()))) {
                    List<String> geneIds = br.lines()
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .toList();
                    log.info("Loaded " + geneIds.size() + " gene IDs from " + notInCurrentAnnotationReleaseUrl);
                    return new HashSet<>(geneIds);
                }
            } catch (IOException e) {
                log.warn("Failed to read 'not in current annotation release' URL " + notInCurrentAnnotationReleaseUrl + ": " + e.getMessage() + ". Falling back to NCBI API.");
            }
        }

        try {
            List<String> geneIds = NCBIEfetch.fetchGeneIDsNotInCurrentAnnotationReleaseSet();
            log.info("Fetched " + geneIds.size() + " gene IDs not in current annotation release from NCBI API.");
            return new HashSet<>(geneIds);
        } catch (Exception e) {
            log.warn("Failed to fetch 'not in current annotation release' gene IDs: " + e.getMessage() + ". Column will be empty.");
            return Set.of();
        }
    }

    /**
     * Enrich each row with RefSeq accessions and annotation release status.
     */
    private void enrichRows(List<NcbiGeneSymbolMatchRow> rows,
                            Map<String, String> geneIdToRefSeqs,
                            Set<String> notInCurrentAnnotationRelease) {
        for (NcbiGeneSymbolMatchRow row : rows) {
            row.setRefSeqAccessions(geneIdToRefSeqs.getOrDefault(row.getNcbiId(), ""));
            if (!notInCurrentAnnotationRelease.isEmpty()) {
                row.setNotInCurrentAnnotationRelease(
                        notInCurrentAnnotationRelease.contains(row.getNcbiId()) ? "true" : "false");
            }
        }
    }

    public File downloadAndExtract() throws IOException {
        String url = this.inputFileUrl;
        File tempGzipFile = File.createTempFile("danio_rerio.gene_info.", ".gz");
        File tempOutputCsvFile = File.createTempFile("danio_rerio.gene_info.", ".csv");

        downloadFile(url, tempGzipFile);
        extractGzip(tempGzipFile, tempOutputCsvFile);
        tempGzipFile.delete();
        return tempOutputCsvFile;
    }

    private void createTemporaryNcbiTable(Session session) {
        String dropTable = "DROP TABLE IF EXISTS tmp_ncbi_zebrafish;";
        session.createNativeQuery(dropTable).executeUpdate();

        dropTable = "DROP TABLE IF EXISTS tmp_ncbi2zfin;";
        session.createNativeQuery(dropTable).executeUpdate();

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

    private void copyFromNcbiFileIntoDatabase(Session session, File inputFile) {
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

    private void createTemporaryTableOfNcbiDblinksToDelete(Map<String, String> toDelete) {
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

    private void createNcbi2ZfinTable(Session session) {
        String query = """
            SELECT geneId AS ncbi_id, replace(t.xref, 'ZFIN:', '') AS zdb_id
            INTO TEMP TABLE tmp_ncbi2zfin
            FROM tmp_ncbi_zebrafish, unnest(string_to_array(dbXrefs, '|')) AS t (xref)
            WHERE t.xref LIKE 'ZFIN:%'
        """;
        session.createNativeQuery(query).executeUpdate();
    }

    private List<NcbiGeneSymbolMatchRow> getGeneSymbolMatches(Session session) {
        String query = """
                SELECT
                    nz.geneId AS ncbi_id,
                    nz.symbol AS gene_symbol,
                    m.mrkr_zdb_id,
                    COALESCE(n2z.zdb_id, '') AS ncbi_predicted_zdb_id,
                    CASE WHEN n2z.zdb_id = m.mrkr_zdb_id THEN 'true' ELSE 'false' END AS zdb_ids_match,
                    nz.typeOfGene AS ncbi_gene_type,
                    m.mrkr_type AS zfin_marker_type
                FROM
                    tmp_ncbi_zebrafish nz
                    INNER JOIN marker m ON m.mrkr_abbrev = nz.symbol
                    LEFT JOIN tmp_ncbi2zfin n2z ON nz.geneId = n2z.ncbi_id
                    LEFT JOIN db_link dbl ON dbl.dblink_acc_num = nz.geneId
                        AND dbl.dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-1'
                        AND dbl.dblink_zdb_id NOT IN (SELECT dblink_zdb_id FROM dblinks_to_delete)
                WHERE
                    dbl.dblink_linked_recid IS NULL
                ORDER BY
                    nz.symbol COLLATE "C" ASC,
                    nz.geneId
                """;
        List<Object[]> results = session.createNativeQuery(query).list();
        log.info("Number of NCBI genes matched by gene symbol: " + results.size());

        return results.stream().map(row ->
                NcbiGeneSymbolMatchRow.builder()
                        .ncbiId((String) row[0])
                        .symbol((String) row[1])
                        .mrkrZdbId((String) row[2])
                        .ncbiPredictedZdbId((String) row[3])
                        .zdbIdsMatch((String) row[4])
                        .ncbiGeneType((String) row[5])
                        .zfinMarkerType((String) row[6])
                        .build()
        ).toList();
    }

    private void writeGeneSymbolMatchCsv(List<NcbiGeneSymbolMatchRow> rows) {
        try (Writer writer = Files.newBufferedWriter(Paths.get(GENE_SYMBOL_MATCH_CSV_FILE));
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                     .withHeader("ncbi_id", "gene_symbol", "mrkr_zdb_id", "ncbi_predicted_zdb_id", "zdb_ids_match",
                             "ncbi_gene_type", "zfin_marker_type", "refseq_accessions", "not_in_current_annotation_release"))) {
            for (NcbiGeneSymbolMatchRow row : rows) {
                csvPrinter.printRecord(row.toList());
            }
            csvPrinter.flush();
            log.info("Wrote gene symbol match results to " + GENE_SYMBOL_MATCH_CSV_FILE);
        } catch (IOException e) {
            log.error("IOException Error while writing gene symbol match csv: " + e.getMessage());
            e.printStackTrace();
            System.exit(4);
        }
    }

    private void dropTemporaryTables() {
        Session session = currentSession();
        Transaction tx = session.beginTransaction();
        session.createNativeQuery("DROP TABLE IF EXISTS tmp_ncbi2zfin").executeUpdate();
        session.createNativeQuery("DROP TABLE IF EXISTS tmp_ncbi_zebrafish").executeUpdate();
        tx.commit();
    }

    private void downloadFile(String url, File tempGzipFile) throws IOException {
        String fileName = tempGzipFile.getAbsolutePath();

        if (tempGzipFile.exists()) {
            new File(fileName).delete();
        }

        URL website = new URL(url);
        try (InputStream in = website.openStream()) {
            Files.copy(in, Paths.get(fileName));
        }
    }

    private void extractGzip(File tempGzipFile, File outputFileName) throws IOException {
        byte[] buffer = new byte[1024];
        try (GZIPInputStream gzis = new GZIPInputStream(new FileInputStream(tempGzipFile))) {
            try (FileOutputStream out = new FileOutputStream(outputFileName)) {
                int len;
                while ((len = gzis.read(buffer)) > 0) {
                    out.write(buffer, 0, len);
                }
            }
        }
    }
}
