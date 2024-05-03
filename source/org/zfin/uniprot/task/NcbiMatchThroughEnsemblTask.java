package org.zfin.uniprot.task;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.zfin.framework.HibernateUtil;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;

import java.net.URL;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.DBLink;
import org.zfin.sequence.ForeignDBDataType;
import org.zfin.sequence.MarkerDBLink;
import org.zfin.sequence.service.SequenceService;
import org.zfin.uniprot.NcbiMatchReportRow;


/**
 *
 * If ncbi has a link to us, but we don't reciprocate, we check for shared ensembl IDs.
 * This is a supplementary check to the NCBI Load (see ZFIN-8517)
 *
 * Description from ticket:
 * Our current NCBI Load is missing some potential matches between ZFIN and NCBI Gene records.  Our NCBI Load script matches on RNA sequence IDs and, in cases where there is no RNA sequence ID, there is no way to match records.
 * NCBI does annotate links to ZFIN genes and I’ve found that the associations they create are quite good.  So NCBI may have a correct match to a ZFIN gene, but we don’t have a reciprocal link.
 * My goal with this report is to identify potential ZFIN/NCBI Gene matches that are supported by additional data, make those associations and pull data from NCBI Gene (including RefSeq IDs) onto ZFIN gene pages as is done with the current NCBI load.  The addition of this data adds to the robustness of ZFIN gene records and adding additional RefSeqs will allow us to associate more UniProt IDs and the resulting UniProt data.
 * A reasonable method to do this would be to find all NCBI Gene records that have a link to ZFIN. Ignore the NCBI Gene records to which we already have links.  For those NCBI Gene records with links to ZFIN that don’t have reciprocal links from ZFIN to NCBI, obtain the Ensembl gene ID in the NCBI Gene records and match those against Ensembl gene IDs on ZFIN gene records.  This list would contain links that NCBI has made to ZFIN and are supported by a shared Ensembl ID.
 * Finally, identify any instances where the Ensembl ID from NCBI hits a ZFIN gene that already contains a link to an NCBI Gene record (there is some sort of conflict that needs to be resolved).
 *
 * Call with first argument being the input file (https://ftp.ncbi.nlm.nih.gov/gene/DATA/GENE_INFO/Non-mammalian_vertebrates/Danio_rerio.gene_info.gz)
 * Alternatively, set the environment variable NCBI_FILE_URL to the input file.
 *
 */
@Log4j2
public class NcbiMatchThroughEnsemblTask extends AbstractScriptWrapper {
    private static final String CSV_FILE = "ncbi_matches_through_ensembl.csv";
    private static final String DEFAULT_INPUT_FILE_URL = "https://ftp.ncbi.nlm.nih.gov/gene/DATA/GENE_INFO/Non-mammalian_vertebrates/Danio_rerio.gene_info.gz";
    private String inputFileUrl;

