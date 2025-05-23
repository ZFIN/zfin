package org.zfin.datatransfer.ncbi;

import jakarta.persistence.Tuple;
import org.apache.commons.io.FileUtils;
import org.hibernate.query.NativeQuery;
import org.zfin.datatransfer.ncbi.port.PortHelper;
import org.zfin.datatransfer.ncbi.port.PortSqlHelper;
import org.zfin.framework.exec.ExecProcess;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.zfin.datatransfer.ncbi.port.PortHelper.*;
import static org.zfin.datatransfer.ncbi.port.PortSqlHelper.getSqlForGeneAndRnagDbLinksFromFdbContId;
import static org.zfin.framework.HibernateUtil.currentSession;
import static org.zfin.util.DateUtil.nowToString;


public class NCBIDirectPort extends AbstractScriptWrapper {
    private File workingDir;

//    use DBI;
//    use Cwd;
//    use POSIX;
//    use Try::Tiny;
//    use FindBin;

//#relative path to library file(s) (ZFINPerlModules.pm)
//    use lib "$FindBin::Bin/../../perl_lib/";
//    use ZFINPerlModules qw(assertEnvironment trim getPropertyValue downloadOrUseLocalFile md5File assertFileExistsAndNotEmpty) ;

    private Boolean debug = true;

    private String pubMappedbasedOnRNA = "ZDB-PUB-020723-3";
    private String pubMappedbasedOnVega = "ZDB-PUB-130725-2";
    private String pubMappedbasedOnNCBISupplement = "ZDB-PUB-230516-87";

    private String fdcontNCBIgeneId = "ZDB-FDBCONT-040412-1";
    private String fdcontVega = "ZDB-FDBCONT-040412-14";
    private String fdcontGenBankRNA = "ZDB-FDBCONT-040412-37";
    private String fdcontGenPept = "ZDB-FDBCONT-040412-42";
    private String fdcontGenBankDNA = "ZDB-FDBCONT-040412-36";
    private String fdcontRefSeqRNA = "ZDB-FDBCONT-040412-38";
    private String fdcontRefPept = "ZDB-FDBCONT-040412-39";
    private String fdcontRefSeqDNA = "ZDB-FDBCONT-040527-1";

    // used in eg. initializeDatabase
    private String dbname;
    private String dbhost;
    private String instance;
    private String username;
    private String password;
    private String handle;

    // used in eg. getMetricsOfDbLinksToDelete
    private Map<String, Integer>  toDelete;
    private Long ctToDelete;

    // used in eg. getRecordCounts
    private Map<String, String>  genesWithRefSeqBeforeLoad = new HashMap<>();
    private Integer ctGenesWithRefSeqBefore;
    private Integer numNCBIgeneIdBefore;
    private String numRefSeqRNABefore;
    private String numRefPeptBefore;
    private String numRefSeqDNABefore;
    private String numGenBankRNABefore;
    private String numGenPeptBefore;
    private String numGenBankDNABefore;
    private String numGenesRefSeqRNABefore;
    private String numGenesRefSeqPeptBefore;
    private String numGenesGenBankBefore;

    // used in eg. readZfinGeneInfoFile
    private String ctVegaIdsNCBI;
    private Map<String, List<String>>  NCBIgeneWithMultipleVega;
    private Map<String, List<String>>  NCBIidsGeneSymbols;
    private Map<String, List<String>>  geneSymbolsNCBIids;
    private Map<String, List<String>>  vegaIdsNCBIids;
    private Map<String, List<String>>  vegaIdwithMultipleNCBIids;

    // used in eg. initializeSetsOfZfinRecords
    private Map<String, List<String>>  supportedGeneZFIN;
    private Map<String, List<String>>  supportingAccZFIN;
    private Map<String, List<String>>  accZFINsupportingMoreThan1;
    private Map<String, List<String>>  geneZFINwithAccSupportingMoreThan1;
    private Map<String, List<String>>  accZFINsupportingOnly1;

    // used in eg. initializeSequenceLengthHash, lots of other places
    private Map<String, List<String>>  sequenceLength;

