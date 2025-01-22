package org.zfin.uniprot.task;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.biojava.bio.BioException;
import org.biojavax.RankedCrossRef;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.io.*;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.engine.spi.SessionImplementor;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;
import org.zfin.framework.HibernateUtil;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to analyze the uniprot problem7 file data.
 * The problem7 file lists uniprot records that don't have EMBL references.
 * We are creating a report here that takes those records and matches
 * them to zfin genes based on RefSeq IDs.
 *
 * Invoke this class with an environment variable (UNIPROT_INPUT_FILE) to point to the problem7 file.
 *
 * Example with bash:
 * $ UNIPROT_INPUT_FILE=/var/tmp/problem7 gradle uniProtAnalysisTask
 *
 * The generated report is written to the file "uniprot_analysis_zfin_8275.csv"
 *
 * See: ZFIN-8275
 */
public class UniProtAnalysisTask extends AbstractScriptWrapper {

    private static final String CSV_FILE = "uniprot_analysis_zfin_8275.csv";

    public static void main(String[] args) {
        UniProtAnalysisTask task = new UniProtAnalysisTask();

        try {
            task.runTask();
        } catch (IOException e) {
            System.err.println("IOException Error while running task: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } catch (BioException e) {
            System.err.println("BioException Error while running task: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        } catch (SQLException e) {
            System.err.println("SQLException Error while running task: " + e.getMessage());
            e.printStackTrace();
            System.exit(3);
        }

        HibernateUtil.closeSession();
        System.out.println("Task completed successfully.");
        System.exit(0);
    }

    public void runTask() throws IOException, BioException, SQLException {
        initAll();

        System.out.println("This method has been disabled temporarily");
        System.err.println("This method has been disabled temporarily");
        System.exit(1);

//        String inputFileName = getInputFileName();
//        RichStreamReaderWrapper richStreamReader = getRichStreamReaderForUniprotDatFile(inputFileName, true);
//
//        System.out.println("Starting to read file: " + inputFileName);
//        List<ImmutablePair<String, String>> pairs = getUniProtRefSeqPairs(richStreamReader);
//        System.out.println("Finished file: " + pairs.size());
//
//        System.out.println("Writing to temp file");
//        File tempFile = writePairsToTemporaryFile(pairs);
//        System.out.println("Finished writing to temp file: " + tempFile.getAbsolutePath());
//
//        writeFileToTemporaryTable(tempFile);
//        generateReport();
//        System.out.println("Finished writing report to file: " + CSV_FILE);
//
//        dropTemporaryTable();
    }

    private String getInputFileName() {
        String inputFile = System.getenv("UNIPROT_INPUT_FILE");
        if (inputFile == null) {
            System.err.println("No input file specified. Please set the environment variable UNIPROT_INPUT_FILE.");
            System.exit(3);
        }
        return inputFile;
    }

    private List<ImmutablePair<String, String>> getUniProtRefSeqPairs(RichStreamReader richStreamReader) throws BioException {
        List<ImmutablePair<String, String>> UniProtRefSeqPairs = new ArrayList<>();
        while (richStreamReader.hasNext()) {
            RichSequence seq = richStreamReader.nextRichSequence();

            UniProtRefSeqPairs.addAll (seq.getRankedCrossRefs()
                    .stream()
                    .filter(rankedXref -> "RefSeq".equals(((RankedCrossRef)rankedXref).getCrossRef().getDbname()))
                    .map(rankedXref -> new ImmutablePair<>(
                            seq.getAccession(),
                            ((RankedCrossRef)rankedXref).getCrossRef().getAccession().replaceAll("\\.\\d*", "")
                    ))
                    .toList());
        }
        return UniProtRefSeqPairs;
    }

    private File writePairsToTemporaryFile(List<ImmutablePair<String, String>> pairs) throws IOException {
        //create temporary file for import
        File tempFile =File.createTempFile("uniprot-report-temp-file", ".csv");

        //write to temporary file
        BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile));
        for (ImmutablePair<String, String> pair : pairs) {
            bw.write(pair.getLeft() + "," + pair.getRight());
            bw.newLine();
        }
        bw.close();
        return tempFile;
    }

    private void writeFileToTemporaryTable(File tempFile) throws SQLException, IOException {

        //create temporary table
        Session session = HibernateUtil.currentSession();
        Transaction transaction = HibernateUtil.createTransaction();

        String dropTable = "DROP TABLE IF EXISTS up_to_refseq_temp;";
        String createTable = "CREATE TABLE up_to_refseq_temp (uniprot text, refseq text);";

        session.createNativeQuery(dropTable).executeUpdate();
        session.createNativeQuery(createTable).executeUpdate();
        session.flush();
        transaction.commit();

        copyFromTempFileIntoDatabase(tempFile);
    }

    private void copyFromTempFileIntoDatabase(File tempFile) throws SQLException, IOException {
        FileReader fileReader = new FileReader(tempFile);
        Session session = HibernateUtil.currentSession();
        SessionImplementor sessImpl = (SessionImplementor) session;
        BaseConnection conn = (BaseConnection) sessImpl.getJdbcConnectionAccess().obtainConnection();
        conn.setAutoCommit(true);
        Transaction tx = sessImpl.beginTransaction();
        CopyManager copyManager = new CopyManager(conn);
        long numInserted = copyManager.copyIn("copy up_to_refseq_temp (uniprot, refseq) from STDIN with csv", fileReader);
        System.out.println("Number of rows inserted: " + numInserted);
        sessImpl.flush();
        session.flush();
        tx.commit();
    }

    private void generateReport() throws IOException {
        Path path = Paths.get( CSV_FILE );
        BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8);

        CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                .withHeader( "uniprot","refseqs","gene_count","genes","dblinks","dblink_info" ));

        String sql = """
            SELECT
                uniprot,
                string_agg(refseq, ';') AS refseqs,
                count(DISTINCT dblink_linked_recid) AS gene_count,
                string_agg(DISTINCT dblink_linked_recid, ';') AS genes,
                string_agg(dblink_zdb_id, ';') AS dblinks,
                string_agg(DISTINCT dblink_info, ';') AS dblink_info
            FROM
                up_to_refseq_temp up
                LEFT JOIN db_link dl ON up.refseq = dl.dblink_acc_num
            GROUP BY
                uniprot
            ORDER BY
            	count(DISTINCT dblink_linked_recid) DESC,
            	uniprot
        """;
        List results = HibernateUtil.currentSession().createNativeQuery(sql).list();
        for(Object result : results) {
            writeCsvLine(result, csvPrinter);
        }

        csvPrinter.flush();
        csvPrinter.close();
        writer.close();
    }

    private void dropTemporaryTable() {
        Session session = HibernateUtil.currentSession();
        Transaction tx = session.beginTransaction();
        session.createNativeQuery("DROP TABLE up_to_refseq_temp").executeUpdate();
        tx.commit();
    }

    private void writeCsvLine(Object result, CSVPrinter csvPrinter) throws IOException {
        Object[] row = (Object[]) result;
        List<String> record = new ArrayList<>();
        for (Object o : row) {
            record.add(o == null ? "" : o.toString());
        }
        csvPrinter.printRecord(record);
    }

}