    public static void main(String[] args) {
        NcbiMatchThroughEnsemblTask task = new NcbiMatchThroughEnsemblTask();

        try {
            task.runTask(args);
        } catch (IOException | SQLException e) {
            log.error("Exception Error while running task: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        HibernateUtil.closeSession();
        log.info("Task completed successfully.");
        System.exit(0);
    }

    public void runTask(String[] args) throws IOException, SQLException {
        initAll();

        setInputFileUrlFromEnvironment(args);
        File extractedCsvFile = downloadAndExtract();
        createTemporaryNcbiTable();
        copyFromNcbiFileIntoDatabase(extractedCsvFile);
        List<Object[]> results = getReportOfPotentialNcbiGenes();
        List<NcbiMatchReportRow> ncbiMatchReportRows = convertResultsToReportRow(results);

        addRnaAccessionsToReportRows(ncbiMatchReportRows);

        writeResultsToCsv(ncbiMatchReportRows);
        dropTemporaryTables();
        extractedCsvFile.delete();
    }

    private void addRnaAccessionsToReportRows(List<NcbiMatchReportRow> ncbiMatchReportRows) {
        HibernateUtil.createTransaction();

        for (NcbiMatchReportRow ncbiMatchReportRow : ncbiMatchReportRows) {
            List<String> rnaAccessions = getRnaAccessions(ncbiMatchReportRow.getZdbId());

            //progress
            System.out.print(".");

            ncbiMatchReportRow.setRnaAccessions(String.join(";", rnaAccessions));
        }

        HibernateUtil.flushAndCommitCurrentSession();
    }

    private List<String> getRnaAccessions(String zdbId) {
        MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();
        SequenceService sequenceService = new SequenceService();
        sequenceService.setMarkerRepository(markerRepository);
        List<MarkerDBLink> results = sequenceService.getMarkerDBLinkResultsForMarkerZdbID(zdbId, false, null, false);
        return results.stream()
                .filter(markerDBLink -> ForeignDBDataType.DataType.RNA.equals(markerDBLink.getReferenceDatabase().getForeignDBDataType().getDataType()))
                .map(DBLink::getAccessionNumber)
                .toList();
    }

    private List<NcbiMatchReportRow> convertResultsToReportRow(List<Object[]> inputRows) {
        return inputRows.stream().map(inputRow ->
                NcbiMatchReportRow.builder()
                    .ncbiId((String) inputRow[0])
                    .zdbId((String) inputRow[1])
                    .ensemblId((String) inputRow[2])
                    .symbol((String) inputRow[3])
                    .dblinks((String) inputRow[4])
                    .publications((String) inputRow[5])
                    .rnaAccessions((String) inputRow[6])
                    .build()
        ).toList();
    }

    /**
     * Figure out the source of the NCBI file URL. (can be a file url eg. file:///var/tmp/... or a http url)
     * It can be set with:
     *  the first command line argument,
     *  or the environment variable NCBI_FILE_URL,
     *  or it uses the default URL.
     * @param args command line arguments
     */
    private void setInputFileUrlFromEnvironment(String[] args) {
        String inputFile = null;
        if (args.length > 0) {
            inputFile = args[0];
        } else {
            inputFile = System.getenv("NCBI_FILE_URL");
            if (inputFile == null) {
                inputFile = System.getProperty("ncbiFileUrl");
                if (inputFile == null) {
                    log.error("No input file url specified. Please set the environment variable NCBI_FILE_URL or property ncbiFileUrl. Using default url.");
                    inputFile = DEFAULT_INPUT_FILE_URL;
                }
            }
        }
        log.info("Using input file url: " + inputFile);
        this.inputFileUrl = inputFile;
    }

    /**
     * Download the file from the URL and extract it to a temporary csv file.
     */
    public File downloadAndExtract() throws IOException {
        String url = this.inputFileUrl;
        File tempGzipFile = File.createTempFile("danio_rerio.gene_info.", ".gz");
        File tempOutputCsvFile = File.createTempFile("danio_rerio.gene_info.", ".csv");

        downloadFile(url, tempGzipFile);
        extractGzip(tempGzipFile, tempOutputCsvFile);
        tempGzipFile.delete();
        return tempOutputCsvFile;
    }

    /**
     * Create the table structure used later in data load.
     */
    private void createTemporaryNcbiTable() {

        //create temporary table
        Session session = HibernateUtil.currentSession();
        Transaction transaction = HibernateUtil.createTransaction();

        String dropTable = "DROP TABLE IF EXISTS tmp_ncbi_zebrafish;";
        session.createNativeQuery(dropTable).executeUpdate();

        dropTable = "DROP TABLE IF EXISTS tmp_ncbi2ensembl;";
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
        transaction.commit();
    }

    /**
     *  Load the contents of the NCBI file into a temporary table in the database (tmp_ncbi_zebrafish).
     *  This table has the same columns as the NCBI file.  It's just used to make the data easier to work with
     *  for set operations.
      * @param inputFile (the temporary csv file after extracting the gzipped NCBI file)
     */
    private void copyFromNcbiFileIntoDatabase(File inputFile) {

        try (CSVParser parser = CSVParser.parse(inputFile, Charset.defaultCharset(), CSVFormat.TDF)) {

            //confirm header looks good
            CSVRecord headerRecord = parser.iterator().next();
            if (!headerRecord.get(0).equals("#tax_id")) {
                throw new RuntimeException("Unexpected header format. Expected #tax_id but found " + headerRecord.get(0));
            }

            log.info("Parsing file: " + inputFile.getAbsolutePath() + " and loading into temporary table in database.");
            Session session = HibernateUtil.currentSession();
            Transaction tx = session.beginTransaction();
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
            tx.commit();
            log.info("Finished data load of " + recordCount + " records.");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     *  The main logic for comparing the NCBI data to the ZFIN data.
     */
    private List<Object[]> getReportOfPotentialNcbiGenes() {
        Session session = HibernateUtil.currentSession();
        Transaction transaction = HibernateUtil.createTransaction();

        //create ncbi2zfin table -- based on data loaded through NCBI
        String query = """
            SELECT geneId AS ncbi_id, replace(t.xref, 'ZFIN:', '') AS zdb_id
            INTO TEMP TABLE tmp_ncbi2zfin 
            FROM tmp_ncbi_zebrafish, unnest(string_to_array(dbXrefs, '|')) AS t (xref)
            WHERE t.xref LIKE 'ZFIN:%'                
        """;
        session.createNativeQuery(query).executeUpdate();

        //create ncbi2ensembl table -- based on data loaded through NCBI
        query = """
            SELECT geneId as ncbi_id, replace(t.xref, 'Ensembl:', '') as ensembl_id, symbol 
            INTO TEMP TABLE tmp_ncbi2ensembl
            FROM tmp_ncbi_zebrafish, unnest(string_to_array(dbXrefs, '|')) as t(xref)
            WHERE t.xref like 'Ensembl:%' 
        """;
        session.createNativeQuery(query).executeUpdate();

        //how many ncbi genes have a link to zfin, but we don't have a reciprocal link?
        query = """
            SELECT count(*) AS num_ncbi_genes_without_zfin_links FROM
            (SELECT DISTINCT n2z.zdb_id 
            FROM tmp_ncbi2zfin n2z
                LEFT JOIN db_link dbl ON dbl.dblink_acc_num = n2z.ncbi_id
                    AND dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-1'
            WHERE
                dblink_linked_recid IS NULL) AS x
        """;
        BigInteger count = (BigInteger) session.createNativeQuery(query).uniqueResult();
        log.info("Number of NCBI genes that have a link to ZFIN, but we don't have a reciprocal link: " + count);

        // Get semi-final report using the above mapping tables.
        // First (step 1), get all ncbi genes that have a link to zfin, but we don't have a reciprocal link.
        // Then, get all zfin genes that have a common link to the same ensembl gene as those ncbi genes from step 1.
        query = """
                SELECT
                    n2z.ncbi_id,
                    zdb_id,
                    ensembl_id,
                    symbol,
                    string_agg(dbl2.dblink_zdb_id, '; ' order by dbl2.dblink_zdb_id) AS dblinks,
                    string_agg(ra.recattrib_source_zdb_id, '; ' order by ra.recattrib_source_zdb_id) AS publications
                    INTO TEMP TABLE ncbi_match_report
                FROM
                    tmp_ncbi2zfin n2z
                    LEFT JOIN db_link dbl ON dbl.dblink_acc_num = n2z.ncbi_id
                        AND "dblink_fdbcont_zdb_id" = 'ZDB-FDBCONT-040412-1'
                    LEFT JOIN tmp_ncbi2ensembl n2e ON n2z.ncbi_id = n2e.ncbi_id
                    LEFT JOIN db_link dbl2 ON n2e.ensembl_id = dbl2.dblink_acc_num
                    LEFT JOIN record_attribution ra ON dbl2.dblink_zdb_id = ra.recattrib_data_zdb_id
                WHERE
                    dbl.dblink_linked_recid IS NULL -- No match on ncbi ID
                    AND ensembl_id IS NOT NULL -- Yes match on ensembl ID
                    AND dbl2.dblink_linked_recid = n2z.zdb_id
                GROUP BY
                    n2z.ncbi_id,
                    zdb_id,
                    ensembl_id,
                    symbol ORDER BY n2z.ncbi_id
                """;
        session.createNativeQuery(query).executeUpdate();

        // Add the rna accessions to the report.
        query = """
            SELECT
                nmr.*,
                string_agg(dblink_acc_num, ';' ORDER BY dblink_acc_num) AS rna_accessions
            FROM
                ncbi_match_report nmr
                LEFT JOIN (
                    SELECT
                        dblink_linked_recid,
                        dblink_acc_num
                    FROM
                        db_link
                        LEFT JOIN foreign_db_contains ON dblink_fdbcont_zdb_id = fdbcont_zdb_id
                    WHERE
                        fdbcont_fdbdt_id = 3) subq ON subq.dblink_linked_recid = nmr.zdb_id
            GROUP BY
                ncbi_id,
                zdb_id,
                ensembl_id,
                symbol,
                dblinks,
                publications
            ORDER BY
                symbol COLLATE "C" ASC, -- because some systems don't sort the same based on locale (examples: dre-mir-21-2 and dre-mir-212, see: https://stackoverflow.com/questions/22534484)
                ncbi_id,
                zdb_id,
                ensembl_id,
                dblinks,
                publications,
                rna_accessions
            """;
        List<Object[]> results = session.createNativeQuery(query).list();

        transaction.commit();

        log.info("Number of those NCBI genes with shared ensembl id : " + results.size());

        return results;
    }

    /**
     * Write the results of the sql comparisons to a CSV file.
     *
     * @param results The results from DB query to write to CSV.
     */
    private void writeResultsToCsv(List<NcbiMatchReportRow> results) {
        try (Writer writer = Files.newBufferedWriter(Paths.get(CSV_FILE));
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                     .withHeader("ncbi_id", "zdb_id", "ensembl_id", "symbol", "dblinks", "publications", "rna_accessions"))) {
            for (NcbiMatchReportRow result : results) {
                csvPrinter.printRecord(result.toList());
            }
            csvPrinter.flush();
            log.info("Wrote results to " + CSV_FILE);
        } catch (IOException e) {
            log.error("IOException Error while writing results to csv: " + e.getMessage());
            e.printStackTrace();
            System.exit(4);
        }
    }

    /**
     * Drop the temporary tables used in the comparison.
     * This is unnecessary, but it's good practice to clean up after yourself.
     */
    private void dropTemporaryTables() {
        Session session = HibernateUtil.currentSession();
        Transaction tx = session.beginTransaction();
        session.createNativeQuery("DROP TABLE IF EXISTS tmp_ncbi2zfin").executeUpdate();
        session.createNativeQuery("DROP TABLE IF EXISTS tmp_ncbi2ensembl").executeUpdate();
        session.createNativeQuery("DROP TABLE IF EXISTS tmp_ncbi_zebrafish").executeUpdate();
        tx.commit();
    }

    /**
     * Download the NCBI gene file from NCBI.
     *
     * @param url The URL to download the file from (could be "file:///...").
     * @param tempGzipFile The temporary file destination.
     * @throws IOException If there is an error downloading the file.
     */
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

    /**
     * Extract the downloaded file.
     *
     * @param tempGzipFile The temporary file source.
     * @param outputFileName The output file destination.
     * @throws IOException If there is an error extracting the file.
     */
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