    // used in eg. parseGene2AccessionFile
    private String ctNoLength;
    private String ctNoLengthRefSeq;
    private String ctZebrafishGene2accession;
    private Map<String, List<String>>  GenBankDNAncbiGeneIds;
    private Map<String, List<String>>  GenPeptNCBIgeneIds;
    private Map<String, List<String>>  RefPeptNCBIgeneIds;
    private Map<String, List<String>>  RefSeqDNAncbiGeneIds;
    private Map<String, List<String>>  RefSeqRNAncbiGeneIds;
    private Map<String, List<String>>  noLength;
    private Map<String, List<String>>  supportedGeneNCBI;
    private Map<String, List<String>>  supportingAccNCBI;

    //  used in eg. initializeHashOfNCBIAccessionsSupportingMultipleGenes
    private Map<String, List<String>>  accNCBIsupportingMoreThan1;
    private Map<String, List<String>>  accNCBIsupportingOnly1;
    private Map<String, List<String>>  geneNCBIwithAccSupportingMoreThan1;

    //  used in eg. initializeMapOfZfinToNCBIgeneIds
    private Map<String, List<String>>  oneToNZFINtoNCBI;
    private Map<String, List<String>>  oneToOneZFINtoNCBI;
    private Map<String, List<String>>  genesZFINwithNoRNAFoundAtNCBI;

    //  used in eg. oneWayMappingNCBItoZfinGenes
    private Map<String, List<String>>  oneToOneNCBItoZFIN;
    private Map<String, List<String>>  oneToNNCBItoZFIN;

    //  used in eg. prepare2WayMappingResults
    private Map<String, List<String>>  mapped; // //  the list of 1:1; key: ZDB gene Id; value: NCBI gene Id
    private Map<String, List<String>>  mappedReversed;
    private String ctOneToOneNCBI;

    private Map<String, List<String>>  ncbiSupplementMap;
    private Map<String, List<String>>  ncbiSupplementMapReversed;

    //  used in eg. writeNCBIgeneIdsMappedBasedOnGenBankRNA
    private String ctToLoad;

    //  used in eg. getOneToNNCBItoZFINgeneIds
    private Map<String, List<String>>  nToOne;
    private Map<String, List<String>>  oneToN;

    //  used in eg. getNtoOneAndNtoNfromZFINtoNCBI
    private Map<String, List<String>>  zdbGeneIdsNtoOneAndNtoN;

    //  used in eg. buildVegaIDMappings
    private Map<String, List<String>>  ZDBgeneAndVegaGeneIds;
    private Map<String, List<String>> VegaGeneAndZDBgeneIds;
    private Map<String, List<String>>  ZDBgeneWithMultipleVegaGeneIds;
    private Map<String, List<String>>  vegaGeneIdWithMultipleZFINgenes;

    //  used in eg. writeCommonVegaGeneIdMappings
    private Map<String, List<String>>  oneToOneViaVega;

    //  used in eg. getGenBankAndRefSeqsWithZfinGenes
    private Map<String, List<String>>  geneAccFdbcont;

    //  used in eg. initializeGenPeptAccessionsMap
    private Map<String, List<String>>  GenPeptAttributedToNonLoadPub;
    private Map<String, List<String>>  GenPeptDbLinkIdAttributedToNonLoadPub;

    //  used in eg. processGenBankAccessionsAssociatedToNonLoadPubs
    private Map<String, List<String>>  GenPeptsToLoad;

    //  readZfinGeneInfoFile
    private Map<String, List<String>>  geneZDBidsSymbols;

    private String FASTA_LEN_COMMAND="./fasta_len.pl"; // was fasta_len.awk

//    assertFileExistsAndNotEmpty($FASTA_LEN_COMMAND, "Could not find FASTA_LEN_COMMAND: $FASTA_LEN_COMMAND");

    private Long stepCount = 0L;
    private Long STEP_TIMESTAMP = 0L;

    private BufferedWriter LOG;
    private BufferedWriter STATS_PRIORITY1;
    private BufferedWriter STATS_PRIORITY2;
    private BufferedWriter STATS;

    public static void main(String[] args) {
        NCBIDirectPort port = new NCBIDirectPort();
        port.initAll();
        port.run();
    }

    private void run() {
        assertEnvironment("PGHOST", "DB_NAME");

        initializeWorkingDir();

        assertExpectedFilesExist();

        outputDate();

        initializeDatabase();

        removeOldFiles();

        openLoggingFileHandles();
        printTimingInformation(1);


//    #-------------------------------------------------------------------------------------------------
//    # Step 1: Download NCBI data files
//    #-------------------------------------------------------------------------------------------------
        downloadNCBIFiles();
        printTimingInformation(2);

        captureBeforeState();
        printTimingInformation(201);

        prepareNCBIgeneLoadDatabaseQuery();
        printTimingInformation(3);

        getMetricsOfDbLinksToDelete();
        printTimingInformation(4);

//    # Get Record Counts using global variables
        getRecordCounts();
        printTimingInformation(5);

//        readZfinGeneInfoFile();
        printTimingInformation(6);

    }

    private void getRecordCounts() {
        String sql = """
        select mrkr_zdb_id, mrkr_abbrev from marker
        where (mrkr_zdb_id like 'ZDB-GENE%' or mrkr_zdb_id like '%RNAG%')
        and exists (select 1 from db_link
        where dblink_linked_recid = mrkr_zdb_id
        and dblink_fdbcont_zdb_id in (:fdcontRefSeqRNA,:fdcontRefPept,:fdcontRefSeqDNA))
        """;
        NativeQuery<Tuple> query = currentSession().createNativeQuery(sql, Tuple.class);
        query.setParameter("fdcontRefSeqRNA", fdcontRefSeqRNA);
        query.setParameter("fdcontRefPept", fdcontRefPept);
        query.setParameter("fdcontRefSeqDNA", fdcontRefSeqDNA);
        List<Tuple> curGenesWithRefSeq = query.list();
        for(Tuple tuple : curGenesWithRefSeq) {
            String geneId = (String) tuple.get(0);
            String geneSymbol = (String) tuple.get(1);
            genesWithRefSeqBeforeLoad.put(geneId, geneSymbol);
        }
        ctGenesWithRefSeqBefore = genesWithRefSeqBeforeLoad.size();


//    # NCBI Gene Id
        sql = getSqlForGeneAndRnagDbLinksFromFdbContId(fdcontNCBIgeneId);
        numNCBIgeneIdBefore = PortSqlHelper.countData(currentSession(), sql);
    }

    private void doSystemCommand(List<String> commandWithArguments, String stdoutRedirect, String stderrRedirect) throws IOException, InterruptedException {
        ExecProcess execProcess = new ExecProcess(commandWithArguments, false);
        execProcess.setWorkingDirectory(workingDir);

        FileOutputStream fileOutputStream = new FileOutputStream(workingDir + File.separator + stdoutRedirect);
        execProcess.setOutputStream(fileOutputStream);

        FileOutputStream fileErrorStream = new FileOutputStream(workingDir + File.separator + stderrRedirect);
        execProcess.setErrorStream(fileErrorStream);

        int result = execProcess.exec();

        if (result != 0) {
            throw new IOException("command failed: " + String.join(" ", commandWithArguments));
        }
    }

    private void doSystemCommand(String command, String stdoutRedirect, String stderrRedirect) throws IOException, InterruptedException {
        ExecProcess execProcess = new ExecProcess(command);
        execProcess.setWorkingDirectory(workingDir);

        FileOutputStream fileOutputStream = new FileOutputStream(workingDir + File.separator + stdoutRedirect);
        execProcess.setOutputStream(fileOutputStream);

        FileOutputStream fileErrorStream = new FileOutputStream(workingDir + File.separator + stderrRedirect);
        execProcess.setErrorStream(fileErrorStream);

        int result = execProcess.exec();

        if (result != 0) {
            throw new IOException("command failed: " + command);
        }
    }

    private void captureBeforeState() {
        try {
            doSystemCommand(
                    List.of(
                    "psql", "--echo-all", "-v", "ON_ERROR_STOP=1", "-U", env("PGUSER"), "-h", env("PGHOST"), "-d", env("DB_NAME"), "-a", "-c", "\\copy (select * from db_link order by dblink_linked_recid, dblink_acc_num) to 'before_db_link.csv' with csv header"
                    ), "prepareLog1", "prepareLog2");
            doSystemCommand(
                    List.of(
                    "psql", "--echo-all", "-v", "ON_ERROR_STOP=1", "-U", env("PGUSER"), "-h", env("PGHOST"), "-d", env("DB_NAME"), "-a", "-c", "\\copy (select * from record_attribution order by recattrib_data_zdb_id, recattrib_source_zdb_id) to 'before_recattrib.csv' with csv header"
                    ),
                    "prepareLog1", "prepareLog2");
        } catch (IOException | InterruptedException e) {
            reportErrAndExit("Auto from ${INSTANCE}: NCBI_gene_load.pl :: failed at before capture - $_");
        }
        print(LOG, "Done with preparing the delete list and the list for mapping.\n\n");

        String subject = "Auto from ${INSTANCE}: NCBI_gene_load.pl :: prepareLog1 file";
        sendMailWithAttachedReport(env("SWISSPROT_EMAIL_ERR"),subject,"prepareLog1");

        subject = "Auto from ${INSTANCE}: NCBI_gene_load.pl :: prepareLog2 file";
        sendMailWithAttachedReport(env("SWISSPROT_EMAIL_ERR"),subject,"prepareLog2");
    }

    private void assertExpectedFilesExist() {
        if (envTrue("SKIP_DOWNLOADS")) {
            assertFileExistsAndNotEmpty("gene2accession.gz", "missing gene2accession.gz");
            assertFileExistsAndNotEmpty("RELEASE_NUMBER", "missing RELEASE_NUMBER");
            assertFileExistsAndNotEmpty("RefSeqCatalog.gz", "missing RefSeqCatalog.gz");
            assertFileExistsAndNotEmpty("gene2vega.gz", "missing gene2vega.gz");
            assertFileExistsAndNotEmpty("zf_gene_info.gz", "missing zf_gene_info.gz");

            unless(envTrue("FORCE_EFETCH"), () -> {
                assertFileExistsAndNotEmpty("seq.fasta", "missing seq.fasta");
            });
        }
    }

    public void assertFileExistsAndNotEmpty(String filename, String errorMessage) {
        PortHelper.assertFileExistsAndNotEmpty(workingDir, filename, errorMessage);
    }

    private static void outputDate() {
        System.out.println(nowToString("yyyy-MM-dd HH:mm:ss"));
    }

    private void initializeWorkingDir() {
        String workingDirEnvironmentVariable = System.getenv("WORKING_DIR");
        if (workingDirEnvironmentVariable != null && !workingDirEnvironmentVariable.isEmpty()) {
            workingDir = new File(workingDirEnvironmentVariable);
        } else {
            String rootPath = System.getenv("ROOT_PATH");
            workingDir = new File(rootPath + "/server_apps/data_transfer/NCBIGENE/");
        }
    }

    private void initializeDatabase() {
    }

    private void removeOldFiles() {
        if (!envTrue("SKIP_DOWNLOADS")) {
            if (!envTrue("NO_SLEEP")) {
                System.out.println("Removing old files in 30 seconds...");
                try {
                    Thread.sleep(30_000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            System.out.println("Removing prepareLog* loadLog* logNCBIgeneLoad debug* report* toDelete.unl toMap.unl toLoad.unl length.unl noLength.unl seq.fasta zf_gene_info.gz gene2vega.gz gene2accession.gz RefSeqCatalog.gz RELEASE_NUMBER");
            rmFile(workingDir, "prepareLog*", true);
            rmFile(workingDir, "loadLog*", true);
            rmFile(workingDir, "logNCBIgeneLoad", false);
            rmFile(workingDir, "debug*", true);
            rmFile(workingDir, "report*", true);
            rmFile(workingDir, "toDelete.unl", false);
            rmFile(workingDir, "toMap.unl", false);
            rmFile(workingDir, "toLoad.unl", false);
            rmFile(workingDir, "length.unl", false);
            rmFile(workingDir, "noLength.unl", false);
            rmFile(workingDir, "seq.fasta", false);
            rmFile(workingDir, "zf_gene_info.gz", false);
            rmFile(workingDir, "gene2vega.gz", false);
            rmFile(workingDir, "gene2accession.gz", false);
            rmFile(workingDir, "RefSeqCatalog.gz", false);
            rmFile(workingDir, "RELEASE_NUMBER", false);
        }
    }

    private void openLoggingFileHandles() {
        try {
            LOG = new BufferedWriter(new FileWriter(new File(workingDir, "logNCBIgeneLoad")));
            STATS_PRIORITY1 = new BufferedWriter(new FileWriter(new File(workingDir, "reportStatistics_p1")));
            STATS_PRIORITY2 = new BufferedWriter(new FileWriter(new File(workingDir, "reportStatistics_p2")));
            STATS = new BufferedWriter(new FileWriter(new File(workingDir, "reportStatistics")));
            print(LOG, "Start ... \n");
        } catch (IOException e) {
            System.err.println("Cannot open logging file handle");
            System.err.println(e.getMessage());
            System.exit(3);
        }
    }

    private void printTimingInformation(int step) {
        String logLine = "Step " + step + " timestamp: " + nowToString();

        long timeDiff = 0;
        if (STEP_TIMESTAMP == 0) {
            STEP_TIMESTAMP = time();
        } else {
            Long lastTimeStamp = STEP_TIMESTAMP;
            STEP_TIMESTAMP = time();

            timeDiff = STEP_TIMESTAMP - lastTimeStamp;
            logLine += ". Time since last step: " + timeDiff + " seconds.";
        }
        print(logLine);
        print(LOG, logLine);
    }

    private void downloadNCBIFiles() {
    }

    private void prepareNCBIgeneLoadDatabaseQuery() {
//    # Global: $dbname
//    #--------------------------------------------------------------------------------------------------------------------
//    # Step 2: execute prepareNCBIgeneLoad.sql to prepare
//    #    1) a delete list, toDelete.unl
//    #    2) a list of ZFIN genes to be mapped, toMap.unl
//    #--------------------------------------------------------------------------------------------------------------------

        try {
            doSystemCommand("psql --echo-all -v ON_ERROR_STOP=1 -d " + env("DB_NAME") + " -a -f prepareNCBIgeneLoad.sql", "prepareLog1","prepareLog2");
        } catch (IOException | InterruptedException e) {
            reportErrAndExit("Auto from $instance: NCBI_gene_load.pl :: faile at prepareNCBIgeneLoad.sql - $_");
        }

        print(LOG, "Done with preparing the delete list and the list for mapping.\n\n");
        String subject = "Auto from $instance: NCBI_gene_load.pl :: prepareLog1 file";
        sendMailWithAttachedReport(env("SWISSPROT_EMAIL_ERR"),subject,"prepareLog1");

        subject = "Auto from $instance: NCBI_gene_load.pl :: prepareLog2 file";
        sendMailWithAttachedReport(env("SWISSPROT_EMAIL_ERR"),subject,"prepareLog2");
    }

    private void getMetricsOfDbLinksToDelete() {
        toDelete = new HashMap<>();
        ctToDelete = 0L;

        try {
            List<String> lines = FileUtils.readLines(new File(workingDir,"toDelete.unl"), StandardCharsets.UTF_8);
            for (String line : lines) {
                ctToDelete++;
                String dblinkIdToBeDeleted = line.trim();
                toDelete.put(dblinkIdToBeDeleted, 1);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (ctToDelete == 0) {
            String subjectLine = "Auto from ${INSTANCE}: NCBI_gene_load.pl :: the delete list, toDelete.unl, is empty";
            reportErrAndExit(subjectLine);
        }

    }
}
