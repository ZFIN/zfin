package org.zfin.datatransfer.ncbi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.persistence.Tuple;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.hibernate.query.NativeQuery;
import org.zfin.datatransfer.ncbi.port.PortHelper;
import org.zfin.datatransfer.ncbi.port.PortSqlHelper;
import org.zfin.datatransfer.report.model.LoadReportAction;
import org.zfin.datatransfer.report.model.LoadReportActionLink;
import org.zfin.datatransfer.report.model.LoadReportActionTag;
import org.zfin.datatransfer.util.CSVDiff;
import org.zfin.datatransfer.util.CSVToXLSXConverter;
import org.zfin.datatransfer.webservice.BatchNCBIFastaFetchTask;
import org.zfin.datatransfer.webservice.NCBIEfetch;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.exec.ExecProcess;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.uniprot.task.NcbiMatchThroughEnsemblTask;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import static org.zfin.datatransfer.ncbi.port.PortHelper.*;
import static org.zfin.datatransfer.ncbi.port.PortSqlHelper.getSqlForGeneAndRnagDbLinksFromFdbContId;
import static org.zfin.framework.HibernateUtil.currentSession;
import static org.zfin.properties.ZfinPropertiesEnum.SOURCEROOT;
import static org.zfin.util.DateUtil.nowToString;
import static org.zfin.util.FileUtil.createZipArchive;
import static org.zfin.util.FileUtil.writeToFileOrZip;
import static org.zfin.util.ZfinCollectionUtils.removeAndReturnDuplicateMapEntries;


public class NCBIDirectPort extends AbstractScriptWrapper {
    private static final String JSON_PLACEHOLDER_IN_TEMPLATE = "JSON_GOES_HERE";
    private static final long MAX_REPORT_FILE_SIZE = 50_000_000; // 50 MB
    public File workingDir;

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

    private static final String fdcontNCBIgeneId = "ZDB-FDBCONT-040412-1";
    private static final String fdcontVega = "ZDB-FDBCONT-040412-14";
    private static final String fdcontGenBankRNA = "ZDB-FDBCONT-040412-37";
    private static final String fdcontGenPept = "ZDB-FDBCONT-040412-42";
    private static final String fdcontGenBankDNA = "ZDB-FDBCONT-040412-36";
    private static final String fdcontRefSeqRNA = "ZDB-FDBCONT-040412-38";
    private static final String fdcontRefPept = "ZDB-FDBCONT-040412-39";
    private static final String fdcontRefSeqDNA = "ZDB-FDBCONT-040527-1";

    //enum to wrap these values
    public enum DBName {
        NCBI("NCBI"),
        NCBI_VEGA("NCBI Vega"),
        GENBANK_RNA("GenBank RNA"),
        GENPEPT("GenPept"),
        GENBANK_DNA("GenBank DNA"),
        REFSEQ_RNA("RefSeq RNA"),
        REFSEQ_PEPTIDE("RefSeq Peptide"),
        REFSEQ_DNA("RefSeq DNA");

        private final String displayName;
        DBName(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
        public static String getDisplayNameForForeignDB(String fdbcontId) {
            return switch (fdbcontId) {
                case fdcontNCBIgeneId -> NCBI.getDisplayName();
                case fdcontVega -> NCBI_VEGA.getDisplayName();
                case fdcontGenBankRNA -> GENBANK_RNA.getDisplayName();
                case fdcontGenPept -> GENPEPT.getDisplayName();
                case fdcontGenBankDNA -> GENBANK_DNA.getDisplayName();
                case fdcontRefSeqRNA -> REFSEQ_RNA.getDisplayName();
                case fdcontRefPept -> REFSEQ_PEPTIDE.getDisplayName();
                case fdcontRefSeqDNA -> REFSEQ_DNA.getDisplayName();
                default -> "Unknown Foreign DB";
            };
        }
    }

    public File beforeFile;
    public File afterFile;

    // used in eg. initializeDatabase
    private String dbname;
    private String dbhost;
    private String instance;
    private String username;
    private String password;
    // private String handle; // In Java, this will be managed by Hibernate session

    // used in eg. getMetricsOfDbLinksToDelete
    private Map<String, Integer>  toDelete;
    private Long ctToDelete;

    // used in eg. getRecordCounts
    private Map<String, String>  genesWithRefSeqBeforeLoad = new HashMap<>();
    public Integer ctGenesWithRefSeqBefore;
    public Integer numNCBIgeneIdBefore;
    public Integer numRefSeqRNABefore;
    public Integer numRefPeptBefore;
    public Integer numRefSeqDNABefore;
    public Integer numGenBankRNABefore;
    public Integer numGenPeptBefore;
    public Integer numGenBankDNABefore;
    public Integer numGenesRefSeqRNABefore;
    public Integer numGenesRefSeqPeptBefore;
    public Integer numGenesGenBankBefore;

    // used in eg. readZfinGeneInfoFile
    private int ctVegaIdsNCBI; // Changed to int
    private Map<String, List<String>> NCBIgeneWithMultipleVega = new HashMap<>();
    private Map<String, String> NCBIidsGeneSymbols = new HashMap<>(); // Value is String, not List<String> based on Perl
    private Map<String, String> geneSymbolsNCBIids = new HashMap<>(); // Value is String, not List<String>
    private Map<String, String> vegaIdsNCBIids = new HashMap<>(); // Value is String, not List<String>
    private Map<String, List<String>> vegaIdwithMultipleNCBIids = new HashMap<>();


    // used in eg. initializeSetsOfZfinRecords
    private Map<String, List<String>> supportedGeneZFIN = new HashMap<>();
    private Map<String, List<String>> supportingAccZFIN = new HashMap<>();
    private Map<String, List<String>> accZFINsupportingMoreThan1 = new HashMap<>();
    private Map<String, List<String>> geneZFINwithAccSupportingMoreThan1 = new HashMap<>();
    private Map<String, String> accZFINsupportingOnly1 = new HashMap<>(); // Changed value to String


    // used in eg. initializeSequenceLengthHash, lots of other places
    private Map<String, Integer> sequenceLength = new HashMap<>(); // Changed value to Integer

    // used in eg. parseGene2AccessionFile
    private Integer ctNoLength;
    private Integer ctNoLengthRefSeq;
    private Integer ctZebrafishGene2accession;
    private Map<String, List<String>>  GenBankDNAncbiGeneIds;
    private Map<String, String>  GenPeptNCBIgeneIds;
    private Map<String, String>  RefPeptNCBIgeneIds;
    private Map<String, String>  RefSeqDNAncbiGeneIds;
    private Map<String, String>  RefSeqRNAncbiGeneIds;
    private Map<String, String>  noLength;
    private Map<String, Set<String>>  supportedGeneNCBI;
    private Map<String, Set<String>>  supportingAccNCBI;

    //  used in eg. initializeHashOfNCBIAccessionsSupportingMultipleGenes
    private Map<String, List<String>>  accNCBIsupportingMoreThan1;
    private Map<String, String>  accNCBIsupportingOnly1;
    private Map<String, List<String>>  geneNCBIwithAccSupportingMoreThan1;

    //  used in eg. initializeMapOfZfinToNCBIgeneIds
    private Map<String, Map<String, String>>  oneToNZFINtoNCBI; // ZFIN Gene ID -> Map<NCBI Gene ID, Accession>
    private Map<String, String>  oneToOneZFINtoNCBI;
    private Map<String, List<String>>  genesZFINwithNoRNAFoundAtNCBI;

    //  used in eg. oneWayMappingNCBItoZfinGenes
    private Map<String, String>  oneToOneNCBItoZFIN;
    private Map<String, Map<String, String>> oneToNNCBItoZFIN; // NCBI Gene ID -> Map<ZFIN Gene ID, Accession>
    private Map<String, List<String>> genesNCBIwithAllAccsNotFoundAtZFIN;

    //  used in eg. prepare2WayMappingResults
    private Map<String, String>  mapped; //  the list of 1:1; key: ZDB gene Id; value: NCBI gene Id
    private Map<String, String>  mappedReversed; // NCBI gene Id -> ZDB gene Id
    private long ctOneToOneNCBI; // Count of 1:1 mappings

    private Map<String, String>  ncbiSupplementMap;
    private Map<String, String>  ncbiSupplementMapReversed;

    //  used in eg. writeNCBIgeneIdsMappedBasedOnGenBankRNA
    private long ctToLoad = 0L;

    private BufferedWriter TOLOAD;      // For toLoad.unl
    private BufferedWriter TO_PRESERVE; // For toPreserve.unl

    //  used in eg. getOneToNNCBItoZFINgeneIds
    private Map<String, Map<String, String>>  nToOne; // NCBI Gene ID -> Map<ZFIN Gene ID, Accession>
    private Map<String, Map<String, String>>  oneToN; // ZFIN Gene ID -> Map<NCBI Gene ID, Accession>

    //  used in eg. getNtoOneAndNtoNfromZFINtoNCBI
    private Map<String, String>  zdbGeneIdsNtoOneAndNtoN; // ZFIN Gene ID -> NCBI Gene ID (for N:1 cases)

    //  used in eg. buildVegaIDMappings
    private Map<String, String>  ZDBgeneAndVegaGeneIds; // ZFIN Gene ID -> Vega Gene ID
    private Map<String, String> VegaGeneAndZDBgeneIds; // Vega Gene ID -> ZFIN Gene ID
    private Map<String, List<String>>  ZDBgeneWithMultipleVegaGeneIds; // ZFIN Gene ID -> List<Vega Gene ID>
    private Map<String, List<String>>  vegaGeneIdWithMultipleZFINgenes; // Vega Gene ID -> List<ZFIN Gene ID>

    //  used in eg. writeCommonVegaGeneIdMappings
    private Map<String, String>  oneToOneViaVega; // NCBI Gene ID -> ZDB Gene ID

    //  used in eg. getGenBankAndRefSeqsWithZfinGenes
    private Map<String, String>  geneAccFdbcont; // Concatenated Key (gene+acc+fdbcont) -> dblink_zdb_id

    //  used in eg. initializeGenPeptAccessionsMap
    private Map<String, String>  GenPeptAttributedToNonLoadPub; // GenPept Acc -> Pub ZDB ID
    private Map<String, String>  GenPeptDbLinkIdAttributedToNonLoadPub; // GenPept Acc -> DBLink ZDB ID

    //  used in eg. processGenBankAccessionsAssociatedToNonLoadPubs
    private Map<String, String>  GenPeptsToLoad; // GenPept Acc -> ZFIN Gene ID

    //  readZfinGeneInfoFile
    private Map<String, String> geneZDBidsSymbols = new HashMap<>(); // Value is String

    private Long stepCount = 0L;
    private Long STEP_TIMESTAMP = 0L;
    private String LOAD_TIMESTAMP = nowToString("yyyy-MM-dd_HH-mm-ss");

    private BufferedWriter LOG;
    private BufferedWriter STATS_PRIORITY1;
    private BufferedWriter STATS_PRIORITY2;
    private BufferedWriter STATS;

    // After the load
    private Map<String, String> allGenPeptWithGeneAfterLoad = new HashMap<>();
    private Map<String, List<String>> GenPeptWithMultipleZDBgeneAfterLoad = new HashMap<>();
    public Integer numNCBIgeneIdAfter;
    public Integer numRefSeqRNAAfter;
    public Integer numRefPeptAfter;
    public Integer numRefSeqDNAAfter;
    public Integer numGenBankRNAAfter;
    public Integer numGenPeptAfter;
    public Integer numGenBankDNAAfter;
    public Integer numGenesRefSeqRNAAfter;
    public Integer numGenesRefSeqPeptAfter;
    public Integer numGenesGenBankAfter;
    private Map<String, String> genesWithRefSeqAfterLoad = new HashMap<>();
    public Integer ctGenesWithRefSeqAfter;
    private List<String> geneIDsNotInCurrentAnnotationRelease = null;
    private List<LoadReportAction> manyToManyWarningActions = new ArrayList<>();
    private List<LoadReportAction> oneToManyWarningActions = new ArrayList<>();
    private List<LoadReportAction> manyToOneWarningActions = new ArrayList<>();
    private Set<String> loggedMessages = new HashSet<>(); // To avoid duplicate log messages


    public static void main(String[] args) {
        NCBIDirectPort port = new NCBIDirectPort();
        port.initAll();
        port.run();
        System.exit(0);
    }

    private void run() {
        assertEnvironment("PGHOST", "DB_NAME", "PGUSER"); // PGUSER is needed for psql commands

        initializeWorkingDir();

        assertExpectedFilesExist();

        outputDate();

        // initializeDatabase(); // This is now called by initAll() via AbstractScriptWrapper

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

        readZfinGeneInfoFile();
        printTimingInformation(6);

        fetchGeneIDsNotInCurrentAnnotationReleaseSet();
        printTimingInformation(601);
        //----------------------------------------------------------------------------------------------------------------------
        // Step 5: Map ZFIN gene records to NCBI gene records based on GenBank RNA sequences
        //----------------------------------------------------------------------------------------------------------------------

        //-----------------------------------------
        // Step 5-1: initial set of ZFIN records
        //-----------------------------------------
        initializeSetsOfZfinRecordsPart1(); //
        initializeSetsOfZfinRecordsPart2(); //
        printTimingInformation(7); //

        initializeSequenceLengthHash();
        printTimingInformation(8);

//    #----------------------- 2) parse RefSeq-release#.catalog file to get the length for RefSeq sequences ----------------------

        parseRefSeqCatalogFileForSequenceLength();
        printTimingInformation(9);

        printSequenceLengthsCount();
        printTimingInformation(10);

        parseGene2AccessionFile();
        printTimingInformation(11);

        countNCBIGenesWithSupportingGenBankRNA();
        printTimingInformation(12);

        logSupportingAccNCBI();
        printTimingInformation(13);

        initializeHashOfNCBIAccessionsSupportingMultipleGenes();
        printTimingInformation(14);

        initializeMapOfZfinToNCBIgeneIds();
        printTimingInformation(15);

        logOneToZeroAssociations();
        printTimingInformation(16);

        oneWayMappingNCBItoZfinGenes();
        printTimingInformation(17);

        logGenBankDNAncbiGeneIds();
        printTimingInformation(18);

        prepare2WayMappingResults();
        printTimingInformation(19);

        addReverseMappedGenesFromNCBItoZFINFromSupplementaryLoad();
        printTimingInformation(20);

//    # -------- write the NCBI gene Ids mapped based on GenBank RNA accessions on toLoad.unl ------------
        writeNCBIgeneIdsMappedBasedOnGenBankRNA();
        printTimingInformation(21);

//    # -------- write the NCBI gene Ids mapped based supplementary ncbi load logic ------------
        writeNCBIgeneIdsMappedBasedOnSupplementaryLoad();
        printTimingInformation(22);

//    #------------------------ get 1:N list and N:N from ZFIN to NCBI -----------------------------
        manyToManyWarningActions = getOneToNNCBItoZFINgeneIds();
        printTimingInformation(23);

//    #------------------------ get N:1 list and N:N from ZFIN to NCBI -----------------------------
        getNtoOneAndNtoNfromZFINtoNCBI();
        printTimingInformation(24);

//    #--------------------- report 1:N ---------------------------------------------
        oneToManyWarningActions = reportOneToN();
        printTimingInformation(25);

//    #------------------- report N:1 -------------------------------------------------
        manyToOneWarningActions = reportNtoOne();
        printTimingInformation(26);


//    ##-----------------------------------------------------------------------------------
//    ## Step 6: map ZFIN gene records to NCBI gene Ids based on common Vega Gene Id
//    ##-----------------------------------------------------------------------------------

        buildVegaIDMappings();
        printTimingInformation(27);

        writeCommonVegaGeneIdMappings();
        printTimingInformation(28);

        calculateLengthForAccessionsWithoutLength();
        printTimingInformation(29);

        getGenBankAndRefSeqsWithZfinGenes();
        printTimingInformation(30);

        writeGenBankRNAaccessionsWithMappedGenesToLoad();
        printTimingInformation(31);

        initializeGenPeptAccessionsMap();
        printTimingInformation(32);

        processGenBankAccessionsAssociatedToNonLoadPubs();
        printTimingInformation(33);

        printGenPeptsAssociatedWithGeneAtZFIN();
        printTimingInformation(34);

        writeGenBankDNAaccessionsWithMappedGenesToLoad();
        printTimingInformation(35);

        writeRefSeqRNAaccessionsWithMappedGenesToLoad();
        printTimingInformation(36);

        writeRefPeptAccessionsWithMappedGenesToLoad();
        printTimingInformation(37);

        writeRefSeqDNAaccessionsWithMappedGenesToLoad();
        printTimingInformation(38);

        closeUnloadFiles();
        printTimingInformation(3850); // Arbitrary intermediate step number

        printStatsBeforeDelete();
        printTimingInformation(39);

        if (envTrue("EARLY_EXIT")) {
            System.out.println("Early exit requested, skipping delete and load.");
            print(LOG, "Early exit requested, skipping delete and load.\n");
            return;
        }

        executeDeleteAndLoadSQLFile();
        printTimingInformation(40);

        executeMarkerAssemblyUpdate();
        printTimingInformation(4001);

        //Add call to Christian's script here for updating by the gff3_ncbi table

        sendLoadLogs(); // This was called if loadNCBIgeneAccs.sql failed, good to call after too.
        printTimingInformation(41);

        captureAfterState();
        printTimingInformation(4101);

        captureMoreWarnings();
        printTimingInformation(4102);

        reportAllLoadStatistics();
        printTimingInformation(42);

        emailLoadReports();
        printTimingInformation(43);

        // Sort noLength.unl so we can compare the results with the previous run.
        doSystemCommand(List.of("sort",
                "-o",
                new File(workingDir, "noLength.unl").getAbsolutePath(),
                new File(workingDir, "noLength.unl").getAbsolutePath()),
                "prepareLog1",
                "prepareLog2");

        outputDate(); // Corresponds to system("/bin/date");

        cleanupForJenkins();
        printTimingInformation(44);

        System.out.println("All done!");
        print(LOG, "\n\nAll done! \n\n\n");

        try {
            if (LOG != null) LOG.close();
            if (STATS_PRIORITY1 != null) STATS_PRIORITY1.close();
            if (STATS_PRIORITY2 != null) STATS_PRIORITY2.close();
            if (STATS != null) STATS.close();
            if (TOLOAD != null) TOLOAD.close(); // Ensure these are closed if not done in closeUnloadFiles
            if (TO_PRESERVE != null) TO_PRESERVE.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void cleanupForJenkins() {
        beforeFile = new File(workingDir, "before_load.csv");
        afterFile = new File(workingDir, "after_load.csv");
        File compressedBeforeAfterFile = new File(workingDir, "before_after_load_csvs.zip");
        try {
            createZipArchive(compressedBeforeAfterFile, List.of(beforeFile, afterFile));
            beforeFile.delete();
            afterFile.delete();
        } catch (IOException e) {
            print(LOG, "Error while creating zip archive for before_after_load_csvs.zip");
        }

        //zip debug files: debug1 debug10 debug12 debug13 debug14 debug15 debug16.json debug17
        //debug2 debug3 debug4 debug5 debug5a debug6
        List<File> debugFilesToZip = List.of("debug1", "debug10", "debug12", "debug13", "debug14", "debug15", "debug16.json", "debug17",
                "debug2", "debug3", "debug4", "debug5", "debug5a", "debug6", "java_debug_readZfinGeneInfoFile.json").stream()
                .map(filename -> new File(workingDir, filename))
                .filter(File::exists)
                .collect(Collectors.toList());
        try {
            createZipArchive(new File(workingDir, "debug_files.zip"), debugFilesToZip);
            debugFilesToZip.forEach(File::delete);
        } catch (IOException e) {
            print(LOG, "Error while creating zip archive for debug_files.zip");
        }

        //zip log files
        List<File> logFilesToZip = List.of("loadLog1.sql", "loadLog2.sql", "prepareLog1", "prepareLog2", "logNCBIgeneLoad").stream()
                .map(filename -> new File(workingDir, filename))
                .filter(File::exists)
                .collect(Collectors.toList());
        try {
            createZipArchive(new File(workingDir, "log_files.zip"), logFilesToZip);
            logFilesToZip.forEach(File::delete);
        } catch (IOException e) {
            print(LOG, "Error while creating zip archive for log_files.zip");
        }

        // zip unload files
        //length.unl noLength.unl notInCurrentReleaseGeneIDs.unl referenceProteinDeletes.unl toDelete.unl toLoad.unl toMap.unl toPreserve.unl
        List<File> unloadFilesToZip = List.of("length.unl", "noLength.unl", "notInCurrentReleaseGeneIDs.unl",
                "referenceProteinDeletes.unl", "toDelete.unl", "toLoad.unl", "toMap.unl", "toPreserve.unl").stream()
                .map(filename -> new File(workingDir, filename))
                .filter(File::exists)
                .collect(Collectors.toList());
        try {
            createZipArchive(new File(workingDir, "unload_files.zip"), unloadFilesToZip);
            unloadFilesToZip.forEach(File::delete);
        } catch (IOException e) {
            print(LOG, "Error while creating zip archive for unload_files.zip");
        }

    }

    /**
     * Capture additional warnings from post-run reports not captured during the main load process.
     */
    private void captureMoreWarnings() {
        // Report source: post_run_n_to_1_zdb_to_ncbi.csv
        File warningsReportFile = new File(workingDir, "post_run_n_to_1_zdb_to_ncbi.csv");
        try {
            List<String> lines = Files.readAllLines(warningsReportFile.toPath());
            //headers: gene_id	ncbi_id	dblink_zdb_id	load_pub	existing
            List<Map<String, String>> records = new ArrayList<>();
            for (String line : lines.subList(1, lines.size())) { // Skip header
                String[] parts = line.split(",");
                String zdbGeneId = parts[0];
                String ncbiId = parts[1];
                String dblinkZdbId = parts[2];
                String loadPub = parts[3];
                String existing = parts[4];

                String message = String.format("N:1 Warning - ZFIN Gene %s mapped to NCBI Gene %s\n" +
                                "via DBLink %s\n" +
                                "(Load Pub: %s)",
                        zdbGeneId, ncbiId, dblinkZdbId, loadPub);
                print(LOG, message);
                records.add(Map.of(
                        "zdbGeneId", zdbGeneId,
                        "ncbiId", ncbiId,
                        "dblinkZdbId", dblinkZdbId,
                        "loadPub", loadPub,
                        "existing", existing,
                        "message", message
                ));
            }

            //group by ncbi id
            Map<String, List<Map<String, String>>> recordsByNcbiId = records.stream()
                    .collect(Collectors.groupingBy(r -> r.get("ncbiId")));
            for (Map.Entry<String, List<Map<String, String>>> entry : recordsByNcbiId.entrySet()) {
                String ncbiId = entry.getKey();
                List<Map<String, String>> recs = entry.getValue();
                if (recs.size() > 1) {
                    String combinedMessage = "N:1 Warning - NCBI Gene " + ncbiId + " mapped to multiple ZFIN Genes: " +
                            recs.stream()
                                    .map(r -> r.get("zdbGeneId") + " via DBLink " + r.get("dblinkZdbId") +
                                            " (Load Pub: " + r.get("loadPub") + ", Existing: " + r.get("existing") + ")")
                                    .collect(Collectors.joining("; "));
                    print(LOG, combinedMessage);
                    LoadReportAction action = new LoadReportAction();
                    action.setType(LoadReportAction.Type.WARNING);
                    action.setSubType("N to 1");
                    action.setAccession(ncbiId);
                    List<String> geneIDs = recs.stream()
                            .map(r -> r.get("zdbGeneId"))
                            .distinct().toList();
                    String geneIDsCsv = String.join(", ", geneIDs);
                    action.setGeneZdbID(geneIDsCsv);
                    action.setDetails(combinedMessage);
                    action.addRelatedActionsKeys(ncbiId);
                    geneIDs.forEach(action::addRelatedActionsKeys);
                    manyToOneWarningActions.add(action);
                } else {
                    Map<String, String> r = recs.get(0);
                    LoadReportAction action = new LoadReportAction();
                    action.setType(LoadReportAction.Type.WARNING);
                    action.setSubType("N to 1");
                    action.setAccession(ncbiId);
                    action.setGeneZdbID(r.get("zdbGeneId"));
                    action.setDetails(r.get("message"));
                    action.addRelatedActionsKeys(ncbiId);
                    action.addRelatedActionsKeys(r.get("zdbGeneId"));
                    manyToOneWarningActions.add(action);
                }
            }

        } catch (IOException e) {
        }
    }

    private void initializeWorkingDir() {
        String workingDirEnvironmentVariable = System.getenv("WORKING_DIR");
        if (StringUtils.isNotEmpty(workingDirEnvironmentVariable)) {
            workingDir = new File(workingDirEnvironmentVariable);
        } else {
            String rootPath = SOURCEROOT.value();
            workingDir = new File(rootPath, "server_apps/data_transfer/NCBIGENE/");
        }
        if (!workingDir.exists() && !workingDir.mkdirs()) {
            System.err.println("Could not create working directory: " + workingDir.getAbsolutePath());
            System.exit(1);
        }
        if (!workingDir.isDirectory()) {
            System.err.println("Working directory is not a directory: " + workingDir.getAbsolutePath());
            System.exit(1);
        }
        System.setProperty("user.dir", workingDir.getAbsolutePath());
        // LOG might not be initialized yet, so print to console first or delay log print.
        // For now, will assume LOG is initialized before this is called or handle null if not.
        if (LOG != null) {
            print(LOG, "Working directory set to: " + workingDir.getAbsolutePath() + "\n");
        } else {
            System.out.println("Working directory set to: " + workingDir.getAbsolutePath());
        }
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

    private static void outputDate() {
        System.out.println(nowToString("yyyy-MM-dd HH:mm:ss"));
    }

    private void removeOldFiles() {
        String filesToRemoveMessage = "Removing prepareLog* loadLog* logNCBIgeneLoad debug* report* toDelete.unl toMap.unl toLoad.unl length.unl noLength.unl";
        System.out.println(filesToRemoveMessage);
        rmFiles(workingDir, List.of(
                "*.csv",
                "*.danio.*",
                "*.html",
                "*.json",
                "*.log",
                "*.xlsx",
                "*md5",
                "after_db_link.csv",
                "after_load*csv",
                "after_recattrib.csv",
                "before_db_link.csv",
                "before_load*csv",
                "before_recattrib.csv",
                "debug*",
                "java_debug_readZfinGeneInfoFile.json",
                "loadLog*",
                "logNCBIgeneLoad",
                "prepareLog*",
                "report*",
                "length.unl",
                "noLength.unl",
                "toDelete.unl",
                "toLoad.unl",
                "toMap.unl"));

        if (!envTrue("SKIP_DOWNLOADS")) {
            if (!envTrue("NO_SLEEP")) {
                System.out.println("Removing old files in 30 seconds...");
                try {
                    Thread.sleep(30_000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
            }
            filesToRemoveMessage = "Removing seq.fasta zf_gene_info.gz gene2vega.gz gene2accession.gz RefSeqCatalog.gz RELEASE_NUMBER";
            System.out.println(filesToRemoveMessage);
            if (LOG != null) print(LOG, filesToRemoveMessage + "\n");

            if (!envTrue("SKIP_EFETCH")) {
                rmFile(workingDir, "seq.fasta", false);
            }
            rmFile(workingDir, "zf_gene_info.gz", false);
            rmFile(workingDir, "gene2vega.gz", false);
            rmFile(workingDir, "gene2accession.gz", false);
            rmFile(workingDir, "RefSeqCatalog.gz", false);
            rmFile(workingDir, "RELEASE_NUMBER", false);
        }
    }

    private void openLoggingFileHandles() {
        try {
            // Ensure workingDir is initialized
            if (workingDir == null) {
                // This is a fallback, ideally initializeWorkingDir is called first.
                // However, openLoggingFileHandles is called early.
                String rootPath = SOURCEROOT.value();
                workingDir = new File(rootPath, "server_apps/data_transfer/NCBIGENE/");
                if (!workingDir.exists() && !workingDir.mkdirs()) {
                    System.err.println("Could not create working directory for logs: " + workingDir.getAbsolutePath());
                    System.exit(3); // Exit if cannot create dir for logs
                }
            }

            LOG = new BufferedWriter(new FileWriter(new File(workingDir, "logNCBIgeneLoad")));
            STATS_PRIORITY1 = new BufferedWriter(new FileWriter(new File(workingDir, "reportStatistics_p1")));
            STATS_PRIORITY2 = new BufferedWriter(new FileWriter(new File(workingDir, "reportStatistics_p2")));
            STATS = new BufferedWriter(new FileWriter(new File(workingDir, "reportStatistics")));

            TOLOAD = new BufferedWriter(new FileWriter(new File(workingDir, "toLoad.unl")));
            TO_PRESERVE = new BufferedWriter(new FileWriter(new File(workingDir, "toPreserve.unl")));

            print(LOG, "Start ... \n");
        } catch (IOException e) {
            System.err.println("Cannot open logging file handle: " + e.getMessage());
            // e.printStackTrace(); // For more detailed error info during development
            System.exit(3);
        }
    }

    private void downloadNCBIFiles() {
        if (LOG != null) print(LOG, "Downloading NCBI files...\n"); else System.out.println("Downloading NCBI files...");
        String releaseNum = getReleaseNumber();
        if (LOG != null) print(LOG, "RefSeq Catalog Release Number is " + releaseNum + ".\n\n"); else System.out.println("RefSeq Catalog Release Number is " + releaseNum + ".\n\n");

        downloadNCBIFilesForRelease(releaseNum);
        if (LOG != null) print(LOG, "Done with downloading.\n\n"); else System.out.println("Done with downloading.\n\n");

        if (!envTrue("SKIP_DOWNLOADS")) {
            assertFileExistsAndNotEmpty("zf_gene_info.gz", "ERROR with download: zf_gene_info.gz missing after download attempt");
            assertFileExistsAndNotEmpty("gene2accession.gz", "ERROR with download: gene2accession.gz missing after download attempt");
            // RefSeqCatalog.gz might be filtered to RefSeqCatalog.danio.*.gz, so check original before asserting
            assertFileExistsAndNotEmpty("RefSeqCatalog.gz", "ERROR with download: RefSeqCatalog.gz (original) missing after download attempt");
            assertFileExistsAndNotEmpty("gene2vega.gz", "ERROR with download: gene2vega.gz missing after download attempt");
        }
    }

    private void captureBeforeState() {
        beforeFile = new File(workingDir, "before_load.csv");
        captureState(beforeFile);
    }

    /**
     * Captures the state of relevant tables before or after the load.
     * The output is written to the specified outputFile in CSV format.
     * The relevant tables are:
     * - db_link
     * - record_attribution
     * - marker_assembly
     * - marker_annotation_status
     * @param outputFile
     */
    private void captureState(File outputFile) {
        try {
            String sqlQuery = "\\copy (" +
                    """
                    select d.*, string_agg(r.recattrib_source_zdb_id, '|' order by r.recattrib_source_zdb_id) as recattrib_source_zdb_id,
                    string_agg(ma_a_pk_id::varchar, '|' order by ma_a_pk_id) as marker_assemblies,
                    string_agg(mas_vt_pk_id::varchar, '|') as marker_annotation_status
                     from db_link d
                     left join record_attribution r on d.dblink_zdb_id = r.recattrib_data_zdb_id
                     left join marker_assembly on d.dblink_linked_recid = ma_mrkr_zdb_id
                     left join marker_annotation_status on d.dblink_linked_recid = mas_mrkr_zdb_id
                     group by dblink_linked_recid,dblink_acc_num,dblink_info,dblink_zdb_id,dblink_acc_num_display,dblink_length,dblink_fdbcont_zdb_id
                     order by dblink_linked_recid, dblink_acc_num
                    """ +
                    ") to  '" + outputFile.getAbsolutePath() + "'  with csv header ";

            //remove newlines from sqlQuery
            sqlQuery = sqlQuery.replaceAll("\\s+", " ");

            HibernateUtil.createTransaction();
            doSystemCommand(
                    List.of(
                            "psql", "--echo-all", "-v", "ON_ERROR_STOP=1", "-U", env("PGUSER"), "-h", env("PGHOST"), "-d", env("DB_NAME"), "-a", "-c", sqlQuery
                    ), "prepareLog1", "prepareLog2");
            HibernateUtil.flushAndCommitCurrentSession();
        } catch (Exception e) {
            HibernateUtil.rollbackTransaction();
            reportErrAndExit("Auto from " + instance + ": NCBI_gene_load.pl :: failed at before capture - " + e.getMessage());
        }
        print(LOG, "Done with capturing before state.\n\n");

    }

    private void prepareNCBIgeneLoadDatabaseQuery() {
        String sqlFile = "prepareNCBIgeneLoad.sql";
        // Construct path relative to SOURCEROOT/server_apps/data_transfer/NCBIGENE/
        File sqlFilePath = new File(new File(SOURCEROOT.value(), "server_apps/data_transfer/NCBIGENE/"), sqlFile);


        if (!sqlFilePath.exists()) {
            reportErrAndExit("SQL file not found: " + sqlFilePath.getAbsolutePath());
            return;
        }

        try {
            HibernateUtil.createTransaction();
            doSystemCommand(
                    List.of(
                            "psql", "--echo-all", "-v", "ON_ERROR_STOP=1",
                            "-U", env("PGUSER"),
                            "-h", env("PGHOST"),
                            "-d", env("DB_NAME"),
                            "-a", "-f", sqlFilePath.getAbsolutePath()
                    ), "prepareLog1", "prepareLog2"
            );
            HibernateUtil.flushAndCommitCurrentSession();
        } catch (Exception e) {
            HibernateUtil.rollbackTransaction();
            reportErrAndExit("Auto from " + instance + ": NCBI_gene_load.pl :: failed at prepareNCBIgeneLoad.sql - " + e.getMessage());
        }

        print(LOG, "Done with preparing the delete list and the list for mapping.\n\n");
        String subjectPrefix = "Auto from " + instance + ": NCBI_gene_load.pl :: ";
        sendMailWithAttachedReport(env("SWISSPROT_EMAIL_ERR"),subjectPrefix + "prepareLog1 file","prepareLog1", workingDir);
        sendMailWithAttachedReport(env("SWISSPROT_EMAIL_ERR"),subjectPrefix + "prepareLog2 file","prepareLog2", workingDir);
    }

    private void getMetricsOfDbLinksToDelete() {
        toDelete = new HashMap<>();
        ctToDelete = 0L;
        File toDeleteFile = new File(workingDir,"toDelete.unl");

        try {
            if (!toDeleteFile.exists()) {
                print(LOG, "Warning: toDelete.unl does not exist. ctToDelete will be 0.\n");
                // The original script would fail if ctToDelete is 0 later,
                // but only after trying to read the file.
                // If the file doesn't exist, ctToDelete remains 0.
            } else {
                List<String> lines = FileUtils.readLines(toDeleteFile, StandardCharsets.UTF_8);
                for (String line : lines) {
                    if (StringUtils.isNotBlank(line)) {
                        ctToDelete++;
                        String dblinkIdToBeDeleted = line.trim();
                        toDelete.put(dblinkIdToBeDeleted, 1); // Value is just a marker like in Perl
                    }
                }
            }
        } catch (IOException e) {
            reportErrAndExit("Error reading toDelete.unl: " + e.getMessage());
        }


        if (ctToDelete == 0) { // Perl condition: if ($ctToDelete == 0)
            String subjectLine = "Auto from " + instance + ": NCBI_gene_load.pl :: the delete list, toDelete.unl, is empty";
            // print(LOG, "\nThe delete list, toDelete.unl is empty. Something is wrong.\n\n"); // Original Perl log.
            // The original script *conditionally* reports error. If toDelete.unl is missing, it's fine.
            // If it's present but empty, it's an error.
            // My current Java logic doesn't differentiate this way: if ctToDelete is 0, it's an error.
            // Let's refine to match Perl: error only if file exists and is empty or unreadable leading to 0 count.
            if (toDeleteFile.exists() && toDeleteFile.length() == 0) { // File exists but is empty
                reportErrAndExit(subjectLine + " (file is present but empty).");
            } else if (toDeleteFile.exists() && toDeleteFile.length() > 0 && ctToDelete == 0) { // File exists, not empty, but parsed to 0
                reportErrAndExit(subjectLine + " (file is present and non-empty, but parsed to 0).");
            } else if (!toDeleteFile.exists()) {
                print(LOG, "Note: The delete list (toDelete.unl) does not exist. ctToDelete is 0. Continuing operation.\n");
            } else {
                // This case (ctToDelete == 0, but file exists and has content that didn't parse to items)
                // might be redundant with the above, but covers other scenarios.
                // For now, if ctToDelete is 0 and we didn't hit "does not exist", it's potentially an issue.
                // The Perl logic is: if ($ctToDelete == 0) { reportErrAndExit }
                // This implies *any* scenario where ctToDelete is 0 is an error. Let's stick to that for now.
                reportErrAndExit(subjectLine);
            }
        }
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

        //RefSeq RNA
        sql = getSqlForGeneAndRnagDbLinksFromFdbContId(fdcontRefSeqRNA);
        numRefSeqRNABefore = PortSqlHelper.countData(currentSession(), sql);

        // RefPept
        sql = getSqlForGeneAndRnagDbLinksFromFdbContId(fdcontRefPept);
        numRefPeptBefore = PortSqlHelper.countData(currentSession(), sql);

        //RefSeq DNA
        sql = getSqlForGeneAndRnagDbLinksFromFdbContId(fdcontRefSeqDNA);
        numRefSeqDNABefore = PortSqlHelper.countData(currentSession(), sql);

        // GenBank RNA (only those loaded - excluding curated ones)
        sql = PortSqlHelper.getSqlForGeneAndRnagDbLinksSupportedByLoadPubsFromFdbContId(
                fdcontGenBankRNA, pubMappedbasedOnRNA, pubMappedbasedOnVega, pubMappedbasedOnNCBISupplement
        );
        numGenBankRNABefore = PortSqlHelper.countData(currentSession(), sql);

        // GenPept (only those loaded - excluding curated ones)
        sql = PortSqlHelper.getSqlForGeneAndRnagDbLinksSupportedByLoadPubsFromFdbContId(
                fdcontGenPept, pubMappedbasedOnRNA, pubMappedbasedOnVega, pubMappedbasedOnNCBISupplement
        );
        numGenPeptBefore = PortSqlHelper.countData(currentSession(), sql);

        // GenBank DNA (only those loaded - excluding curated ones)
        sql = PortSqlHelper.getSqlForGeneAndRnagDbLinksSupportedByLoadPubsFromFdbContId(
                fdcontGenBankDNA, pubMappedbasedOnRNA, pubMappedbasedOnVega, pubMappedbasedOnNCBISupplement
        );
        numGenBankDNABefore = PortSqlHelper.countData(currentSession(), sql);

        // number of genes with RefSeq RNA
        sql = StringSubstitutor.replace(
                """
            select distinct dblink_linked_recid
            from db_link
            where dblink_fdbcont_zdb_id = '${fdcontRefSeqRNA}'
            and dblink_acc_num like 'NM_%'
            and (dblink_linked_recid like 'ZDB-GENE%' or dblink_linked_recid like '%RNAG%')
            """,  Map.of("fdcontRefSeqRNA", fdcontRefSeqRNA));
        numGenesRefSeqRNABefore = PortSqlHelper.countData(currentSession(), sql);


        // number of genes with RefPept
        sql = StringSubstitutor.replace("""
            select distinct dblink_linked_recid
            from db_link
            where dblink_fdbcont_zdb_id = '${fdcontRefPept}'
            and dblink_acc_num like 'NP_%'
            and (dblink_linked_recid like 'ZDB-GENE%' or dblink_linked_recid like '%RNAG%')
            """, Map.of("fdcontRefPept", fdcontRefPept));
        numGenesRefSeqPeptBefore = PortSqlHelper.countData(currentSession(), sql);

        // number of genes with GenBank
        sql = """
            select distinct dblink_linked_recid
            from db_link, foreign_db_contains, foreign_db
            where dblink_fdbcont_zdb_id = fdbcont_zdb_id
            and fdbcont_fdb_db_id = fdb_db_pk_id
            and fdb_db_name = 'GenBank'
            and (dblink_linked_recid like 'ZDB-GENE%' or dblink_linked_recid like '%RNAG%')
            """;
        numGenesGenBankBefore = PortSqlHelper.countData(currentSession(), sql);
    }

    private void readZfinGeneInfoFile() {
        int ctLines = 0;
        vegaIdsNCBIids = new HashMap<>();
        NCBIidsGeneSymbols = new HashMap<>();
        geneSymbolsNCBIids = new HashMap<>(); // Added initialization
        vegaIdwithMultipleNCBIids = new HashMap<>();
        NCBIgeneWithMultipleVega = new HashMap<>();
        ctVegaIdsNCBI = 0;

        File zfGeneInfoFile = new File(workingDir, "zf_gene_info.gz");
        try (
                FileInputStream fis = new FileInputStream(zfGeneInfoFile);
                GZIPInputStream gzis = new GZIPInputStream(fis);
                InputStreamReader reader = new InputStreamReader(gzis);
                BufferedReader br = new BufferedReader(reader)
        ) {
            String line;
            if (br.ready()) {
                br.readLine(); //skip first line
                ctLines = 1; // Accounts for the header line being read
            }

            while ((line = br.readLine()) != null) {
                ctLines++;
                String[] fields = line.split("\t", -1); // Keep trailing empty fields

                if (fields.length < 15) continue;

                String taxId = fields[0];
                if (!"7955".equals(taxId)) {
                    continue;
                }

                String ncbiGeneId = fields[1];
                String symbol = fields[2];

                geneSymbolsNCBIids.put(symbol, ncbiGeneId);
                NCBIidsGeneSymbols.put(ncbiGeneId, symbol);

                String dbXrefs = fields[5];
                Pattern vegaPattern = Pattern.compile("Vega:(OTTDARG[0-9]+)");
                List<String> foundVegaIds = new ArrayList<>();
                Matcher matcher = vegaPattern.matcher(dbXrefs);
                while (matcher.find()) {
                    foundVegaIds.add(matcher.group(1));
                }

                if (foundVegaIds.size() > 1) {
                    // Perl stores the raw dbXrefs string, split by '|' into a list for NCBIgeneWithMultipleVega
                    // For Java, we'll store the list of Vega IDs extracted if that's more useful,
                    // or stick to the Perl logic of storing the dbXrefs field itself.
                    // The Perl code was: $NCBIgeneWithMultipleVega{$NCBIgeneId} = $dbXrefs;
                    // And prints $dbXrefs. So we store the raw field.
                    NCBIgeneWithMultipleVega.put(ncbiGeneId, List.of(dbXrefs)); // Storing as a list with one element: the raw field
                    print(LOG, "\nMultiple Vega: \n " + ncbiGeneId + "\t" + dbXrefs + "\n");
                    continue;
                }

                if (foundVegaIds.size() == 1) {
                    String vegaIdNCBI = foundVegaIds.get(0);
                    ctVegaIdsNCBI++;

                    if (vegaIdwithMultipleNCBIids.containsKey(vegaIdNCBI) ||
                        (vegaIdsNCBIids.containsKey(vegaIdNCBI) && !vegaIdsNCBIids.get(vegaIdNCBI).equals(ncbiGeneId))) {
                        if (!vegaIdwithMultipleNCBIids.containsKey(vegaIdNCBI)) {
                            String firstNCBIgeneIdFound = vegaIdsNCBIids.get(vegaIdNCBI);
                            vegaIdwithMultipleNCBIids.put(vegaIdNCBI, new ArrayList<>(Arrays.asList(firstNCBIgeneIdFound, ncbiGeneId)));
                        } else {
                            vegaIdwithMultipleNCBIids.get(vegaIdNCBI).add(ncbiGeneId);
                        }
                    }
                    vegaIdsNCBIids.put(vegaIdNCBI, ncbiGeneId);
                }
            }
        } catch (IOException e) {
            reportErrAndExit("Cannot open or read zf_gene_info.gz: " + e.getMessage());
        }
        // ctLines was incremented for each line read, including header.
        // Perl decrements $ctlines (which starts at 0) after loop if header was counted.
        // Here, ctLines includes header, so if we want lines *of data*:
        long dataLinesInZfGeneInfo = ctLines > 0 ? ctLines -1 : 0;


        print(LOG, "\nTotal number of records on NCBI's Danio_rerio.gene_info file: " + dataLinesInZfGeneInfo + "\n\n");
        if (ctVegaIdsNCBI > 0) {
            print(LOG, "\nctVegaIdsNCBI from zf_gene_info:  " + ctVegaIdsNCBI + "\n\n");
        }
        print(STATS_PRIORITY1, "\nTotal number of records on NCBI's Danio_rerio.gene_info file: " + dataLinesInZfGeneInfo + "\n");
        if (ctVegaIdsNCBI > 0) {
            print(STATS_PRIORITY1, "\nNumber of Vega Gene Id/NCBI Gene Id pairs on Danio_rerio.gene_info file: " + ctVegaIdsNCBI + "\n\n");
        }

        if (ctVegaIdsNCBI == 0) {
            print(LOG, "No Vega IDs found in zf_gene_info. Parsing gene2vega.gz\n");
            ctLines = 0; // Reset for gene2vega file
            // ctVegaIdsNCBI is already 0
            File gene2VegaFile = new File(workingDir, "gene2vega.gz");
            try (
                    FileInputStream fis = new FileInputStream(gene2VegaFile);
                    GZIPInputStream gzis = new GZIPInputStream(fis);
                    InputStreamReader reader = new InputStreamReader(gzis);
                    BufferedReader br = new BufferedReader(reader)
            ) {
                String line;
                if (br.ready()) {
                    br.readLine(); // Skip header
                    ctLines = 1;
                }
                while ((line = br.readLine()) != null) {
                    ctLines++;
                    String[] fields = line.split("\t", -1);
                    if (fields.length < 3) continue;

                    String taxId = fields[0];
                    if (!"7955".equals(taxId)) {
                        continue;
                    }

                    String ncbiGeneId = fields[1];
                    String vegaIdNCBI = fields[2];
                    ctVegaIdsNCBI++;

                    if (vegaIdwithMultipleNCBIids.containsKey(vegaIdNCBI) ||
                        (vegaIdsNCBIids.containsKey(vegaIdNCBI) && !vegaIdsNCBIids.get(vegaIdNCBI).equals(ncbiGeneId))) {
                        if (!vegaIdwithMultipleNCBIids.containsKey(vegaIdNCBI)) {
                            String firstNCBIgeneIdFound = vegaIdsNCBIids.get(vegaIdNCBI);
                            vegaIdwithMultipleNCBIids.put(vegaIdNCBI, new ArrayList<>(Arrays.asList(firstNCBIgeneIdFound, ncbiGeneId)));
                        } else {
                            vegaIdwithMultipleNCBIids.get(vegaIdNCBI).add(ncbiGeneId);
                        }
                    }
                    vegaIdsNCBIids.put(vegaIdNCBI, ncbiGeneId);
                }

            } catch (IOException e) {
                reportErrAndExit("Cannot open or read gene2vega.gz: " + e.getMessage());
            }
            long dataLinesInGene2Vega = ctLines > 0 ? ctLines - 1 : 0;

            print(LOG, "\nTotal number of records on NCBI's gene2vega file: " + dataLinesInGene2Vega + "\n\n");
            if (ctVegaIdsNCBI > 0) {
                print(LOG, "\nctVegaIdsNCBI from gene2vega:  " + ctVegaIdsNCBI + "\n\n");
            }
            print(STATS_PRIORITY1, "\nTotal number of records on NCBI's gene2vega file: " + dataLinesInGene2Vega + "\n");
            if (ctVegaIdsNCBI > 0) {
                print(STATS_PRIORITY1, "\nNumber of Vega Gene Id/NCBI Gene Id pairs on gene2vega file: " + ctVegaIdsNCBI + "\n\n");
            }
        }

        if (ctVegaIdsNCBI > 0 && !vegaIdwithMultipleNCBIids.isEmpty()) { // Check if the map is not empty
            print(STATS_PRIORITY2, "On NCBI's relevant file (zf_gene_info or gene2vega), the following Vega Ids correspond to more than 1 NCBI genes\n\n");
            print(LOG, "On NCBI's relevant file (zf_gene_info or gene2vega), the following Vega Ids correspond to more than 1 NCBI genes\n");

            long ctVegaIdWithMultipleNCBIgene = vegaIdwithMultipleNCBIids.size();
            for (Map.Entry<String, List<String>> entry : vegaIdwithMultipleNCBIids.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey()).collect(Collectors.toList())) {
                String vega = entry.getKey();
                List<String> ncbiGenes = entry.getValue();
                String ncbiGenesStr = String.join(" ", ncbiGenes);
                print(LOG, vega + " " + ncbiGenesStr + "\n");
                print(STATS_PRIORITY2, vega + " " + ncbiGenesStr + "\n");
            }
            print(LOG, "\nctVegaIdWithMultipleNCBIgene = " + ctVegaIdWithMultipleNCBIgene + "\n\n");
        } else if (ctVegaIdsNCBI > 0) {
            print(LOG, "No Vega IDs found to correspond to more than 1 NCBI gene, or ctVegaIdsNCBI is zero.\n");
        }


        String sqlGeneZDBidsSymbols = """
            select mrkr_zdb_id, mrkr_abbrev from marker
            where (mrkr_zdb_id like 'ZDB-GENE%' or mrkr_zdb_id like '%RNAG%')
            and mrkr_abbrev not like 'WITHDRAWN%'
            """;
        NativeQuery<Tuple> query = currentSession().createNativeQuery(sqlGeneZDBidsSymbols, Tuple.class);
        List<Tuple> results = query.list();
        for (Tuple tuple : results) {
            String zdbId = (String) tuple.get(0);
            String symbol = (String) tuple.get(1);
            geneZDBidsSymbols.put(zdbId, symbol);
        }

        //write for debugging purposes
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode rootNode = mapper.createObjectNode();
        rootNode.set("vegaIdsNCBIids", mapper.valueToTree(vegaIdsNCBIids));
        rootNode.set("NCBIidsGeneSymbols", mapper.valueToTree(NCBIidsGeneSymbols));
        rootNode.set("geneSymbolsNCBIids", mapper.valueToTree(geneSymbolsNCBIids));
        rootNode.set("vegaIdwithMultipleNCBIids", mapper.valueToTree(vegaIdwithMultipleNCBIids));
        rootNode.set("NCBIgeneWithMultipleVega", mapper.valueToTree(NCBIgeneWithMultipleVega));
        rootNode.put("ctVegaIdsNCBI", Integer.toString(ctVegaIdsNCBI));

        try {
            mapper.writeValue(new File(workingDir,"java_debug_readZfinGeneInfoFile.json"), rootNode);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void initializeSetsOfZfinRecordsPart1() {
        supportedGeneZFIN = new HashMap<>();
        supportingAccZFIN = new HashMap<>();

        File toMapFile = new File(workingDir, "toMap.unl");
        long ctSupportedZFINgenes = 0;
        // Map<String, Integer> zfinGenes = new HashMap<>(); // Not strictly needed for the maps themselves

        try (BufferedReader reader = new BufferedReader(new FileReader(toMapFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (StringUtils.isNotBlank(line)) {
                    ctSupportedZFINgenes++;
                    String trimmedLine = line.trim();
                    String[] parts = trimmedLine.split("\\|");
                    if (parts.length >= 2) {
                        String geneZDBid = parts[0];
                        String acc = parts[1];

                        // zfinGenes.put(geneZDBid, 1);
                        supportingAccZFIN.computeIfAbsent(acc, k -> new ArrayList<>()).add(geneZDBid);
                        supportedGeneZFIN.computeIfAbsent(geneZDBid, k -> new ArrayList<>()).add(acc);
                    } else {
                        print(LOG, "WARN: Malformed line in toMap.unl: " + line + "\n");
                    }
                }
            }
        } catch (FileNotFoundException e) {
            reportErrAndExit("toMap.unl not found: " + e.getMessage());
        } catch (IOException e) {
            reportErrAndExit("Cannot open or read toMap.unl: " + e.getMessage());
        }

        print(LOG, "ctSupportedZFINgenes::: " + ctSupportedZFINgenes + "\n\n");
        print(LOG, "Total number of ZFIN gene records supported by GenBank RNA: " + ctSupportedZFINgenes + "\n\n");

        if (debug) {
            writeMapOfListsToFileForDebug("debug1", supportedGeneZFIN);
            writeMapOfListsToFileForDebug("debug2", supportingAccZFIN);
        }
    }

    private void initializeSetsOfZfinRecordsPart2() {
        accZFINsupportingMoreThan1 = new HashMap<>();
        geneZFINwithAccSupportingMoreThan1 = new HashMap<>();
        accZFINsupportingOnly1 = new HashMap<>();

        long ctAllSupportingAccZFIN = 0;
        long ctAccZFINSupportingMoreThan1Local = 0; // Renamed to avoid conflict with class member if any
        long ctAccZFINSupportingOnly1Local = 0;

        for (Map.Entry<String, List<String>> entry : supportingAccZFIN.entrySet()) {
            ctAllSupportingAccZFIN++;
            String acc = entry.getKey();
            List<String> zdbGeneIDs = entry.getValue();

            if (zdbGeneIDs.size() > 1) {
                ctAccZFINSupportingMoreThan1Local++;
                accZFINsupportingMoreThan1.put(acc, zdbGeneIDs);
                for (String gene : zdbGeneIDs) {
                    // Ensure supportedGeneZFIN.get(gene) is not null
                    List<String> supportingAccessionsForGene = supportedGeneZFIN.get(gene);
                    if (supportingAccessionsForGene != null) {
                        geneZFINwithAccSupportingMoreThan1.put(gene, supportingAccessionsForGene);
                    } else {
                        print(LOG, "WARN: Gene " + gene + " found in supportingAccZFIN list for ACC " + acc +
                                   " but has no entry in supportedGeneZFIN map. Skipping for geneZFINwithAccSupportingMoreThan1.\n");
                    }
                }
            } else if (zdbGeneIDs.size() == 1) {
                ctAccZFINSupportingOnly1Local++;
                accZFINsupportingOnly1.put(acc, zdbGeneIDs.get(0));
            } else {
                print(LOG, "WARN: Accession " + acc + " in supportingAccZFIN has an empty list of ZDB Gene IDs.\n");
            }
        }

        print(STATS_PRIORITY2, "\n\nThe following GenBank RNA accessions found at ZFIN are associated with multiple ZFIN genes.");
        print(STATS_PRIORITY2, "\nThe ZDB Gene Ids associated with these GenBank RNAs are excluded from mapping and hence the loading.\n\n");

        long ctGenBankRNAsupportingMultipleZFINgenes = accZFINsupportingMoreThan1.size();
        List<String> sortedKeys = accZFINsupportingMoreThan1.keySet().stream().sorted().collect(Collectors.toList());
        for (String accSupportingMoreThan1Key : sortedKeys) {
            List<String> genes = accZFINsupportingMoreThan1.get(accSupportingMoreThan1Key);
            print(STATS_PRIORITY2, accSupportingMoreThan1Key + "\t" + String.join(" ", genes) + "\n");
        }
        print(STATS_PRIORITY2, "\nTotal: " + ctGenBankRNAsupportingMultipleZFINgenes + "\n\n");

        long ctGenesZFINwithAccSupportingMoreThan1 = geneZFINwithAccSupportingMoreThan1.size();

        if (debug) {
            writeMapOfListsToFileForDebug("debug3", accZFINsupportingMoreThan1);
            writeMapOfListsToFileForDebug("debug4", geneZFINwithAccSupportingMoreThan1);
        }

        print(LOG, "\nThe following should add up \nctAccZFINSupportingOnly1 + ctAccZFINSupportingMoreThan1 = ctAllSupportingAccZFIN \nOtherwise there is bug.\n");
        print(LOG, ctAccZFINSupportingOnly1Local + " + " + ctAccZFINSupportingMoreThan1Local + " = " + ctAllSupportingAccZFIN + "\n\n");

        if (ctAccZFINSupportingOnly1Local + ctAccZFINSupportingMoreThan1Local != ctAllSupportingAccZFIN) {
            try {
                if (STATS_PRIORITY2 != null) STATS_PRIORITY2.close();
            } catch (IOException e) { /* ignore */ }
            String subjectLine = "Auto from " + instance + ": NCBI_gene_load.pl :: some numbers don't add up in initializeSetsOfZfinRecordsPart2";
            reportErrAndExit(subjectLine);
        }

        print(LOG, "ctGenesZFINwithAccSupportingMoreThan1 = " + ctGenesZFINwithAccSupportingMoreThan1 + "\n\n");
    }

    private void initializeSequenceLengthHash() {
        // Perl globals: %sequenceLength, $fdcontGenBankRNA, $fdcontGenPept, $fdcontGenBankDNA
        // In Perl: %sequenceLength = ();
        sequenceLength = new HashMap<>();

        String sqlGenBankAccessionLength = """
            select dblink_acc_num, dblink_length
            from db_link
            where dblink_length is not null
              and (dblink_linked_recid like 'ZDB-GENE%' or dblink_linked_recid like '%RNAG%')
              and dblink_fdbcont_zdb_id in (:fdcontGenBankRNA, :fdcontGenPept, :fdcontGenBankDNA)
            """; //

        NativeQuery<Tuple> query = currentSession().createNativeQuery(sqlGenBankAccessionLength, Tuple.class);
        query.setParameter("fdcontGenBankRNA", fdcontGenBankRNA);
        query.setParameter("fdcontGenPept", fdcontGenPept);
        query.setParameter("fdcontGenBankDNA", fdcontGenBankDNA);

        List<Tuple> results = query.list();

        int ctGenBankSeqLengthAtZFIN = 0;
        for (Tuple row : results) {
            String genBankAcc = row.get(0, String.class);
            Integer seqLength = row.get(1, Integer.class); // dblink_length is likely an integer

            if (genBankAcc != null && seqLength != null) {
                ctGenBankSeqLengthAtZFIN++;
                sequenceLength.put(genBankAcc, seqLength); //
            }
        }

        print(LOG, "\nctGenBankSeqLengthAtZFIN = " + ctGenBankSeqLengthAtZFIN + "\n\n");
    }

    public Map<String, Integer> parseRefSeqCatalogFileForSequenceLength() {
        File catalogFile = findRefSeqCatalogFile();
        if (catalogFile == null) {
            print(LOG, "Skipping parsing of RefSeq catalog lengths as no suitable file was found.\n");
            return null;
        }

        print(LOG, "Parsing RefSeq catalog file: " + catalogFile.getName() + "\n");
        boolean processingFullCatalog = catalogFile.getName().equals("RefSeqCatalog.gz");

        int ctRefSeqLengthFromCatalog = 0;
        try (FileInputStream fis = new FileInputStream(catalogFile);
             GZIPInputStream gzis = new GZIPInputStream(fis);
             InputStreamReader reader = new InputStreamReader(gzis);
             BufferedReader br = new BufferedReader(reader)) {

            String line;
            while ((line = br.readLine()) != null) {
                String[] fields = line.split("\t");
                if (fields.length >= 6) {
                    String taxId = fields[0];
                    if (processingFullCatalog && !"7955".equals(taxId)) {
                        continue;
                    }

                    String refSeqAccWithVersion = fields[2];
                    if (StringUtils.isBlank(refSeqAccWithVersion) || "-".equals(refSeqAccWithVersion)) {
                        print(LOG, "WARN: Skipping line with missing or placeholder RefSeq accession: [" + line + "]\n");
                        continue;
                    }
                    String refSeqAcc = refSeqAccWithVersion.replaceFirst("\\.\\d+$", "");

                    try {
                        int length = Integer.parseInt(fields[5]);
                        sequenceLength.put(refSeqAcc, length);
                        ctRefSeqLengthFromCatalog++;
                    } catch (NumberFormatException e) {
                        print(LOG, "WARN: Could not parse length as integer for RefSeq acc " + refSeqAcc + " from line: [" + line + "]. Error: " + e.getMessage() + "\n");
                    }
                } else {
                    if (StringUtils.isNotBlank(line)) {
                        print(LOG, "WARN: Skipping malformed line in RefSeq catalog (expected at least 7 fields): [" + line + "]\n");
                    }
                }
            }
        } catch (IOException e) {
            reportErrAndExit("Error reading or processing RefSeq catalog file " + catalogFile.getAbsolutePath() + ": " + e.getMessage());
        }

        print(LOG, "\nctRefSeqLengthFromCatalog = " + ctRefSeqLengthFromCatalog + "\n\n");
        return sequenceLength;
    }

    private void printSequenceLengthsCount() {
        int ctAccWithLength = sequenceLength.size();
        print(LOG, "\nTotal ctAccWithLength (GenBank from DB + RefSeq from Catalog) = " + ctAccWithLength + "\n\n");
    }

    private void parseGene2AccessionFile() {
        File gene2accessionFile = findGene2AccessionFile();
        if (gene2accessionFile == null) {
            reportErrAndExit("parseGene2AccessionFile: No gene2accession file found. Cannot proceed.");
            return;
        }

        print(LOG, "Parsing NCBI gene2accession file: " + gene2accessionFile.getName() + " at " + nowToString("yyyy-MM-dd HH:mm:ss") + " \n");
        boolean processingFullFile = gene2accessionFile.getName().equals("gene2accession.gz");

        supportedGeneNCBI = new HashMap<>();
        supportingAccNCBI = new HashMap<>();
        GenPeptNCBIgeneIds = new HashMap<>();
        GenBankDNAncbiGeneIds = new HashMap<>();
        RefSeqRNAncbiGeneIds = new HashMap<>();
        RefPeptNCBIgeneIds = new HashMap<>();
        RefSeqDNAncbiGeneIds = new HashMap<>();
        noLength = new HashMap<>();

        ctNoLength = 0;
        ctNoLengthRefSeq = 0;
        ctZebrafishGene2accession = 0;

        int ctLines = 0;

        try (FileInputStream fis = new FileInputStream(gene2accessionFile);
             GZIPInputStream gzis = new GZIPInputStream(fis);
             InputStreamReader reader = new InputStreamReader(gzis);
             BufferedReader br = new BufferedReader(reader)) {

            String line;
            boolean headerSkipped = !processingFullFile;

            while ((line = br.readLine()) != null) {
                ctLines++;
                if (!headerSkipped && line.startsWith("#")) {
                    continue;
                }
                headerSkipped = true;

                String[] fields = line.split("\t", -1); // Keep trailing empty fields
                if (fields.length < 8) {
                    if (StringUtils.isNotBlank(line)) print(LOG, "WARN: Skipping malformed line (expected at least 8 fields): [" + line + "]\n");
                    continue;
                }

                String taxId = fields[0];
                if (processingFullFile && !"7955".equals(taxId)) {
                    continue;
                }
                ctZebrafishGene2accession++;

                String ncbiGeneId = fields[1];
                String status = fields[2];
                if (!StringUtils.isEmpty(status) && "SUPPRESSED".equals(status)) {
                    print(LOG, "WARN: Skipping line SUPPRESSED status: [" + line + "]\n");
                    continue;
                }

                String rnaAccVersion = fields[3];
                String proteinAccVersion = fields[5];
                String dnaAccVersion = fields[7];

                BiConsumer<String, String> checkAndStoreNoLength = (acc, geneId) -> {
                    if (!sequenceLength.containsKey(acc)) {
                        noLength.put(acc, geneId);
                        this.ctNoLength++;
                    }
                };

                BiConsumer<String, String> checkAndStoreNoLengthRefSeq = (acc, geneId) -> {
                    if (!sequenceLength.containsKey(acc)) {
                        noLength.put(acc, geneId);
                        this.ctNoLength++;
                        this.ctNoLengthRefSeq++;
                    }
                };


                if ("-".equals(status)) {
                    if (stringStartsWithLetter(rnaAccVersion)) {
                        String rnaAcc = rnaAccVersion.replaceFirst("\\.\\d+$", "");
                        supportedGeneNCBI.computeIfAbsent(ncbiGeneId, k -> new LinkedHashSet<>()).add(rnaAcc);
                        supportingAccNCBI.computeIfAbsent(rnaAcc, k -> new LinkedHashSet<>()).add(ncbiGeneId);
                        checkAndStoreNoLength.accept(rnaAcc, ncbiGeneId);
                    }
                    if (stringStartsWithLetter(proteinAccVersion)) {
                        String proteinAcc = proteinAccVersion.replaceFirst("\\.\\d+$", "");
                        GenPeptNCBIgeneIds.put(proteinAcc, ncbiGeneId);
                        checkAndStoreNoLength.accept(proteinAcc, ncbiGeneId);
                    }
                    if (stringStartsWithLetter(dnaAccVersion)) {
                        String dnaAcc = dnaAccVersion.replaceFirst("\\.\\d+$", "");
                        GenBankDNAncbiGeneIds.computeIfAbsent(dnaAcc, k -> new ArrayList<>()).add(ncbiGeneId);
                        checkAndStoreNoLength.accept(dnaAcc, ncbiGeneId);
                    }
                } else {
                    if (stringStartsWithLetter(rnaAccVersion)) {
                        String rnaAcc = rnaAccVersion.replaceFirst("\\.\\d+$", "");
                        if (rnaAcc.matches("^(NM_|XM_|NR_|XR_).*")) {
                            RefSeqRNAncbiGeneIds.put(rnaAcc, ncbiGeneId);
                        }
                        checkAndStoreNoLengthRefSeq.accept(rnaAcc, ncbiGeneId);
                    }
                    if (stringStartsWithLetter(proteinAccVersion)) {
                        String proteinAcc = proteinAccVersion.replaceFirst("\\.\\d+$", "");
                        RefPeptNCBIgeneIds.put(proteinAcc, ncbiGeneId);
                        checkAndStoreNoLengthRefSeq.accept(proteinAcc, ncbiGeneId);
                    }
                    if (stringStartsWithLetter(dnaAccVersion)) {
                        String dnaAcc = dnaAccVersion.replaceFirst("\\.\\d+$", "");
                        RefSeqDNAncbiGeneIds.put(dnaAcc, ncbiGeneId);
                        checkAndStoreNoLengthRefSeq.accept(dnaAcc, ncbiGeneId);
                    }
                }
            }
        } catch (IOException e) {
            reportErrAndExit("Error reading or processing gene2accession file " + gene2accessionFile.getAbsolutePath() + ": " + e.getMessage());
        }

        print(LOG, "\n\nNumber of lines on gene2accession file:  " + ctLines + "\n");
        print(LOG, "ctZebrafishGene2accession:  " + ctZebrafishGene2accession + "\n");
        print(LOG, "ctNoLength = " + ctNoLength + "\nctNoLengthRefSeq = " + ctNoLengthRefSeq + "\n\n");

        if (debug) {
            writeDebug16Output();
        }
    }

    private void countNCBIGenesWithSupportingGenBankRNA() {
        int ctGeneIdsNCBIonGene2accession = supportedGeneNCBI.size();

        if (debug) {
            // In Perl, debug5 was written here.
            // The writeMapOfListsToFileForDebug handles Map<String, List<String>>
            writeMapOfListsToFileForDebug("debug5", supportedGeneNCBI);
        }

        String logMessage = "\nThe number of NCBI genes with supporting GenBank RNA: " + ctGeneIdsNCBIonGene2accession + "\n\n";
        print(LOG, logMessage);
        print(STATS_PRIORITY2, logMessage);
    }

    private void logSupportingAccNCBI() {
        if (debug) {
            // In Perl, debug6 was written here.
            // The writeMapOfListsToFileForDebug handles Map<String, List<String>>
            writeMapOfListsToFileForDebug("debug6", supportingAccNCBI);
        }
        // No other logging is specified for this method in the Perl script,
        // besides the debug file output.
    }

    private void initializeHashOfNCBIAccessionsSupportingMultipleGenes() {
        // Initialize class member maps
        accNCBIsupportingMoreThan1 = new HashMap<>();
        geneNCBIwithAccSupportingMoreThan1 = new HashMap<>();
        accNCBIsupportingOnly1 = new HashMap<>();

        // Local counters for this specific method execution
        long ctAllSupportingAccNCBI = 0;
        long ctAccNCBISupportingMoreThan1 = 0;
        long ctAccNCBISupportingOnly1 = 0;

        for (Map.Entry<String, Set<String>> entry : supportingAccNCBI.entrySet()) {
            ctAllSupportingAccNCBI++;
            String acc = entry.getKey();
            Set<String> ncbiGeneIds = entry.getValue();

            if (ncbiGeneIds.size() > 1) {
                ctAccNCBISupportingMoreThan1++;
                accNCBIsupportingMoreThan1.put(acc, ncbiGeneIds.stream().collect(Collectors.toList()));

                for (String geneId : ncbiGeneIds) {
                    // Populate geneNCBIwithAccSupportingMoreThan1 with the list of all accessions supporting this geneId
                    if (supportedGeneNCBI.containsKey(geneId)) {
                        geneNCBIwithAccSupportingMoreThan1.put(geneId, supportedGeneNCBI.get(geneId).stream().collect(Collectors.toList()));
                    } else {
                        // This case should ideally not happen if data is consistent
                        print(LOG, "WARN: NCBI Gene ID " + geneId + " found in supportingAccNCBI's list for ACC " + acc +
                                   " but not found as a key in supportedGeneNCBI map.\n");
                        geneNCBIwithAccSupportingMoreThan1.put(geneId, new ArrayList<>()); // Put empty list to avoid nulls later
                    }
                }
            } else if (ncbiGeneIds.size() == 1) { // Should always be 1 if not > 1, but good to be explicit
                ctAccNCBISupportingOnly1++;
                accNCBIsupportingOnly1.put(acc, ncbiGeneIds.stream().findFirst().get());
            } else {
                // This case (empty list for an accession) should ideally not happen
                print(LOG, "WARN: Accession " + acc + " in supportingAccNCBI has an empty list of NCBI Gene IDs.\n");
            }
        }

        // Logging and Reporting
        String header1 = "\nThe following GenBank accession found on NCBI's gene2accession file support more than 1 NCBI genes\n";
        print(STATS_PRIORITY2, header1);
        print(LOG, header1);
        accNCBIsupportingMoreThan1.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    String line = entry.getKey() + "\t" + String.join(" ", entry.getValue()) + "\n";
                    print(STATS_PRIORITY2, line);
                    print(LOG, line);
                });

        String header2 = "\nThe following NCBI's Gene Ids have at least 1 supporting GenBank accession that supports more than 1 NCBI genes\n";
        print(STATS_PRIORITY2, header2);
        print(LOG, header2);
        geneNCBIwithAccSupportingMoreThan1.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    String line = entry.getKey() + "\t" + String.join(" ", entry.getValue()) + "\n";
                    // In Perl, STATS_PRIORITY2 also got this log.
                    print(STATS_PRIORITY2, line);
                    print(LOG, line);
                });

        print(LOG, "\nThe following should add up \nctAccNCBISupportingOnly1 + ctAccNCBISupportingMoreThan1 = ctAllSupportingAccNCBI \nOtherwise there is bug.\n");
        print(LOG, ctAccNCBISupportingOnly1 + " + " + ctAccNCBISupportingMoreThan1 + " = " + ctAllSupportingAccNCBI + "\n\n");

        if (ctAccNCBISupportingOnly1 + ctAccNCBISupportingMoreThan1 != ctAllSupportingAccNCBI) {
            try {
                if (STATS_PRIORITY2 != null) STATS_PRIORITY2.close(); // Close before exit
            } catch (IOException e) {
                print(LOG, "Error closing STATS_PRIORITY2: " + e.getMessage() + "\n");
            }
            String subjectLine = "Auto from " + (instance != null ? instance : "UnknownInstance") + ": NCBI_gene_load.pl :: some numbers don't add up in initializeHashOfNCBIAccessionsSupportingMultipleGenes";
            reportErrAndExit(subjectLine);
        }
    }

    /**
     * Step 5-4: get 1:1, 1:N and 1:0 (from ZFIN to NCBI) lists
     * pass 1 of the mapping: one-way mapping of ZFIN genes onto NCBI genes
     */
    private void initializeMapOfZfinToNCBIgeneIds() {
        // Initialize class fields that will be populated by this method
        oneToNZFINtoNCBI = new HashMap<>();
        oneToOneZFINtoNCBI = new HashMap<>();
        genesZFINwithNoRNAFoundAtNCBI = new HashMap<>();

        long ct1to1ZFINtoNCBI = 0;
        long ct1toNZFINtoNCBI = 0;
        long ctProcessedZFINgenes = 0;
        long ctZFINgenesSupported = 0;
        long ctZFINgenesWithAllAccsNotFoundAtNCBI = 0;

        if (supportedGeneZFIN == null) {
            print(LOG, "Warning: supportedGeneZFIN map is null. Skipping initializeMapOfZfinToNCBIgeneIds.\n");
            return;
        }

        for (Map.Entry<String, List<String>> entry : supportedGeneZFIN.entrySet()) {
            ctZFINgenesSupported++;
            String zfinGene = entry.getKey();
            List<String> arrayOfAccs = entry.getValue(); // refence to the array of supporting GenBank RNA accessions

            // those genes with even just 1 supporting RNA sequence that supports another gene won't be processed
            if (geneZFINwithAccSupportingMoreThan1 != null &&
                !geneZFINwithAccSupportingMoreThan1.containsKey(zfinGene)) {

                ctProcessedZFINgenes++;

                int ctAccsForGene = 0;
                boolean mapped1to1 = true;
                String firstMappedNCBIgeneIdSaved = "None";
                // This map stores {NCBI Gene ID -> Accession} for the current ZFIN gene
                Map<String, String> mappedNCBIgeneIdsForCurrentZfinGene = new HashMap<>();

                if (arrayOfAccs != null) {
                    for (String acc : arrayOfAccs) {
                        // only map ZFIN genes to NCBI genes that are with supporting RNA accessions
                        // supporting only 1 NCBI gene may have supporting acc at ZFIN that is not
                        // found at NCBI or not supporting any gene at NCBI; do nothing in such cases
                        if (accNCBIsupportingOnly1 != null && accNCBIsupportingOnly1.containsKey(acc) &&
                            (accNCBIsupportingMoreThan1 == null || !accNCBIsupportingMoreThan1.containsKey(acc))) {

                            ctAccsForGene++;
                            String ncbiGeneId = accNCBIsupportingOnly1.get(acc); // this is the NCBI gene Id

                            if (ctAccsForGene == 1) { // first acc in the supporting acc list for ZFIN gene which is also found at NCBI
                                firstMappedNCBIgeneIdSaved = ncbiGeneId;
                                mappedNCBIgeneIdsForCurrentZfinGene.put(ncbiGeneId, acc);
                            } else {
                                // if the gene is not found in the save hash, it means mapped to another NCBI gene
                                if (!mappedNCBIgeneIdsForCurrentZfinGene.containsKey(ncbiGeneId)) {
                                    // do nothing if it is found in the save hash (i.e., already mapped to this ncbiGeneId via another acc)
                                    mapped1to1 = false;
                                    mappedNCBIgeneIdsForCurrentZfinGene.put(ncbiGeneId, acc); // add it to the save hash
                                }
                            }
                        }
                    } // end of foreach acc
                }


                if (mapped1to1 && !"None".equals(firstMappedNCBIgeneIdSaved)) {
                    oneToOneZFINtoNCBI.put(zfinGene, firstMappedNCBIgeneIdSaved);
                    ct1to1ZFINtoNCBI++;
                } else if (!mapped1to1) { // This implies mappedNCBIgeneIdsForCurrentZfinGene.size() > 1
                    ct1toNZFINtoNCBI++;
                    oneToNZFINtoNCBI.put(zfinGene, new HashMap<>(mappedNCBIgeneIdsForCurrentZfinGene));
                } else { // mapped1to1 is true, but firstMappedNCBIgeneIdSaved is "None" (no valid NCBI mapping found)
                    ctZFINgenesWithAllAccsNotFoundAtNCBI++;
                    genesZFINwithNoRNAFoundAtNCBI.put(zfinGene, arrayOfAccs); // original list of accs
                }
            }
        } // end of foreach zfinGene

        if (debug) {
            writeMapOfStringsToFileForDebug("debug9", oneToOneZFINtoNCBI);
            writeMapOfMapsToFileForDebug("debug10", oneToNZFINtoNCBI);
            // Note: The Perl version did not have a specific debug write for genesZFINwithNoRNAFoundAtNCBI here.
            // If needed, you could add:
            // writeMapOfListsToFileForDebug("debug_genesZFINwithNoRNAFoundAtNCBI", genesZFINwithNoRNAFoundAtNCBI);
        }

        print(LOG, "\nctZFINgenesSupported = " + ctZFINgenesSupported + "\nctProcessedZFINgenes = " + ctProcessedZFINgenes + "\n\n");
        print(LOG, "ct1to1ZFINtoNCBI = " + ct1to1ZFINtoNCBI + "\nct1toNZFINtoNCBI = " + ct1toNZFINtoNCBI + "\n\n");

        String consistencyCheckMsg = "\nThe following should add up: \n" +
                                     "ct1to1ZFINtoNCBI + ct1toNZFINtoNCBI + ctZFINgenesWithAllAccsNotFoundAtNCBI = ctProcessedZFINgenes \n" +
                                     "Otherwise there is a bug.\n";
        print(LOG, consistencyCheckMsg);
        print(LOG, ct1to1ZFINtoNCBI + " + " + ct1toNZFINtoNCBI + " + " + ctZFINgenesWithAllAccsNotFoundAtNCBI + " = " + ctProcessedZFINgenes + "\n\n");

        // if the numbers don't add up, stop the whole process
        if (ct1to1ZFINtoNCBI + ct1toNZFINtoNCBI + ctZFINgenesWithAllAccsNotFoundAtNCBI != ctProcessedZFINgenes) {
            try {
                if (STATS_PRIORITY2 != null) STATS_PRIORITY2.close();
            } catch (Exception e) { // Catching generic Exception for safety, though IOException is typical for close()
                print(LOG, "Error closing STATS_PRIORITY2: " + e.getMessage() + "\n");
            }
            String subjectLine = "Auto from " + (instance != null ? instance : "UnknownInstance") +
                                 ": NCBIDirectPort.java :: some numbers don't add up in initializeMapOfZfinToNCBIgeneIds";
            reportErrAndExit(subjectLine);
        }
    }


    private void logOneToZeroAssociations() {
        long ctOneToZero = genesZFINwithNoRNAFoundAtNCBI.size();

        if (debug) {
            File reportFile = new File(workingDir, "reportOneToZero");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(reportFile))) {
                writer.write(getArtifactComparisonURLs()); // Write Jenkins URLs if available
                genesZFINwithNoRNAFoundAtNCBI.entrySet().stream()
                        .sorted(Map.Entry.comparingByKey())
                        .forEach(entry -> {
                            try {
                                String line = entry.getKey() + "\t" + String.join(" ", entry.getValue()) + "\n";
                                writer.write(line);
                            } catch (IOException e) {
                                // Log or handle exception during individual line write if necessary,
                                // though the outer try-catch should handle most file issues.
                                // For now, let it propagate to the outer catch.
                                throw new RuntimeException("Error writing to " + reportFile.getName(), e);
                            }
                        });
            } catch (IOException | RuntimeException e) { // Catch RuntimeException from lambda
                print(LOG, "ERROR: Could not write to " + reportFile.getName() + " file: " + e.getMessage() + "\n");
                // Depending on policy, might not want to reportErrAndExit for a debug file failure.
            }
        }

        print(LOG, "\nctOneToZero = " + ctOneToZero + "\n\n");
        print(STATS_PRIORITY2, "\nMapping result statistics: number of 1:0 (ZFIN to NCBI) - " + ctOneToZero + "\n\n");
    }

    private void oneWayMappingNCBItoZfinGenes() {
        // Step 5-5: get 1:1, 1:N and 1:0 (from NCBI to ZFIN) lists
        oneToNNCBItoZFIN = new HashMap<>();
        oneToOneNCBItoZFIN = new HashMap<>();
        genesNCBIwithAllAccsNotFoundAtZFIN = new HashMap<>();

        long ct1to1NCBItoZFIN = 0;
        long ct1toNNCBItoZFIN = 0;
        long ctProcessedNCBIgenes = 0;
        long ctNCBIgenesSupported = supportedGeneNCBI.size();
        long ctNCBIgenesWithAllAccsNotFoundAtZFIN = 0;

        for (Map.Entry<String, Set<String>> ncbiEntry : supportedGeneNCBI.entrySet()) {
            String ncbiGene = ncbiEntry.getKey();
            Set<String> rnaAccessionsForNcbiGene = ncbiEntry.getValue();

            if (geneNCBIwithAccSupportingMoreThan1.containsKey(ncbiGene)) {
                continue; // Skip NCBI genes that have problematic accessions (supporting multiple NCBI genes)
            }
            ctProcessedNCBIgenes++;

            boolean mapped1to1 = true;
            String firstMappedZFINgeneIdSaved = "None";
            Map<String, String> currentGeneMappedZFINgeneIds = new HashMap<>(); // ZFIN Gene ID -> Accession

            for (String acc : rnaAccessionsForNcbiGene) {
                if (accZFINsupportingOnly1.containsKey(acc) && !accZFINsupportingMoreThan1.containsKey(acc)) {
                    String zdbGeneId = accZFINsupportingOnly1.get(acc);

                    if (currentGeneMappedZFINgeneIds.isEmpty()) {
                        firstMappedZFINgeneIdSaved = zdbGeneId;
                        currentGeneMappedZFINgeneIds.put(zdbGeneId, acc);
                    } else {
                        if (!currentGeneMappedZFINgeneIds.containsKey(zdbGeneId)) {
                            mapped1to1 = false;
                            currentGeneMappedZFINgeneIds.put(zdbGeneId, acc);
                        }
                        // If zdbGeneId is already a key, it means another ACC for the same NCBI gene maps to the same ZFIN gene.
                        // This is fine and doesn't change the 1:1 or 1:N status for the NCBI gene itself.
                    }
                }
            }

            if (mapped1to1 && !"None".equals(firstMappedZFINgeneIdSaved)) {
                oneToOneNCBItoZFIN.put(ncbiGene, firstMappedZFINgeneIdSaved);
                ct1to1NCBItoZFIN++;
            } else if (!mapped1to1) { // This implies currentGeneMappedZFINgeneIds.size() > 1
                oneToNNCBItoZFIN.put(ncbiGene, new HashMap<>(currentGeneMappedZFINgeneIds));
                ct1toNNCBItoZFIN++;
            } else { // mapped1to1 is true, but firstMappedZFINgeneIdSaved is "None" (no valid ZFIN mapping)
                genesNCBIwithAllAccsNotFoundAtZFIN.put(ncbiGene, rnaAccessionsForNcbiGene.stream().collect(Collectors.toList()));
                ctNCBIgenesWithAllAccsNotFoundAtZFIN++;
            }
        }

        if (debug) {
            writeMapOfStringsToFileForDebug("debug12", oneToOneNCBItoZFIN);
            writeMapOfMapsToFileForDebug("debug13", oneToNNCBItoZFIN);
            writeMapOfListsToFileForDebug("debug14", genesNCBIwithAllAccsNotFoundAtZFIN);
        }

        print(LOG, "\nctNCBIgenesSupported = " + ctNCBIgenesSupported + "\nctProcessedNCBIgenes = " + ctProcessedNCBIgenes + "\n\n");
        print(LOG, "ct1to1NCBItoZFIN = " + ct1to1NCBItoZFIN + "\nct1toNNCBItoZFIN = " + ct1toNNCBItoZFIN + "\n\n");
        print(LOG, "\nctNCBIgenesWithAllAccsNotFoundAtZFIN (0:1 ZFIN to NCBI) = " + ctNCBIgenesWithAllAccsNotFoundAtZFIN + "\n\n");


        String consistencyCheckMsg = "\nThe following should add up: \n" +
                                     "ct1to1NCBItoZFIN + ct1toNNCBItoZFIN + ctNCBIgenesWithAllAccsNotFoundAtZFIN = ctProcessedNCBIgenes \n" +
                                     "Otherwise there is a bug.\n";
        print(LOG, consistencyCheckMsg);
        print(LOG, ct1to1NCBItoZFIN + " + " + ct1toNNCBItoZFIN + " + " + ctNCBIgenesWithAllAccsNotFoundAtZFIN + " = " + ctProcessedNCBIgenes + "\n\n");

        if (ct1to1NCBItoZFIN + ct1toNNCBItoZFIN + ctNCBIgenesWithAllAccsNotFoundAtZFIN != ctProcessedNCBIgenes) {
            try {
                if (STATS_PRIORITY2 != null) STATS_PRIORITY2.close();
            } catch (IOException e) {
                print(LOG, "Error closing STATS_PRIORITY2: " + e.getMessage() + "\n");
            }
            String subjectLine = "Auto from " + (instance != null ? instance : "UnknownInstance") +
                                 ": NCBI_gene_load.pl :: numbers don't add up in oneWayMappingNCBItoZfinGenes";
            reportErrAndExit(subjectLine);
        }
        // The Perl script calls this "0:1 (ZFIN to NCBI)" which refers to NCBI genes that don't map to ZFIN.
        print(STATS_PRIORITY2, "\nMapping result statistics: number of 0:1 (ZFIN to NCBI) - " + ctNCBIgenesWithAllAccsNotFoundAtZFIN + "\n\n");
    }

    private void logGenBankDNAncbiGeneIds() {
        if (!debug) {
            return;
        }

        File debugFile = new File(workingDir, "debug5a");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(debugFile))) {
            GenBankDNAncbiGeneIds.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> {
                        String genBankAccession = entry.getKey();
                        List<String> ncbiGeneIds = entry.getValue();
                        StringBuilder lineBuilder = new StringBuilder();
                        lineBuilder.append(genBankAccession).append("\t");

                        for (String ncbiGeneId : ncbiGeneIds) {
                            lineBuilder.append(ncbiGeneId);
                            if (oneToOneNCBItoZFIN.containsKey(ncbiGeneId)) {
                                lineBuilder.append("/").append(oneToOneNCBItoZFIN.get(ncbiGeneId));
                            }
                            lineBuilder.append(" ");
                        }
                        // Remove trailing space if any
                        String line = lineBuilder.toString().trim();
                        try {
                            writer.write(line);
                            writer.newLine();
                        } catch (IOException e) {
                            throw new RuntimeException("Error writing to " + debugFile.getName(), e);
                        }
                    });
        } catch (IOException | RuntimeException e) {
            print(LOG, "ERROR: Could not write to " + debugFile.getName() + " file: " + e.getMessage() + "\n");
            // Depending on policy, might not want to reportErrAndExit for a debug file failure.
        }
    }

    private void prepare2WayMappingResults() {
        mapped = new HashMap<>();
        mappedReversed = new HashMap<>();
        this.ctOneToOneNCBI = 0;

        long ctAllpotentialOneToOneZFIN = 0;
        long ctOneToOneZFIN = 0;

        for (Map.Entry<String, String> entry : oneToOneZFINtoNCBI.entrySet()) {
            ctAllpotentialOneToOneZFIN++;
            String zdbid = entry.getKey();
            String ncbiIdFromZfinMap = entry.getValue();

            if (ncbiIdFromZfinMap != null && zdbid.equals(oneToOneNCBItoZFIN.get(ncbiIdFromZfinMap))) {
                ctOneToOneZFIN++;
                mapped.put(zdbid, ncbiIdFromZfinMap);
                mappedReversed.put(ncbiIdFromZfinMap, zdbid); // Populate mappedReversed here as well
            }
        }
        // ctOneToOneNCBI should be the size of these confirmed 1:1 maps.
        this.ctOneToOneNCBI = mapped.size();

        // The second loop in Perl for ctAllpotentialOneToOneNCBI and ctOneToOneNCBI
        // essentially re-confirms the same set for mappedReversed.
        // Our current ctOneToOneNCBI is now based on the size of the 'mapped' (or 'mappedReversed') map.

        print(LOG, "\nctAllpotentialOneToOneZFIN = " + ctAllpotentialOneToOneZFIN + "\nctOneToOneZFIN (reciprocal) = " + ctOneToOneZFIN + "\n\n");
        // To match Perl's logging, ctAllpotentialOneToOneNCBI would be oneToOneNCBItoZFIN.size()
        print(LOG, "ctAllpotentialOneToOneNCBI = " + oneToOneNCBItoZFIN.size() + "\nctOneToOneNCBI (reciprocal, final count) = " + this.ctOneToOneNCBI + "\n\n");
        print(STATS_PRIORITY2, "\nMapping result statistics: number of 1:1 based on GenBank RNA - " + this.ctOneToOneNCBI + "\n\n");

        if (debug) {
            File debugFile = new File(workingDir, "debug17");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(debugFile))) {
                writer.write("ZFIN to NCBI\n===================\n");
                mapped.entrySet().stream()
                        .sorted(Map.Entry.comparingByKey())
                        .forEach(entry -> {
                            try {
                                writer.write(entry.getKey() + "\t" + entry.getValue() + "\n");
                            } catch (IOException e) {
                                throw new RuntimeException("Error writing to " + debugFile.getName(), e);
                            }
                        });

                writer.write("\n\nNCBI to ZFIN\n===================\n");
                mappedReversed.entrySet().stream()
                        .sorted(Map.Entry.comparingByKey())
                        .forEach(entry -> {
                            try {
                                writer.write(entry.getKey() + "\t" + entry.getValue() + "\n");
                            } catch (IOException e) {
                                throw new RuntimeException("Error writing to " + debugFile.getName(), e);
                            }
                        });
            } catch (IOException | RuntimeException e) {
                print(LOG, "ERROR: Could not write to " + debugFile.getName() + " file: " + e.getMessage() + "\n");
            }
        }
    }

    private void addReverseMappedGenesFromNCBItoZFINFromSupplementaryLoad() {
        if (!"true".equalsIgnoreCase(env("LOAD_NCBI_ONE_WAY_GENES"))) {
            print(LOG, "Skipping the load of genes that are mapped from NCBI to ZFIN without being mapped back from ZFIN to NCBI (LOAD_NCBI_ONE_WAY_GENES not true).\n");
            return;
        }
        print(LOG, "Running the load of genes that are mapped from NCBI to ZFIN without being mapped back from ZFIN to NCBI (ZFIN-8517).\n");
        print(LOG, "More information in debug15 file.\n");

        StringBuilder debugBuffer = new StringBuilder();
        File inputFile;
        String sourceRoot = env("SOURCEROOT");
        if (sourceRoot == null) {
            reportErrAndExit("SOURCEROOT environment variable is not set. Cannot determine path for ncbi_matches_through_ensembl.csv or gradle execution.");
            return;
        }

        String overrideReportFile = env("LOAD_NCBI_ONE_WAY_REPORT");
        if (StringUtils.isNotEmpty(overrideReportFile)) {
            inputFile = new File(overrideReportFile);
            print(LOG, "Using provided report file through LOAD_NCBI_ONE_WAY_REPORT: " + inputFile.getAbsolutePath() + "\n");
        } else {

            inputFile = new File(sourceRoot, "ncbi_matches_through_ensembl.csv");
            NcbiMatchThroughEnsemblTask task = new NcbiMatchThroughEnsemblTask();
            try {
                File downloadFile = new File(workingDir, "zf_gene_info.gz");
                String[] args = new String[]{};
                if (envTrue("SKIP_DOWNLOADS") && downloadFile.exists()) {
                    System.out.println("Skipping download of zf_gene_info.gz as SKIP_DOWNLOADS is set to true.");
                    print(LOG, "Skipping download of zf_gene_info.gz as SKIP_DOWNLOADS is set to true.\n");
                    args = new String[]{"file://" + downloadFile.getAbsolutePath()};
                } else {
                    System.out.println("Downloading zf_gene_info.gz as SKIP_DOWNLOADS is not set to true.");
                    print(LOG, "Downloading zf_gene_info.gz as SKIP_DOWNLOADS is not set to true.\n");
                }
                task.runTask(args);
                String md5 = md5File(inputFile, LOG);
                FileUtils.copyFile(inputFile, new File(workingDir, inputFile.getName()));
                System.out.println("md5 checksum of " + inputFile.getName() + ": " + md5);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

        }

        print(LOG, "Reading supplementary mapping input file: " + inputFile.getAbsolutePath() + "\n");
        File localInputFile = new File(workingDir, inputFile.getName());
        try {
            Files.copy(inputFile.toPath(), localInputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            reportErrAndExit("Failed to copy input file " + inputFile.getAbsolutePath() + " to working directory: " + e.getMessage());
            return;
        }

        ncbiSupplementMap = new HashMap<>();
        ncbiSupplementMapReversed = new HashMap<>();
        long ncbiSupplementMapCount = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(localInputFile))) {
            String line = reader.readLine(); // Skip header
            if (line == null) {
                print(LOG, "WARN: Supplementary mapping file is empty: " + localInputFile.getAbsolutePath() + "\n");
            }

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", -1); // Use -1 limit to include trailing empty strings
                if (parts.length < 7) {
                    debugBuffer.append("Skip Malformed line: ").append(line).append("\n");
                    continue;
                }

                String ncbiId = parts[0].trim();
                String zdbId = parts[1].trim();
                // String ensemblId = parts[2].trim(); // not used in this logic directly
                // String symbol = parts[3].trim(); // not used in this logic directly
                // String dblinks = parts[4].trim(); // not used in this logic directly
                // String publications = parts[5].trim(); // not used in this logic directly
                String rnaAccessions = parts[6].trim();

                if (mappedReversed.containsKey(ncbiId) || mapped.containsKey(zdbId)) {
                    debugBuffer.append("Skip Duplicate: ").append(line).append("\n");
                    continue;
                }

                if (StringUtils.isNotEmpty(rnaAccessions) && !zdbId.contains("ZDB-MIRNAG-")) {
                    debugBuffer.append("Skip NON-blank NON-MIRNAG: ").append(line).append("\n");
                    continue;
                }

                // Check if the gene already has an NCBI Gene link in the database
                String sqlCheckExistingNCBILink = """
                 SELECT COUNT(*) FROM db_link
                 WHERE dblink_linked_recid = :zdbId
                 AND dblink_fdbcont_zdb_id = :fdcontNCBIgeneId
                 """;
                NativeQuery<Long> checkQuery = currentSession().createNativeQuery(sqlCheckExistingNCBILink, Long.class);
                checkQuery.setParameter("zdbId", zdbId);
                checkQuery.setParameter("fdcontNCBIgeneId", fdcontNCBIgeneId);
                Long existingLinkCount = checkQuery.uniqueResult();

                if (existingLinkCount != null && existingLinkCount > 0) {
                    debugBuffer.append("Skip Gene with existing NCBI link: ").append(line).append("\n");
                    continue;
                }

                debugBuffer.append("Supplemental mapping: ").append(line).append("\n");
                ncbiSupplementMap.put(zdbId, ncbiId);
                ncbiSupplementMapReversed.put(ncbiId, zdbId);
                ncbiSupplementMapCount++;
            }
        } catch (IOException e) {
            reportErrAndExit("Error reading supplementary mapping file " + localInputFile.getAbsolutePath() + ": " + e.getMessage());
            return;
        }

        print(LOG, "ncbiSupplementMapCount = " + ncbiSupplementMapCount + "\n");
        debugBuffer.append("ncbiSupplementMapCount = ").append(ncbiSupplementMapCount).append("\n");

        if (debug) {
            File debugFile = new File(workingDir, "debug15");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(debugFile))) {
                writer.write(debugBuffer.toString());
            } catch (IOException e) {
                print(LOG, "ERROR: Could not write to debug15 file: " + e.getMessage() + "\n");
            }
        }
    }

    private void writeNCBIgeneIdsMappedBasedOnGenBankRNA() {
        // Writes NCBI Gene IDs mapped based on GenBank RNA accessions to TOLOAD.
        // ctToLoad is a running total.
        try {
            if (TOLOAD == null) {
                print(LOG, "ERROR: TOLOAD writer is null in writeNCBIgeneIdsMappedBasedOnGenBankRNA. Cannot write.\n");
                reportErrAndExit("TOLOAD writer not initialized before writing mapped GenBank RNA.");
                return;
            }

            //remove from map any duplicate entries
            //these should have already been filtered earlier but this is a safety net
            //if we find any here we want to log them, and find out how they got through
            Map<String, Set<String>> duplicateNcbiIssues = removeAndReturnDuplicateMapEntries(mapped);
            String duplicatesInMappedReport =
                    duplicateNcbiIssues.entrySet().stream()
                            .sorted(Map.Entry.comparingByKey())
                            .map(entry -> entry.getKey() + "," + String.join("|", entry.getValue()) + "\n")
                            .collect(Collectors.joining());
            if (!duplicatesInMappedReport.isEmpty()) {
                Files.writeString(new File(workingDir, "duplicates_in_mapped.csv").toPath(), duplicatesInMappedReport);
            }

            mapped.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> {
                        String zdbId = entry.getKey();
                        String mappedNCBIgeneId = entry.getValue();
                        // Format: zdbId|mappedNCBIgeneId|||fdcontNCBIgeneId|pubMappedbasedOnRNA
                        String line = String.format("%s|%s|||%s|%s\n", zdbId, mappedNCBIgeneId, fdcontNCBIgeneId, pubMappedbasedOnRNA);
                        try {
                            TOLOAD.write(line);
                            this.ctToLoad++;
                        } catch (IOException e) {
                            // Throw a RuntimeException to be caught by the calling context if direct error handling is complex here
                            throw new RuntimeException("Error writing to TOLOAD in writeNCBIgeneIdsMappedBasedOnGenBankRNA for ZDB ID " + zdbId, e);
                        }
                    });
        } catch (RuntimeException e) { // Catch runtime exceptions from the lambda
            if (e.getCause() instanceof IOException) {
                reportErrAndExit("IOException during writeNCBIgeneIdsMappedBasedOnGenBankRNA: " + e.getCause().getMessage());
            } else {
                reportErrAndExit("Unexpected error in writeNCBIgeneIdsMappedBasedOnGenBankRNA: " + e.getMessage());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeNCBIgeneIdsMappedBasedOnSupplementaryLoad() {
        if (!envTrue("LOAD_NCBI_ONE_WAY_GENES")) {
            return;
        }
        // Writes NCBI Gene IDs mapped based on supplementary load logic to TOLOAD.
        try {
            if (TOLOAD == null) {
                print(LOG, "ERROR: TOLOAD writer is null in writeNCBIgeneIdsMappedBasedOnSupplementaryLoad. Cannot write.\n");
                reportErrAndExit("TOLOAD writer not initialized before writing supplementary load.");
                return;
            }

            ncbiSupplementMap.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> {
                        String zdbId = entry.getKey();
                        String mappedNCBIgeneId = entry.getValue();
                        // Format: zdbId|mappedNCBIgeneId|||fdcontNCBIgeneId|pubMappedbasedOnNCBISupplement
                        String line = String.format("%s|%s|||%s|%s\n", zdbId, mappedNCBIgeneId, fdcontNCBIgeneId, pubMappedbasedOnNCBISupplement);
                        try {
                            TOLOAD.write(line);
                            this.ctToLoad++;
                        } catch (IOException e) {
                            throw new RuntimeException("Error writing to TOLOAD in writeNCBIgeneIdsMappedBasedOnSupplementaryLoad for ZDB ID " + zdbId, e);
                        }
                    });
        } catch (RuntimeException e) { // Catch runtime exceptions from the lambda
            if (e.getCause() instanceof IOException) {
                reportErrAndExit("IOException during writeNCBIgeneIdsMappedBasedOnSupplementaryLoad: " + e.getCause().getMessage());
            } else {
                reportErrAndExit("Unexpected error in writeNCBIgeneIdsMappedBasedOnSupplementaryLoad: " + e.getMessage());
            }
        }
    }

    private List<LoadReportAction> getOneToNNCBItoZFINgeneIds() {
        nToOne = new HashMap<>();
        oneToN = new HashMap<>();
        List<LoadReportAction> loadReportActions = new ArrayList<>();

        File ntonFile = new File(workingDir, "reportNtoN");
        File ntonFile2 = new File(workingDir, "reportNtoN.2");
        try (BufferedWriter ntonWriter = new BufferedWriter(new FileWriter(ntonFile));
             BufferedWriter ntonWriter2 = new BufferedWriter(new FileWriter(ntonFile2))) {
            ntonWriter.write(getArtifactComparisonURLs());

            long ctOneToNCount = 0;
            long ctNtoNfromZFINCount = 0;

            List<String> sortedOneToNZFINtoNCBIKeys = oneToNZFINtoNCBI.keySet().stream().sorted().collect(Collectors.toList());

            for (String geneZFINtoMultiNCBI : sortedOneToNZFINtoNCBIKeys) {
                Map<String, Integer> zdbIdsOfNtoN = new HashMap<>(); // In Perl: key -> 1 (exists)
                boolean oneToNflag = true;
                Map<String, String> ref_hashNCBIids = oneToNZFINtoNCBI.get(geneZFINtoMultiNCBI);

                for (String ncbiId : ref_hashNCBIids.keySet()) {
                    if (oneToNNCBItoZFIN.containsKey(ncbiId)) {
                        oneToNflag = false;
                        Map<String, String> ref_hashZdbIds = oneToNNCBItoZFIN.get(ncbiId);
                        for (String zdbId : ref_hashZdbIds.keySet()) {
                            if (oneToNZFINtoNCBI.containsKey(zdbId) || oneToOneZFINtoNCBI.containsKey(zdbId)) {
                                zdbIdsOfNtoN.put(zdbId, 1);
                            } else {
                                print(LOG, "\n\nThere is a bug: " + zdbId + " is one of the mapped ZDB Ids of " + ncbiId + " but could not find a mapped NCBI Id?\n\n");
                            }
                        }
                    }
                }

                if (!oneToNflag) { // N to N case
                    ctNtoNfromZFINCount++;
                    ntonWriter.write(String.format("%d) -------------------------------------------------------------------------------------------------\n", ctNtoNfromZFINCount));
                    ntonWriter2.write(String.format("%d) -------------------------------------------------------------------------------------------------\n", ctNtoNfromZFINCount));
                    LoadReportAction warningAction = new LoadReportAction();
                    List<String> zdbIdsOfNtoNList = zdbIdsOfNtoN.keySet().stream().sorted().collect(Collectors.toList());
                    List<String> ncbiIdsOfNtoNList = ref_hashNCBIids.keySet().stream().sorted().collect(Collectors.toList());
                    ManyToManyProblem problem = new ManyToManyProblem();

                    for (String zdbIdNtoN : zdbIdsOfNtoNList) {
                        List<String> refArrayAccsZFIN = supportedGeneZFIN.getOrDefault(zdbIdNtoN, Collections.emptyList());
                        String zfinSymbol = geneZDBidsSymbols.getOrDefault(zdbIdNtoN, "<no symbol>");
                        problem.addAssociatedDataByZdbID(zdbIdNtoN, zfinSymbol, refArrayAccsZFIN);
                        ntonWriter.write(String.format("%s (%s) [%s]\n", zdbIdNtoN, zfinSymbol, String.join(" ", refArrayAccsZFIN)));
                        warningAction.setType(LoadReportAction.Type.WARNING);
                        warningAction.setSubType("N to N");
                        warningAction.setGeneZdbID(zdbIdNtoN);
                        warningAction.addRelatedActionsKeys(zdbIdNtoN);
                        warningAction.addDetails(String.format("ZFIN gene %s (%s) maps to multiple NCBI genes.\n", zdbIdNtoN, zfinSymbol));
//                        warningAction.addDetails(String.format("%s (%s) [%s]", zdbIdNtoN, zfinSymbol, String.join(" ", refArrayAccsZFIN)));
                        for(String refseq : refArrayAccsZFIN) {
                            warningAction.addRefSeqLink(refseq);
                        }


                        Map<String, String> associatedNCBIgenes = new HashMap<>();
                        if (oneToNZFINtoNCBI.containsKey(zdbIdNtoN)) {
                            associatedNCBIgenes = oneToNZFINtoNCBI.get(zdbIdNtoN);
                        } else if (oneToOneZFINtoNCBI.containsKey(zdbIdNtoN)) {
                            associatedNCBIgenes.put(oneToOneZFINtoNCBI.get(zdbIdNtoN), "1"); // Value not used, just key presence
                        }

                        for (String ncbiId : associatedNCBIgenes.keySet().stream().sorted().collect(Collectors.toList())) {
                            Set<String> refArrayAccsNCBI = supportedGeneNCBI.getOrDefault(ncbiId, Collections.emptySet());
                            String ncbiSymbol = NCBIidsGeneSymbols.getOrDefault(ncbiId, "<no symbol>");

                            problem.addAssociatedDataByNcbiGeneID(ncbiId, ncbiSymbol, refArrayAccsNCBI);
                            ntonWriter.write(String.format("\t%s (%s) [%s]\n", ncbiId, ncbiSymbol, String.join(" ", refArrayAccsNCBI)));

//                            warningAction.addDetails(String.format("\t%s (%s) [%s]\n", ncbiId, ncbiSymbol, String.join(" ", refArrayAccsNCBI)));
                            warningAction.addNcbiGeneIdLink(ncbiId);
                            warningAction.addRelatedActionsKeys(ncbiId);
                            warningAction.setAccession(ncbiId);
                        }
                    }
                    ntonWriter.write("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
//                    warningAction.addDetails("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");

                    for (String ncbiGene : ncbiIdsOfNtoNList) {
                        Set<String> refArrayAccsNCBI = supportedGeneNCBI.getOrDefault(ncbiGene, Collections.emptySet());
                        String ncbiSymbol = NCBIidsGeneSymbols.getOrDefault(ncbiGene, "<no symbol>");
                        problem.addAssociatedDataByNcbiGeneID(ncbiGene, ncbiSymbol, refArrayAccsNCBI);
                        ntonWriter.write(String.format("%s (%s) [%s]\n", ncbiGene, ncbiSymbol, String.join(" ", refArrayAccsNCBI)));
//                        warningAction.addDetails(String.format("%s (%s) [%s]\n", ncbiGene, ncbiSymbol, String.join(" ", refArrayAccsNCBI)));
                        for(String refseq : refArrayAccsNCBI) {
                            warningAction.addRefSeqLink(refseq);
                        }

                        Map<String, String> associatedZFINgenes = new HashMap<>();
                        if (oneToNNCBItoZFIN.containsKey(ncbiGene)) {
                            associatedZFINgenes = oneToNNCBItoZFIN.get(ncbiGene);
                        } else if (oneToOneNCBItoZFIN.containsKey(ncbiGene)) {
                            associatedZFINgenes.put(oneToOneNCBItoZFIN.get(ncbiGene), "1");
                        }

                        for (String zdbId : associatedZFINgenes.keySet().stream().sorted().collect(Collectors.toList())) {
                            List<String> refArrayAccsZFIN = supportedGeneZFIN.getOrDefault(zdbId, Collections.emptyList());
                            String zfinSymbol = geneZDBidsSymbols.getOrDefault(zdbId, "<no symbol>");
                            problem.addAssociatedDataByZdbID(zdbId, zfinSymbol, refArrayAccsZFIN);
                            ntonWriter.write(String.format("\t%s (%s) [%s]\n", zdbId, zfinSymbol, String.join(" ", refArrayAccsZFIN)));
//                            warningAction.addDetails(String.format("\t%s (%s) [%s]\n", zdbId, zfinSymbol, String.join(" ", refArrayAccsZFIN)));
                            warningAction.addZdbIdLink(zdbId, zfinSymbol);
                        }
                    }
                    ntonWriter.write("\n");
                    ntonWriter2.write(problem.summary() + "\n\n");
                    warningAction.setGeneZdbID(String.join(" ", zdbIdsOfNtoNList));
                    warningAction.setAccession(String.join(" ", ncbiIdsOfNtoNList));
                    warningAction.addDetails(problem.summary());
                    loadReportActions.add(warningAction);
                } else { // 1 to N (ZFIN to NCBI) case
                    ctOneToNCount++;
                    oneToN.put(geneZFINtoMultiNCBI, ref_hashNCBIids);
                }
            }
            print(LOG, "\nctOneToN = " + ctOneToNCount + "\nctNtoNfromZFIN = " + ctNtoNfromZFINCount + "\n\n");
            print(STATS_PRIORITY2, "\nMapping result statistics: number of 1:N (ZFIN to NCBI) - " + ctOneToNCount + "\n\n");
            print(STATS_PRIORITY2, "\nMapping result statistics: number of N:N (ZFIN to NCBI) - " + ctNtoNfromZFINCount + "\n\n");

            // Append the second part of N:N reporting (from NCBI to ZFIN perspective) to the same file.
            // This part is generated in getNtoOneAndNtoNfromZFINtoNCBI() in Perl.
            // For Java, we'll call that method and let it append.
            List<LoadReportAction> moreActions = getNtoOneAndNtoNfromZFINtoNCBI(ntonWriter); // Pass the writer
            addToWarningActionsIfNotDuplicate(loadReportActions, moreActions);
        } catch (IOException e) {
            reportErrAndExit("Cannot open or write to reportNtoN: " + e.getMessage());
        }
        return loadReportActions;
    }

    private void getNtoOneAndNtoNfromZFINtoNCBI() {
        // This version is for when called directly from run(), not appending to an existing file.
        // It will create its own reportNtoN or log an error if used incorrectly.
        // However, the logic is now designed to be called by getOneToNNCBItoZFINgeneIds.
        // So, this standalone version might not be strictly needed if the flow is always through there.
        // For now, let's make it clear this is not the primary path for NtoN file generation.
        print(LOG, "WARN: getNtoOneAndNtoNfromZFINtoNCBI() called without BufferedWriter. " +
                   "N:N (NCBI to ZFIN perspective) report part might be missing from reportNtoN if not handled by caller.\n");
        // The actual reporting to file is handled when a BufferedWriter is passed.
        // Here, we just calculate the maps and log counts.
        try {
            // This is a dummy writer to satisfy the method signature if we want to reuse the logic
            // but in practice, the file should be managed by the getOneToNNCBItoZFINgeneIds
            StringWriter sw = new StringWriter();
            BufferedWriter dummyWriter = new BufferedWriter(sw);
            getNtoOneAndNtoNfromZFINtoNCBI(dummyWriter); // Call the main logic
            // The content in sw is not used here, as reportNtoN file is managed by the caller.
        } catch (IOException e) {
            // This should not happen with StringWriter
            reportErrAndExit("Unexpected IOException with StringWriter in getNtoOneAndNtoNfromZFINtoNCBI: " + e.getMessage());
        }
    }

    private List<LoadReportAction> getNtoOneAndNtoNfromZFINtoNCBI(BufferedWriter ntonWriter) throws IOException {
        BufferedWriter ntonWriter3 = new BufferedWriter(new FileWriter(new File(workingDir, "reportNtoN.3")));

        // This method now receives the BufferedWriter to append to reportNtoN
        long ctNtoOne = 0;
        long ctNtoNfromNCBI = 0;
        zdbGeneIdsNtoOneAndNtoN = new HashMap<>(); // ZFIN Gene ID -> NCBI Gene ID (for N:1 ZFIN->NCBI cases)
        List<LoadReportAction> warningActions = new ArrayList<>();

        List<String> sortedOneToNNCBItoZFINKeys = oneToNNCBItoZFIN.keySet().stream().sorted().collect(Collectors.toList());

        for (String geneNCBItoMultiZFIN : sortedOneToNNCBItoZFINKeys) {
            Map<String, Integer> ncbiIdsOfNtoN = new HashMap<>(); // NCBI Gene ID -> 1 (exists)
            boolean oneToNflag = true; // True if 1:N (NCBI to ZFIN), which means N:1 (ZFIN to NCBI)
            Map<String, String> ref_hashZFINids = oneToNNCBItoZFIN.get(geneNCBItoMultiZFIN);
            ManyToManyProblem problem = new ManyToManyProblem();

            for (String zfinId : ref_hashZFINids.keySet()) {
                zdbGeneIdsNtoOneAndNtoN.put(zfinId, geneNCBItoMultiZFIN); // Store for N:1 (ZFIN->NCBI) identification

                if (oneToNZFINtoNCBI.containsKey(zfinId)) {
                    oneToNflag = false; // This indicates an N:N mapping
                    Map<String, String> ref_hashNcbiIds = oneToNZFINtoNCBI.get(zfinId);
                    for (String ncbiId : ref_hashNcbiIds.keySet()) {
                        if (oneToNNCBItoZFIN.containsKey(ncbiId) || oneToOneNCBItoZFIN.containsKey(ncbiId)) {
                            ncbiIdsOfNtoN.put(ncbiId, 1);
                        } else {
                            print(LOG, "\n\nThere is a bug: " + ncbiId + " is one of the mapped NCBI Ids of " + zfinId + " but could not find a mapped ZDB Id?\n\n");
                        }
                    }
                }
            }

            if (!oneToNflag) { // N to N case (from NCBI perspective)
                ctNtoNfromNCBI++;
                ntonWriter.write(String.format("%d -------------------------------------------------------------------------------------------------\n", ctNtoNfromNCBI));
                ntonWriter3.write(String.format("%d -------------------------------------------------------------------------------------------------\n", ctNtoNfromNCBI));
                LoadReportAction warningAction = new LoadReportAction();

                for (String ncbiIdNtoN : ncbiIdsOfNtoN.keySet().stream().sorted().collect(Collectors.toList())) {
                    Set<String> refArrayAccsNCBI = supportedGeneNCBI.getOrDefault(ncbiIdNtoN, Collections.emptySet());
                    String ncbiSymbol = NCBIidsGeneSymbols.getOrDefault(ncbiIdNtoN, "<no symbol>");

                    problem.addAssociatedDataByNcbiGeneID(ncbiIdNtoN, ncbiSymbol, refArrayAccsNCBI);
                    ntonWriter.write(String.format("%s (%s) [%s]\n", ncbiIdNtoN, ncbiSymbol, String.join(" ", refArrayAccsNCBI)));

                    warningAction.setType(LoadReportAction.Type.WARNING);
                    warningAction.setSubType("N to N");
                    warningAction.setAccession(ncbiIdNtoN);
                    warningAction.addRelatedActionsKeys(ncbiIdNtoN);
//                    warningAction.addDetails("NCBI gene " + ncbiIdNtoN + " (" + ncbiSymbol + ") maps to multiple ZFIN genes.\n");
                    warningAction.addDetails(String.format("%s (%s) [%s]\n", ncbiIdNtoN, ncbiSymbol, String.join(" ", refArrayAccsNCBI)));
                    for(String refseq : refArrayAccsNCBI) {
                        warningAction.addRefSeqLink(refseq);
                    }

                    Map<String, String> associatedZFINgenes = new HashMap<>();
                    if (oneToNNCBItoZFIN.containsKey(ncbiIdNtoN)) {
                        associatedZFINgenes = oneToNNCBItoZFIN.get(ncbiIdNtoN);
                    } else if (oneToOneNCBItoZFIN.containsKey(ncbiIdNtoN)) {
                        associatedZFINgenes.put(oneToOneNCBItoZFIN.get(ncbiIdNtoN), "1");
                    }


                    for (String zdbId : associatedZFINgenes.keySet().stream().sorted().collect(Collectors.toList())) {
                        List<String> refArrayAccsZFIN = supportedGeneZFIN.getOrDefault(zdbId, Collections.emptyList());
                        String zfinSymbol = geneZDBidsSymbols.getOrDefault(zdbId, "<no symbol>");
                        problem.addAssociatedDataByZdbID(zdbId, zfinSymbol, refArrayAccsZFIN);
                        ntonWriter.write(String.format("\t%s (%s) [%s]\n", zdbId, zfinSymbol, String.join(" ", refArrayAccsZFIN)));
                        warningAction.addDetails(String.format("\t%s (%s) [%s]\n", zdbId, zfinSymbol, String.join(" ", refArrayAccsZFIN)));
                        warningAction.addZdbIdLink(zdbId, zfinSymbol);
                        warningAction.setGeneZdbID(zdbId);
                        warningAction.addRelatedActionsKeys(zdbId);
                        for(String refseq : refArrayAccsZFIN) {
                            warningAction.addRefSeqLink(refseq);
                        }
                    }
                }
                ntonWriter.write("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
                warningAction.addDetails("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");

                for (String zdbId : ref_hashZFINids.keySet().stream().sorted().collect(Collectors.toList())) {
                    List<String> refArrayAccsZFIN = supportedGeneZFIN.getOrDefault(zdbId, Collections.emptyList());
                    String zfinSymbol = geneZDBidsSymbols.getOrDefault(zdbId, "<no symbol>");
                    problem.addAssociatedDataByZdbID(zdbId, zfinSymbol, refArrayAccsZFIN);
                    ntonWriter.write(String.format("%s (%s) [%s]\n", zdbId, zfinSymbol, String.join(" ", refArrayAccsZFIN)));
                    warningAction.addDetails(String.format("%s (%s) [%s]\n", zdbId, zfinSymbol, String.join(" ", refArrayAccsZFIN)));
                    warningAction.addZdbIdLink(zdbId, zfinSymbol);
                    warningAction.addRelatedActionsKeys(zdbId);

                    Map<String, String> associatedNCBIgenes = new HashMap<>();
                    if (oneToNZFINtoNCBI.containsKey(zdbId)) {
                        associatedNCBIgenes = oneToNZFINtoNCBI.get(zdbId);
                    } else if (oneToOneZFINtoNCBI.containsKey(zdbId)) {
                        associatedNCBIgenes.put(oneToOneZFINtoNCBI.get(zdbId), "1");
                    }

                    for (String ncbiGene : associatedNCBIgenes.keySet().stream().sorted().collect(Collectors.toList())) {
                        Set<String> refArrayAccsNCBI = supportedGeneNCBI.getOrDefault(ncbiGene, Collections.emptySet());
                        String ncbiSymbol = NCBIidsGeneSymbols.getOrDefault(ncbiGene, "<no symbol>");
                        problem.addAssociatedDataByNcbiGeneID(ncbiGene, ncbiSymbol, refArrayAccsNCBI);
                        ntonWriter.write(String.format("\t%s (%s) [%s]\n", ncbiGene, ncbiSymbol, String.join(" ", refArrayAccsNCBI)));
                        warningAction.addDetails(String.format("\t%s (%s) [%s]\n", ncbiGene, ncbiSymbol, String.join(" ", refArrayAccsNCBI)));
                        warningAction.addNcbiGeneIdLink(ncbiGene);
                        warningAction.addRelatedActionsKeys(ncbiGene);
                    }
                }
                ntonWriter.write("\n");
                warningActions.add(warningAction);
            } else { // 1 to N (NCBI to ZFIN), which is N:1 (ZFIN to NCBI)
                ctNtoOne++;
                nToOne.put(geneNCBItoMultiZFIN, ref_hashZFINids);
            }
            ntonWriter3.write(problem.summary() + "\n\n");
        }
        // Note: Perl script closes NTON file here. In Java, the caller of getOneToNNCBItoZFINgeneIds (which also calls this) will close it.
        // The headers for NtoN report are written by the caller.

        print(LOG, "\nctNtoOne = " + ctNtoOne + "\nctNtoNfromNCBI = " + ctNtoNfromNCBI + "\n\n");
        print(STATS_PRIORITY2, "\nMapping result statistics: number of N:1 (ZFIN to NCBI) - " + ctNtoOne + "\n\n");
        print(STATS_PRIORITY2, "\nMapping result statistics: number of N:N (NCBI to ZFIN) - " + ctNtoNfromNCBI + "\n\n");

        // Send email with NtoN report (this was after close NTON in Perl)
        // The file is managed by the caller, so it should be complete at this point.
        String subject = "Auto from " + instance + ": NCBI_gene_load.pl :: List of N to N";
        sendMailWithAttachedReport(env("SWISSPROT_EMAIL_REPORT"), subject, new File(workingDir, "reportNtoN").getAbsolutePath());

        ntonWriter3.close();
        return warningActions;
    }

    public static void addToWarningActionsIfNotDuplicate(List<LoadReportAction> warningActions, List<LoadReportAction> moreWarningAction) {
        for(LoadReportAction action : moreWarningAction) {
            addToWarningActionsIfNotDuplicate(warningActions, action);
        }
    }

    public static void addToWarningActionsIfNotDuplicate(List<LoadReportAction> warningActions, LoadReportAction warningAction) {
        String geneZdbID = warningAction.getGeneZdbID();
        String accession = warningAction.getAccession();

        //Matching logic for N to N warnings means that there exists a warningAction in the list
        // with the same geneZdbID and accession. In the case where a warningAction has multiple
        // multiple geneZdbIDs or accessions (space separated), we need to check if the new
        // warning action is a subset of an existing one.
        for (LoadReportAction existingAction : warningActions) {
            String existingGeneZdbID = existingAction.getGeneZdbID();
            String existingAccession = existingAction.getAccession();

            Set<String> existingGeneZdbIDSet = new HashSet<>(Arrays.asList(existingGeneZdbID.split(" ")));
            Set<String> existingAccessionSet = new HashSet<>(Arrays.asList(existingAccession.split(" ")));

            Set<String> newGeneZdbIDSet = new HashSet<>(Arrays.asList(geneZdbID.split(" ")));
            Set<String> newAccessionSet = new HashSet<>(Arrays.asList(accession.split(" ")));

            if (existingGeneZdbIDSet.containsAll(newGeneZdbIDSet) && existingAccessionSet.containsAll(newAccessionSet)) {
                // The new warningAction is a subset of an existing one, so we do not add it.
                return;
            }
        }
        // If no duplicates found, add the new warningAction
        warningActions.add(warningAction);
    }


    // Overload for initial call without a writer

    private List<LoadReportAction> reportOneToN() {
        File reportFile = new File(workingDir, "reportOneToN");
        List<LoadReportAction> warningActions = new ArrayList<>();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(reportFile))) {
            writer.write(getArtifactComparisonURLs());
            long ct = 0;

            List<String> sortedZfinGeneIds = oneToN.keySet().stream().sorted().collect(Collectors.toList());

            for (String zdbId : sortedZfinGeneIds) {
                ManyToManyProblem problem = new ManyToManyProblem();
                LoadReportAction warningAction = new LoadReportAction();
                warningAction.setType(LoadReportAction.Type.WARNING);
                warningAction.setSubType("1 to N");
                Map<String, String> refHashMultiNCBIgenes = oneToN.get(zdbId);
                ct++;
                writer.write(String.format("%d) ---------------------------------------------\n", ct));

                List<String> zfinAccessions = supportedGeneZFIN.getOrDefault(zdbId, Collections.emptyList());
                String zfinSymbol = geneZDBidsSymbols.getOrDefault(zdbId, "<unknown ZFIN symbol>");
                writer.write(String.format("%s (%s) [%s]\n\n", zdbId, zfinSymbol, String.join(" ", zfinAccessions)));
//                warningAction.addDetails(String.format("%s (%s) [%s]", zdbId, zfinSymbol, String.join(" ", zfinAccessions)));
                problem.addAssociatedDataByZdbID(zdbId, zfinSymbol, zfinAccessions);
                warningAction.addZdbIdLink(zdbId, zfinSymbol);
                warningAction.setGeneZdbID(zdbId);
                warningAction.addRelatedActionsKeys(zdbId);

                for (String refseq : zfinAccessions) {
                    warningAction.addRefSeqLink(refseq);
                }

                List<String> sortedNcbiGeneIds = refHashMultiNCBIgenes.keySet().stream().sorted().collect(Collectors.toList());

                for (String ncbiId : sortedNcbiGeneIds) {
                    // String mappingAccession = refHashMultiNCBIgenes.get(ncbiId); // Not used in output per instruction
                    Set<String> ncbiAccessions = supportedGeneNCBI.getOrDefault(ncbiId, Collections.emptySet());
                    String ncbiSymbol = NCBIidsGeneSymbols.getOrDefault(ncbiId, "<no gene symbol>"); // Perl used <no gene symbol>
                    writer.write(String.format("   %s (%s) [%s]\n\n", ncbiId, ncbiSymbol, String.join(" ", ncbiAccessions)));
//                    warningAction.addDetails(String.format("   %s (%s) [%s]\n\n", ncbiId, ncbiSymbol, String.join(" ", ncbiAccessions)));
                    problem.addAssociatedDataByNcbiGeneID(ncbiId, ncbiSymbol, ncbiAccessions);
                    warningAction.addNcbiGeneIdLink(ncbiId);
                    warningAction.addRelatedActionsKeys(ncbiId);
                }
                warningAction.setAccession(String.join(" ", sortedNcbiGeneIds));
                warningAction.addDetails(problem.summary());
                warningActions.add(warningAction);
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

        sendMailWithAttachedReport(
                env("SWISSPROT_EMAIL_REPORT"),
                "Auto from " + instance + ": NCBI_gene_load.pl :: List of 1 to N",
                reportFile.getAbsolutePath()
        );
        return warningActions;
    }

    private List<LoadReportAction> reportNtoOne() {
        File reportFile = new File(workingDir, "reportNtoOne");
        List<LoadReportAction> warningActions = new ArrayList<>();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(reportFile))) {
            writer.write(getArtifactComparisonURLs());
            long ct = 0;

            List<String> sortedNcbiGeneIds = nToOne.keySet().stream().sorted().collect(Collectors.toList());

            for (String ncbiId : sortedNcbiGeneIds) {
                ManyToManyProblem problem = new ManyToManyProblem();
                LoadReportAction warningAction = new LoadReportAction();
                warningAction.setType(LoadReportAction.Type.WARNING);
                warningAction.setSubType("N to 1");
                warningAction.setAccession(ncbiId);
                warningAction.addRelatedActionsKeys(ncbiId);
                warningAction.addNcbiGeneIdLink(ncbiId);
//                warningAction.addDetails("NCBI gene " + ncbiId + " maps to multiple ZFIN genes.\n");

                Map<String, String> refHashMultiZFINgenes = nToOne.get(ncbiId);
                ct++;
                writer.write(String.format("%d) ---------------------------------------------\n", ct));

                Set<String> ncbiAccessions = supportedGeneNCBI.getOrDefault(ncbiId, Collections.emptySet());
                String ncbiSymbol = NCBIidsGeneSymbols.getOrDefault(ncbiId, "<unknown NCBI symbol>");
                writer.write(String.format("%s (%s) [%s]\n\n", ncbiId, ncbiSymbol, String.join(" ", ncbiAccessions)));
                problem.addAssociatedDataByNcbiGeneID(ncbiId, ncbiSymbol, ncbiAccessions);
//                warningAction.addDetails(String.format("%s (%s) [%s]\n\n", ncbiId, ncbiSymbol, String.join(" ", ncbiAccessions)));

                List<String> sortedZfinGeneIds = refHashMultiZFINgenes.keySet().stream().sorted().collect(Collectors.toList());

                for (String zdbId : sortedZfinGeneIds) {
                    List<String> zfinAccessions = supportedGeneZFIN.getOrDefault(zdbId, Collections.emptyList());
                    String zfinSymbol = geneZDBidsSymbols.getOrDefault(zdbId, "<unknown ZFIN symbol>");
                    writer.write(String.format("   %s (%s) [%s]\n\n", zdbId, zfinSymbol, String.join(" ", zfinAccessions)));
//                    warningAction.addDetails(String.format("   %s (%s) [%s]\n\n", zdbId, zfinSymbol, String.join(" ", zfinAccessions)));
                    problem.addAssociatedDataByZdbID(zdbId, zfinSymbol, zfinAccessions);
                    warningAction.addZdbIdLink(zdbId);
                    warningAction.addRelatedActionsKeys(zdbId);
                }
                warningAction.setGeneZdbID(String.join(" ", sortedZfinGeneIds));
                warningAction.addDetails(problem.summary());
                warningActions.add(warningAction);
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

        sendMailWithAttachedReport(
                env("SWISSPROT_EMAIL_REPORT"),
                "Auto from " + instance + ": NCBI_gene_load.pl :: List of N to 1",
                reportFile.getAbsolutePath()
        );
        return warningActions;
    }

    private void buildVegaIDMappings()  {
        ZDBgeneAndVegaGeneIds = new HashMap<>();
        VegaGeneAndZDBgeneIds = new HashMap<>();
        ZDBgeneWithMultipleVegaGeneIds = new HashMap<>();
        vegaGeneIdWithMultipleZFINgenes = new HashMap<>();

        String sqlGetVEGAidAndGeneZDBId = String.format("""
            select mrel_mrkr_1_zdb_id, dblink_acc_num
            from marker_relationship, db_link
            where mrel_mrkr_2_zdb_id = dblink_linked_recid
              and dblink_fdbcont_zdb_id = '%s'
              and (mrel_mrkr_1_zdb_id like 'ZDB-GENE%%' or mrel_mrkr_1_zdb_id like '%%RNAG%%')
              and dblink_acc_num like 'OTTDARG%%'
              and mrel_type = 'gene produces transcript'
            """, fdcontVega);

        NativeQuery<Tuple> query = currentSession().createNativeQuery(sqlGetVEGAidAndGeneZDBId, Tuple.class);
        List<Tuple> results = query.list();

        long ctTotalZDBgeneIdVegaGeneIds = 0;

        for (Tuple row : results) {
            ctTotalZDBgeneIdVegaGeneIds++;
            String geneZdbId = row.get(0, String.class);
            String vegaGeneId = row.get(1, String.class);

            // Logic for ZDBgeneWithMultipleVegaGeneIds
            if (ZDBgeneWithMultipleVegaGeneIds.containsKey(geneZdbId) ||
                (ZDBgeneAndVegaGeneIds.containsKey(geneZdbId) && !ZDBgeneAndVegaGeneIds.get(geneZdbId).equals(vegaGeneId))) {
                ZDBgeneWithMultipleVegaGeneIds.computeIfAbsent(geneZdbId, k -> {
                    List<String> list = new ArrayList<>();
                    if (ZDBgeneAndVegaGeneIds.containsKey(k)) list.add(ZDBgeneAndVegaGeneIds.get(k)); // Add the first one found
                    return list;
                }).add(vegaGeneId);

                // Keep duplicates
            }
            ZDBgeneAndVegaGeneIds.put(geneZdbId, vegaGeneId); // This will overwrite, keeping the last one, as in Perl

            // Logic for vegaGeneIdWithMultipleZFINgenes
            if (vegaGeneIdWithMultipleZFINgenes.containsKey(vegaGeneId) ||
                (VegaGeneAndZDBgeneIds.containsKey(vegaGeneId) && !VegaGeneAndZDBgeneIds.get(vegaGeneId).equals(geneZdbId))) {
                vegaGeneIdWithMultipleZFINgenes.computeIfAbsent(vegaGeneId, k -> {
                    List<String> list = new ArrayList<>();
                    if (VegaGeneAndZDBgeneIds.containsKey(k)) list.add(VegaGeneAndZDBgeneIds.get(k));
                    return list;
                }).add(geneZdbId);

                // Keep duplicates
            }
            VegaGeneAndZDBgeneIds.put(vegaGeneId, geneZdbId); // This will overwrite, keeping the last one
        }

        print(LOG, "\nctTotalZDBgeneIdVegaGeneIds = " + ctTotalZDBgeneIdVegaGeneIds + "\n\n");
        print(STATS_PRIORITY2, "\nThe total number of ZFIN genes with Vega Gene Id: " + ctTotalZDBgeneIdVegaGeneIds + "\n\n");

        long ctVegaIdWithMultipleZDBgene = 0;
        print(LOG, "\nThe following Vega Gene Ids at ZFIN correspond to multiple ZDB Gene Ids\n");
        for (String vega : vegaGeneIdWithMultipleZFINgenes.keySet().stream().sorted().collect(Collectors.toList())) {
            ctVegaIdWithMultipleZDBgene++;
            List<String> zdbGenes = vegaGeneIdWithMultipleZFINgenes.get(vega);
            print(LOG, vega + " " + String.join(" ", zdbGenes) + "\n");
        }
        print(LOG, "\nctVegaIdWithMultipleZDBgene = " + ctVegaIdWithMultipleZDBgene + "\n\n");

        File reportZdbGeneMultipleVega = new File(workingDir, "reportZDBgeneIdWithMultipleVegaIds");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(reportZdbGeneMultipleVega))) {
            long ctZDBgeneIdWithMultipleVegaId = 0;
            for (String zdbGene : ZDBgeneWithMultipleVegaGeneIds.keySet().stream().sorted().collect(Collectors.toList())) {
                ctZDBgeneIdWithMultipleVegaId++;
                List<String> vegaIds = ZDBgeneWithMultipleVegaGeneIds.get(zdbGene);
                writer.write(zdbGene + " " + String.join(" ", vegaIds) + "\n");
            }
            print(LOG, "\nctZDBgeneIdWithMultipleVegaId = " + ctZDBgeneIdWithMultipleVegaId + "\n\n");
        } catch (IOException e) {
            reportErrAndExit("Cannot open or write to reportZDBgeneIdWithMultipleVegaIds: " + e.getMessage());
        }
    }

    private void writeCommonVegaGeneIdMappings()  {
        oneToOneViaVega = new HashMap<>(); // NCBI Gene ID -> ZDB Gene ID
        long ctMappedViaVega = 0;

        for (String zdbId : geneZDBidsSymbols.keySet().stream().sorted().collect(Collectors.toList())) {
            // Exclude genes already mapped via RNA or involved in complex RNA mappings
            if (mapped.containsKey(zdbId) ||
                oneToNZFINtoNCBI.containsKey(zdbId) ||
                (zdbGeneIdsNtoOneAndNtoN != null && zdbGeneIdsNtoOneAndNtoN.containsKey(zdbId)) || // zdbGeneIdsNtoOneAndNtoN is ZFIN -> NCBI
                geneZFINwithAccSupportingMoreThan1.containsKey(zdbId)) {
                continue;
            }

            // Check if ZFIN gene has a unique Vega ID
            if (ZDBgeneAndVegaGeneIds.containsKey(zdbId) && !ZDBgeneWithMultipleVegaGeneIds.containsKey(zdbId)) {
                String vegaGeneIdAtZfin = ZDBgeneAndVegaGeneIds.get(zdbId);

                // Check if this Vega ID maps uniquely to an NCBI gene and is not problematic at NCBI/ZFIN Vega level
                if (vegaIdsNCBIids.containsKey(vegaGeneIdAtZfin) &&
                    !vegaIdwithMultipleNCBIids.containsKey(vegaGeneIdAtZfin) &&
                    !vegaGeneIdWithMultipleZFINgenes.containsKey(vegaGeneIdAtZfin)) {

                    String ncbiGeneIdMappedViaVega = vegaIdsNCBIids.get(vegaGeneIdAtZfin);

                    // Further exclude NCBI genes that are problematic (multiple Vegas, complex RNA, or already RNA-mapped)
                    if (!NCBIgeneWithMultipleVega.containsKey(ncbiGeneIdMappedViaVega) &&
                        !geneNCBIwithAccSupportingMoreThan1.containsKey(ncbiGeneIdMappedViaVega) &&
                        !mappedReversed.containsKey(ncbiGeneIdMappedViaVega)) {

                        try {
                            // Format: zdbId|NCBIgeneIdMappedViaVega|||fdcontNCBIgeneId|pubMappedbasedOnVega
                            TOLOAD.write(String.format("%s|%s|||%s|%s\n",
                                    zdbId, ncbiGeneIdMappedViaVega, fdcontNCBIgeneId, pubMappedbasedOnVega));
                            this.ctToLoad++;
                            oneToOneViaVega.put(ncbiGeneIdMappedViaVega, zdbId);
                            ctMappedViaVega++;
                        } catch (IOException e) {
                            reportErrAndExit("Error writing to TOLOAD in writeCommonVegaGeneIdMappings for ZDB ID " + zdbId + ": " + e.getMessage());
                        }
                    }
                }
            }
        }

        long ctTotalMapped = ctMappedViaVega + this.ctOneToOneNCBI; // ctOneToOneNCBI is from RNA mapping
        print(LOG, "\nctMappedViaVega = " + ctMappedViaVega + "\n\nTotal number of the gene records mapped: " + ctMappedViaVega + " + " + this.ctOneToOneNCBI + " = " + ctTotalMapped + "\n\n");
        print(STATS_PRIORITY2, "\nMapping result via Vega Gene Id: " + ctMappedViaVega + " additional gene records are mapped\n\n");
        print(STATS_PRIORITY2, "Total number of the gene records mapped: " + ctMappedViaVega + " + " + this.ctOneToOneNCBI + " = " + ctTotalMapped + "\n\n");
    }

    private void calculateLengthForAccessionsWithoutLength()  {
        File noLengthUnlFile = new File(workingDir, "noLength.unl");
        int accessionsToWriteCount = 0;

        //we only care about the accessions that are mapped to NCBI or ZFIN
        Map<String, String> noLengthAccessionsFiltered = noLength.entrySet().stream().filter(
                        entry -> mappedReversed.containsKey(entry.getValue()) || oneToOneViaVega.containsKey(entry.getValue()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));
        accessionsToWriteCount = noLengthAccessionsFiltered.size();
        try {
            FileUtils.writeLines(noLengthUnlFile, noLengthAccessionsFiltered.keySet());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        print(LOG, "Wrote " + accessionsToWriteCount + " accessions to " + noLengthUnlFile.getName() + "\n");
        outputDate(); // Corresponds to system("/bin/date");

        if (!noLengthUnlFile.exists()) { // Should not happen if previous step succeeded
            print(LOG, "\nCannot find " + noLengthUnlFile.getName() + " as input file for efetch.\n\n");
            reportErrAndExit("Auto from " + instance + ": NCBI_gene_load.pl :: no input file for efetch.r");
            return;
        }

        print(LOG, "\nStart efetching at " + nowToString("yyyy-MM-dd HH:mm:ss") + " \n");
        System.out.println("\nStart efetching " + nowToString("yyyy-MM-dd HH:mm:ss") + " \n");

        File seqFastaFile = new File(workingDir, "seq.fasta");

        if (!envTrue("SKIP_DOWNLOADS") || envTrue("FORCE_EFETCH")) {
            print(LOG, "\nRunning the job for efetch step.\n");
            System.out.println("\nRunning the job for efetch step.\n");

            if (!envTrue("SKIP_EFETCH")) {
                BatchNCBIFastaFetchTask task = new BatchNCBIFastaFetchTask(
                        noLengthUnlFile.getAbsolutePath(),
                        seqFastaFile.getAbsolutePath()
                );
                print(LOG, "\nExecuting BatchNCBIFastaFetchTask: " + task.toString() + "\n");
                task.run();
            } else {
                print(LOG, "\nSKIP_EFETCH is set, so skipping the efetch step.\n");
                System.out.println("\nSKIP_EFETCH is set, so skipping the efetch step.\n");
            }
        } else {
            print(LOG, "\nSKIP_DOWNLOADS is set and FORCE_EFETCH is not, so skipping the efetch step.\n\n");
            System.out.println("\nSKIP_DOWNLOADS is set and FORCE_EFETCH is not, so skipping the efetch step.\n\n");
        }

        print(LOG, "\nAfter efetching at " + nowToString("yyyy-MM-dd HH:mm:ss") + " \n");
        System.out.println("\nAfter efetching at " + nowToString("yyyy-MM-dd HH:mm:ss") + " \n");
        outputDate();

        if (!seqFastaFile.exists()) {
            print(LOG, "\n No " + seqFastaFile.getName() + " found (maybe issue with efetch): $! \n\n"); // $! is Perl specific
            reportErrAndExit("Auto from " + instance + ": NCBI_gene_load.pl :: ERROR with efetch, " + seqFastaFile.getName() + " not found.");
            return;
        }

        String md5 = md5File(seqFastaFile, LOG);
        long fileSize = seqFastaFile.length();
        System.out.println(seqFastaFile.getName() + " md5: " + (md5 != null ? md5 : "N/A"));
        System.out.println(seqFastaFile.getName() + " size: " + fileSize);

        File lengthUnlFile = new File(workingDir, "length.unl");
        String fastaLenCommand = env("TARGETROOT") + "/server_apps/data_transfer/NCBIGENE/fasta_len.pl";
        if (!new File(fastaLenCommand).exists()) {
            System.out.println("FASTA_LEN_COMMAND not found at " + fastaLenCommand);
            print(LOG, "\nError happened when execute " + fastaLenCommand + " " + seqFastaFile.getName() + " > " + lengthUnlFile.getName() + "\n\n");
            reportErrAndExit("Auto from " + instance + ": NCBI_gene_load.pl :: ERROR with fasta_len.pl command not found at " + fastaLenCommand);
        }

        String cmdCalLengthString = String.format("%s %s > %s",
                fastaLenCommand,
                seqFastaFile.getAbsolutePath(),
                lengthUnlFile.getAbsolutePath());
        doSystemCommand(List.of("bash", "-c", cmdCalLengthString), "len_calc_out.log", "len_calc_err.log");

        if (!lengthUnlFile.exists()) {
            print(LOG, "\nError happened when execute " + fastaLenCommand + " " + seqFastaFile.getName() + " > " + lengthUnlFile.getName() + "\n\n");
            reportErrAndExit("Auto from " + instance + ": NCBI_gene_load.pl :: ERROR with " + fastaLenCommand);
            return;
        }

        long ctSeqLengthCalculated = 0;
        Pattern lengthPattern = Pattern.compile("^(\\S+)\\|(\\d+)\\|$");
        try (BufferedReader reader = new BufferedReader(new FileReader(lengthUnlFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = lengthPattern.matcher(line.trim());
                if (matcher.find()) {
                    String accession = matcher.group(1);
                    try {
                        Integer length = Integer.parseInt(matcher.group(2));
                        sequenceLength.put(accession, length);
                        ctSeqLengthCalculated++;
                    } catch (NumberFormatException e) {
                        print(LOG, "WARN: Could not parse length as integer for accession " + accession + " from line: [" + line + "]. Error: " + e.getMessage() + "\n");
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        print(LOG, "\nctSeqLengthCalculated = " + ctSeqLengthCalculated + "\n\n");
    }

    private void getGenBankAndRefSeqsWithZfinGenes() {
        geneAccFdbcont = new HashMap<>();

        String sqlGetGenBankAndRefSeqAccs = String.format(
                """
                select dblink_linked_recid, dblink_acc_num, dblink_fdbcont_zdb_id, dblink_zdb_id
                from db_link
                where (dblink_linked_recid like 'ZDB-GENE%%' or dblink_linked_recid like '%%RNAG%%')
                  and dblink_fdbcont_zdb_id in ('%s','%s','%s','%s','%s','%s')
                """,
                fdcontGenBankRNA, fdcontGenPept, fdcontGenBankDNA,
                fdcontRefSeqRNA, fdcontRefPept, fdcontRefSeqDNA
        );

        System.out.println("DEBUGGING: sqlGetGenBankAndRefSeqAccs:\n" + sqlGetGenBankAndRefSeqAccs);

        NativeQuery<Tuple> query = HibernateUtil.currentSession().createNativeQuery(sqlGetGenBankAndRefSeqAccs, Tuple.class);
        List<Tuple> results = query.list();

        long ctGeneAccFdbcont = 0;

        for (Tuple row : results) {
            String gene = row.get(0, String.class);
            String acc = row.get(1, String.class);
            String fdbcont = row.get(2, String.class);
            String dblinkId = row.get(3, String.class);

            if (toDelete == null || !toDelete.containsKey(dblinkId)) {
                String hashKey = gene + acc + fdbcont;
                geneAccFdbcont.put(hashKey, dblinkId);
                ctGeneAccFdbcont++;
            }
        }
        print(LOG, "\nctGeneAccFdbcont = " + ctGeneAccFdbcont + "\n\n");
    }

    private void writeGenBankRNAaccessionsWithMappedGenesToLoad() {
        try {
            List<String> sortedKeys = accNCBIsupportingOnly1.keySet().stream().sorted().collect(Collectors.toList());
            for (String genBankRNA : sortedKeys) {
                String ncbiGeneId = accNCBIsupportingOnly1.get(genBankRNA);
                String[] zdbGeneAndPub = getZdbGeneIdAndAttributionByNCBIgeneId(ncbiGeneId);
                String zdbGeneId = zdbGeneAndPub[0];
                String attributionPub = zdbGeneAndPub[1];

                if (zdbGeneId == null) continue;

                String hashKey = zdbGeneId + genBankRNA + fdcontGenBankRNA;
                if (geneAccFdbcont.containsKey(hashKey)) continue;

                Integer lengthVal = sequenceLength.get(genBankRNA);
                String lengthStr = (lengthVal != null) ? lengthVal.toString() : "";

                // Format: zdbGeneId|GenBankRNA||length|fdcontGenBankRNA|attributionPub
                TOLOAD.write(String.format("%s|%s||%s|%s|%s\n",
                        zdbGeneId, genBankRNA, lengthStr, fdcontGenBankRNA, attributionPub));
                geneAccFdbcont.put(hashKey, "1"); // Mark as added to prevent re-adding
                this.ctToLoad++;
            }
        } catch (IOException e) {
            reportErrAndExit("Error writing GenBank RNA accessions to toLoad.unl: " + e.getMessage());
        }
    }

    private void initializeGenPeptAccessionsMap() {
        GenPeptAttributedToNonLoadPub = new HashMap<>();
        GenPeptDbLinkIdAttributedToNonLoadPub = new HashMap<>();

        String sqlGenPeptAttributedToNonLoadPub = String.format("""
            select dblink_acc_num, dblink_zdb_id, recattrib_source_zdb_id
            from record_attribution, db_link
            where recattrib_data_zdb_id = dblink_zdb_id
              and dblink_fdbcont_zdb_id = '%s'
              and (dblink_linked_recid like 'ZDB-GENE%%' or dblink_linked_recid like '%%RNAG%%')
              and recattrib_source_zdb_id not in ('%s','%s','%s')
            """, fdcontGenPept, pubMappedbasedOnRNA, pubMappedbasedOnVega, pubMappedbasedOnNCBISupplement);
        //Note: Perl had pubMappedbasedOnNCBISupplement missing in the IN clause in the original file for this query.
        //Kept it as is to match original. If this was an omission, it should be added.
        //Looking again, the Perl code for this specific query `sqlGenPeptAttributedToNonLoadPub`
        // `and recattrib_source_zdb_id not in ('$pubMappedbasedOnRNA','$pubMappedbasedOnVega');`
        // *does not* include $pubMappedbasedOnNCBISupplement. I will match that.

        sqlGenPeptAttributedToNonLoadPub = String.format("""
            select dblink_acc_num, dblink_zdb_id, recattrib_source_zdb_id
            from record_attribution, db_link
            where recattrib_data_zdb_id = dblink_zdb_id
              and dblink_fdbcont_zdb_id = '%s'
              and (dblink_linked_recid like 'ZDB-GENE%%' or dblink_linked_recid like '%%RNAG%%')
              and recattrib_source_zdb_id not in ('%s','%s')
            """, fdcontGenPept, pubMappedbasedOnRNA, pubMappedbasedOnVega);


        NativeQuery<Tuple> query = currentSession().createNativeQuery(sqlGenPeptAttributedToNonLoadPub, Tuple.class);
        List<Tuple> results = query.list();

        long ctGenPeptNonLoadPub = 0;
        for (Tuple row : results) {
            String genPeptAcc = row.get(0, String.class);
            String dbLinkId = row.get(1, String.class);
            String nonLoadPub = row.get(2, String.class);

            GenPeptAttributedToNonLoadPub.put(genPeptAcc, nonLoadPub);
            GenPeptDbLinkIdAttributedToNonLoadPub.put(genPeptAcc, dbLinkId);
            ctGenPeptNonLoadPub++;
        }
        print(LOG, "\nNumber of GenPept accessions attributed to non-load pub: " + ctGenPeptNonLoadPub + "\n\n");
        // The Perl script also prints scalar(keys %GenPeptAttributedToNonLoadPub), which should be the same.
        print(LOG, "\nctGenPeptAttributedToNonLoadPub (map size) = " + GenPeptAttributedToNonLoadPub.size() + "\n\n");
    }

    private void processGenBankAccessionsAssociatedToNonLoadPubs() {
        GenPeptsToLoad = new HashMap<>(); // GenPept Acc -> ZFIN Gene ID
        File moreToDeleteFile = new File(workingDir, "toDelete.unl"); // Appending to existing file
        long ctToAttribute = 0;

        print(LOG, "\nThe GenPept accessions used to attribute to non-load publication now attribute to load pub:\n\n");
        print(LOG, "GenPept\tZFIN gene Id\tnon-load pub\tload pub\n");
        print(LOG, "-------\t------------\t------------\t--------\n");

        try (BufferedWriter moreToDeleteWriter = new BufferedWriter(new FileWriter(moreToDeleteFile, true))) { // true for append
            List<String> sortedGenPeptKeys = GenPeptNCBIgeneIds.keySet().stream().sorted().collect(Collectors.toList());

            for (String genPept : sortedGenPeptKeys) {
                String ncbiGeneId = GenPeptNCBIgeneIds.get(genPept);
                String[] zdbGeneAndPub = getZdbGeneIdAndAttributionByNCBIgeneId(ncbiGeneId);
                String zdbGeneId = zdbGeneAndPub[0];
                String attributionPub = zdbGeneAndPub[1];

                if (zdbGeneId == null) continue;

                String hashKey = zdbGeneId + genPept + fdcontGenPept;
                Integer lengthVal = sequenceLength.get(genPept);
                String lengthStr = (lengthVal != null) ? lengthVal.toString() : "";

                if (!geneAccFdbcont.containsKey(hashKey)) {
                    TOLOAD.write(String.format("%s|%s||%s|%s|%s\n",
                            zdbGeneId, genPept, lengthStr, fdcontGenPept, attributionPub));
                    geneAccFdbcont.put(hashKey, "1"); // Mark as added
                    this.ctToLoad++;
                    GenPeptsToLoad.put(genPept, zdbGeneId);
                } else if (GenPeptAttributedToNonLoadPub.containsKey(genPept) &&
                           GenPeptDbLinkIdAttributedToNonLoadPub.containsKey(genPept)) {
                    String moreToDeleteDblinkId = GenPeptDbLinkIdAttributedToNonLoadPub.get(genPept);
                    moreToDeleteWriter.write(moreToDeleteDblinkId + "\n");
                    toDelete.put(moreToDeleteDblinkId, 1); // Add to the main toDelete map

                    TOLOAD.write(String.format("%s|%s||%s|%s|%s\n",
                            zdbGeneId, genPept, lengthStr, fdcontGenPept, attributionPub));
                    // geneAccFdbcont already contains this key, so we are essentially replacing its attribution
                    // No need to increment ctToLoad again if it was already counted, but the logic here implies
                    // it might be a new load line if the attribution changes.
                    // The original Perl script adds to TOLOAD and increments ctToLoad regardless here.
                    // Let's assume the old dblink is deleted and a new one is loaded.
                    this.ctToLoad++; //This logic matches Perl, which would write a new line to toLoad.unl

                    print(LOG, String.format("%s\t%s\t%s\t%s\n",
                            genPept, zdbGeneId, GenPeptAttributedToNonLoadPub.get(genPept), attributionPub));
                    ctToAttribute++;
                }
            }
        } catch (IOException e) {
            reportErrAndExit("Error in processGenBankAccessionsAssociatedToNonLoadPubs: " + e.getMessage());
        }

        print(LOG, "---------------------------------------------------------------\nTotal: " + ctToAttribute + "\n\n");
        print(STATS_PRIORITY2, String.format("\nNon-load attribution for the %d manually curated GenPept db_link records get replaced by;\n 1 of the 2 load pubs (depending on mapping type).\n\n", ctToAttribute));
    }

    private void printGenPeptsAssociatedWithGeneAtZFIN() {
        Map<String, String> allGenPeptWithGeneZFIN = new HashMap<>(); // Acc -> GeneZDBID (last one encountered)
        Map<String, List<String>> genPeptWithMultipleZDBgene = new HashMap<>(); // Acc -> List<GeneZDBID>

        String sqlAllGenPeptWithGeneZFIN = String.format("""
            select dblink_acc_num, dblink_linked_recid
            from db_link
            where dblink_fdbcont_zdb_id = '%s'
              and (dblink_linked_recid like 'ZDB-GENE%%' or dblink_linked_recid like '%%RNAG%%')
            """, fdcontGenPept);

        NativeQuery<Tuple> query = currentSession().createNativeQuery(sqlAllGenPeptWithGeneZFIN, Tuple.class);
        List<Tuple> results = query.list();

        for (Tuple row : results) {
            String genPeptAcc = row.get(0, String.class);
            String geneZdbId = row.get(1, String.class);

            if (genPeptWithMultipleZDBgene.containsKey(genPeptAcc) ||
                (allGenPeptWithGeneZFIN.containsKey(genPeptAcc) && !allGenPeptWithGeneZFIN.get(genPeptAcc).equals(geneZdbId))) {

                genPeptWithMultipleZDBgene.computeIfAbsent(genPeptAcc, k -> {
                    List<String> list = new ArrayList<>();
                    // Add the first one encountered from allGenPeptWithGeneZFIN if this is the first time we detect multiple
                    if (allGenPeptWithGeneZFIN.containsKey(k) && !list.contains(allGenPeptWithGeneZFIN.get(k))) {
                        list.add(allGenPeptWithGeneZFIN.get(k));
                    }
                    return list;
                });
                // Add the current geneZdbId if not already present (from a previous iteration for the same acc)
                if (!genPeptWithMultipleZDBgene.get(genPeptAcc).contains(geneZdbId)) {
                    genPeptWithMultipleZDBgene.get(genPeptAcc).add(geneZdbId);
                }
            }
            allGenPeptWithGeneZFIN.put(genPeptAcc, geneZdbId); // Stores the last gene encountered for an acc
        }

        long ctAllGenPeptWithGeneZFIN = allGenPeptWithGeneZFIN.size();
        long ctGenPeptWithMultipleZDBgene = genPeptWithMultipleZDBgene.size();

        print(LOG, "\nctAllGenPeptWithGeneZFIN = " + ctAllGenPeptWithGeneZFIN + "\n\n");
        print(LOG, "\nctGenPeptWithMultipleZDBgene = " + ctGenPeptWithMultipleZDBgene + "\n\n");

        print(LOG, "-----The GenBank accessions to be loaded but also associated with multiple ZFIN genes----\n\n");
        print(LOG, "GenPept \t mapped gene \tall associated genes\n");
        print(LOG, "--------\t-------------\t-------------\n");

        long ctGenPeptWithMultipleZDBgeneToLoad = 0;
        for (String genPept : genPeptWithMultipleZDBgene.keySet().stream().sorted().collect(Collectors.toList())) {
            if (GenPeptsToLoad.containsKey(genPept)) { // GenPeptsToLoad is Acc -> ZFIN Gene ID
                List<String> associatedGenes = genPeptWithMultipleZDBgene.get(genPept);
                print(LOG, String.format("%s\t%s\t%s\n",
                        genPept, GenPeptsToLoad.get(genPept), String.join(" ", associatedGenes)));
                ctGenPeptWithMultipleZDBgeneToLoad++;
            }
        }
        print(LOG, "-----------------------------------------\nTotal: " + ctGenPeptWithMultipleZDBgeneToLoad + "\n\n\n");
        print(STATS_PRIORITY2, "\nBefore the load, the total number of GenPept accessions associated with multiple ZFIN genes: " + ctGenPeptWithMultipleZDBgeneToLoad + "\n\n");
    }

    private void writeGenBankDNAaccessionsWithMappedGenesToLoad() {
        try {
            List<String> sortedGenBankDNAKeys = GenBankDNAncbiGeneIds.keySet().stream().sorted().collect(Collectors.toList());

            for (String genBankDNA : sortedGenBankDNAKeys) {
                List<String> multipleNCBIgeneIds = GenBankDNAncbiGeneIds.get(genBankDNA);

                if (envTrue("DEBUG_BROKEN_LOGIC_7925")) {
                    print(LOG, "DEBUG_BROKEN_LOGIC_7925\n");
                    if (multipleNCBIgeneIds != null && !multipleNCBIgeneIds.isEmpty()) {
                        String tmpNcbiGeneId = multipleNCBIgeneIds.get(multipleNCBIgeneIds.size() - 1);
                        multipleNCBIgeneIds = List.of(tmpNcbiGeneId); // Effectively process only the last one
                    }
                }
                print(LOG, "DEBUG: " + (multipleNCBIgeneIds != null ? multipleNCBIgeneIds.size() : 0) + " NCBI Gene IDs for " + genBankDNA + ":");

                if (multipleNCBIgeneIds != null) {
                    for (String ncbiGeneId : multipleNCBIgeneIds) {
                        print(LOG, " " + ncbiGeneId);
                        String[] zdbGeneAndPub = getZdbGeneIdAndAttributionByNCBIgeneId(ncbiGeneId);
                        String zdbGeneId = zdbGeneAndPub[0];
                        String attributionPub = zdbGeneAndPub[1];

                        if (zdbGeneId == null) continue;

                        String hashKey = zdbGeneId + genBankDNA + fdcontGenBankDNA;
                        Integer lengthVal = sequenceLength.get(genBankDNA);
                        String lengthStr = (lengthVal != null) ? lengthVal.toString() : "";

                        if (!geneAccFdbcont.containsKey(hashKey)) {
                            TOLOAD.write(String.format("%s|%s||%s|%s|%s\n",
                                    zdbGeneId, genBankDNA, lengthStr, fdcontGenBankDNA, attributionPub));
                            geneAccFdbcont.put(hashKey, "1"); // Mark as added
                            this.ctToLoad++;
                        } else {
                            // In Perl, this existing dblink_zdb_id is retrieved from geneAccFdbcont
                            String dbLinkToPreserve = geneAccFdbcont.get(hashKey);
                            TO_PRESERVE.write(dbLinkToPreserve + "\n");
                            print(LOG, "_DUPE<" + dbLinkToPreserve + ">");
                        }
                    }
                }
                print(LOG, "\n");
            }
        } catch (IOException e) {
            reportErrAndExit("Error writing GenBank DNA accessions to toLoad.unl: " + e.getMessage());
        }
    }

    private void writeRefSeqRNAaccessionsWithMappedGenesToLoad() {
        try {
            List<String> sortedRefSeqRNAKeys = RefSeqRNAncbiGeneIds.keySet().stream().sorted().collect(Collectors.toList());

            for (String refSeqRNA : sortedRefSeqRNAKeys) {
                String ncbiGeneId = RefSeqRNAncbiGeneIds.get(refSeqRNA);
                String[] zdbGeneAndPub = getZdbGeneIdAndAttributionByNCBIgeneId(ncbiGeneId);
                String zdbGeneId = zdbGeneAndPub[0];
                String attributionPub = zdbGeneAndPub[1];

                if (zdbGeneId == null) continue;

                String hashKey = zdbGeneId + refSeqRNA + fdcontRefSeqRNA;
                if (geneAccFdbcont.containsKey(hashKey)) continue;

                Integer lengthVal = sequenceLength.get(refSeqRNA);
                String lengthStr = (lengthVal != null) ? lengthVal.toString() : "";

                TOLOAD.write(String.format("%s|%s||%s|%s|%s\n",
                        zdbGeneId, refSeqRNA, lengthStr, fdcontRefSeqRNA, attributionPub));
                geneAccFdbcont.put(hashKey, "1");
                this.ctToLoad++;
            }
        } catch (IOException e) {
            reportErrAndExit("Error writing RefSeq RNA accessions to toLoad.unl: " + e.getMessage());
        }
    }

    private void writeRefPeptAccessionsWithMappedGenesToLoad() {
        try {
            List<String> sortedRefPeptKeys = RefPeptNCBIgeneIds.keySet().stream().sorted().collect(Collectors.toList());

            for (String refPept : sortedRefPeptKeys) {
                String ncbiGeneId = RefPeptNCBIgeneIds.get(refPept);
                String[] zdbGeneAndPub = getZdbGeneIdAndAttributionByNCBIgeneId(ncbiGeneId);
                String zdbGeneId = zdbGeneAndPub[0];
                String attributionPub = zdbGeneAndPub[1];

                if (zdbGeneId == null) continue;

                String hashKey = zdbGeneId + refPept + fdcontRefPept;
                if (geneAccFdbcont.containsKey(hashKey)) continue;

                Integer lengthVal = sequenceLength.get(refPept);
                String lengthStr = (lengthVal != null) ? lengthVal.toString() : "";

                TOLOAD.write(String.format("%s|%s||%s|%s|%s\n",
                        zdbGeneId, refPept, lengthStr, fdcontRefPept, attributionPub));
                geneAccFdbcont.put(hashKey, "1");
                this.ctToLoad++;
            }
        } catch (IOException e) {
            reportErrAndExit("Error writing RefPept accessions to toLoad.unl: " + e.getMessage());
        }
    }

    private void writeRefSeqDNAaccessionsWithMappedGenesToLoad() {
        try {
            List<String> sortedRefSeqDNAKeys = RefSeqDNAncbiGeneIds.keySet().stream().sorted().collect(Collectors.toList());

            for (String refSeqDNA : sortedRefSeqDNAKeys) {
                String ncbiGeneId = RefSeqDNAncbiGeneIds.get(refSeqDNA);
                String[] zdbGeneAndPub = getZdbGeneIdAndAttributionByNCBIgeneId(ncbiGeneId);
                String zdbGeneId = zdbGeneAndPub[0];
                String attributionPub = zdbGeneAndPub[1];

                if (zdbGeneId == null) {
                    continue; // Skip if no ZFIN gene ID found
                }

                String hashKey = zdbGeneId + refSeqDNA + fdcontRefSeqDNA;
                if (geneAccFdbcont.containsKey(hashKey)) {
                    continue; // Skip if already in the hash (i.e., exists in DB and not in toDelete)
                }

                Integer lengthVal = sequenceLength.get(refSeqDNA);
                String lengthStr = (lengthVal != null) ? lengthVal.toString() : "";

                // Format: zdbGeneId|RefSeqDNA||length|fdcontRefSeqDNA|attributionPub
                TOLOAD.write(String.format("%s|%s||%s|%s|%s\n",
                        zdbGeneId, refSeqDNA, lengthStr, fdcontRefSeqDNA, attributionPub));
                geneAccFdbcont.put(hashKey, "1"); // Mark as added to prevent re-adding in this run for other types if logic allows
                this.ctToLoad++;
            }
        } catch (IOException e) {
            reportErrAndExit("Error writing RefSeq DNA accessions to toLoad.unl: " + e.getMessage());
        }
    }

    private void closeUnloadFiles() {
        try {
            if (TOLOAD != null) {
                TOLOAD.close();
            }
            if (TO_PRESERVE != null) {
                TO_PRESERVE.close();
            }
            // Sort toPreserve.unl
            // Ensure the file exists before trying to sort it.
            File toPreserveFile = new File(workingDir, "toPreserve.unl");
            if (toPreserveFile.exists() && toPreserveFile.length() > 0) {
                doSystemCommand(List.of("sort", "--unique", "-o", toPreserveFile.getAbsolutePath(), toPreserveFile.getAbsolutePath()),
                        "sort_toPreserve_out.log", "sort_toPreserve_err.log");
            } else {
                print(LOG, "toPreserve.unl is empty or does not exist. Skipping sort.\n");
            }
        } catch (IOException e) {
            // This is a simplified error handling. Consider if a more specific action is needed.
            reportErrAndExit("Error closing unload files: " + e.getMessage());
        }
    }

    private void printStatsBeforeDelete() {
        outputDate(); // Corresponds to system("/bin/date");
        print(LOG, "Done everything before doing the deleting and inserting\n");
        String statsMessage = String.format("\n%d total number of db_link records are dropped.\n%d total number of new records are added.\n\n",
                this.ctToDelete, this.ctToLoad);
        print(LOG, statsMessage);
        print(STATS_PRIORITY2, statsMessage);
    }

    private void executeDeleteAndLoadSQLFile() {
        File toLoadFile = new File(workingDir, "toLoad.unl");
        if (!toLoadFile.exists() || this.ctToLoad == 0) {
            String message = "\nMissing the add list, toLoad.unl, or it is empty. Something is wrong!\n\n";
            print(LOG, message);
            if (STATS_PRIORITY2 != null) {
                try { STATS_PRIORITY2.close(); } catch (IOException e) { /* ignore */ }
            }
            String subjectLine = "Auto from " + instance + ": NCBI_gene_load.pl :: missing or empty add list, toLoad.unl";
            reportErrAndExit(subjectLine);
            return; // Should be unreachable due to reportErrAndExit
        }

        String sqlFile = "loadNCBIgeneAccs.sql";
        File fullPathToSqlFile = new File(SOURCEROOT.value(), "server_apps/data_transfer/NCBIGENE/" + sqlFile);

        if (!fullPathToSqlFile.exists()) {
            reportErrAndExit("SQL file not found: " + fullPathToSqlFile.getAbsolutePath());
            return;
        }

        try {
            HibernateUtil.createTransaction(); // Assuming the SQL script manages its own transactionality or this is appropriate
            doSystemCommand(
                    List.of(
                            "psql", "--echo-all", "-v", "ON_ERROR_STOP=1",
                            "-U", env("PGUSER"),
                            "-h", env("PGHOST"),
                            "-d", env("DB_NAME"),
                            "-a", "-f", fullPathToSqlFile.getAbsolutePath()
                    ), "loadLog1.sql", "loadLog2.sql"
            );
            HibernateUtil.flushAndCommitCurrentSession();
        } catch (Exception e) { // Catch general exception from doSystemCommand
            HibernateUtil.rollbackTransaction();
            // Perl script explicitly calls sendLoadLogs() on failure of this specific command.
            sendLoadLogs(); // Send logs before exiting
            reportErrAndExit("Auto from " + instance + ": NCBI_gene_load.pl :: failed at loadNCBIgeneAccs.sql");
        }
        print(LOG, "\nDone with the deletion and loading!\n\n");
    }

    private void executeMarkerAssemblyUpdate() {
        String sqlFile = "markerAssemblyUpdate.sql";
        File fullPathToSqlFile = new File(SOURCEROOT.value(), "server_apps/data_transfer/NCBIGENE/" + sqlFile);

        if (!fullPathToSqlFile.exists()) {
            reportErrAndExit("SQL file not found: " + fullPathToSqlFile.getAbsolutePath());
            return;
        }

        try {
            HibernateUtil.createTransaction(); // Assuming the SQL script manages its own transactionality or this is appropriate
            doSystemCommand(
                    List.of(
                            "psql", "--echo-all", "-v", "ON_ERROR_STOP=1",
                            "-U", env("PGUSER"),
                            "-h", env("PGHOST"),
                            "-d", env("DB_NAME"),
                            "-a", "-f", fullPathToSqlFile.getAbsolutePath()
                    ), "loadLogMarkerAssemblyUpdate.txt", "loadLogMarkerAssemblyUpdateErr.txt"
            );
            HibernateUtil.flushAndCommitCurrentSession();
        } catch (Exception e) { // Catch general exception from doSystemCommand
            HibernateUtil.rollbackTransaction();
            // Perl script explicitly calls sendLoadLogs() on failure of this specific command.
            sendLoadLogs(); // Send logs before exiting
            reportErrAndExit("Auto from " + instance + ": NCBI_gene_load.pl :: failed at markerAssemblyUpdate.sql");
        }
        print(LOG, "\nDone with the update of marker-assembly association!\n\n");
    }

    private void sendLoadLogs() {
        String subject = "Auto from " + instance + ": NCBI_gene_load.pl :: loadLog1 file";
        // The Perl script attached "loadLog1". We might have "loadLog1.sql" and "loadLog2.sql".
        // Let's decide which one to send or if both. The original was just one.
        // If loadNCBIgeneAccs.sql produces loadLog1.sql and loadLog2.sql:
        sendMailWithAttachedReport(env("SWISSPROT_EMAIL_ERR"),subject,"loadLog1.sql", workingDir);
        // If there's another loadLog1 from a different context, that needs to be clarified.
        // The Perl script seems to imply a generic "loadLog1" that might be overwritten.
        // For now, sending the one from loadNCBIgeneAccs.sql.
    }

    private void reportAllLoadStatistics() {
        // ----- AFTER THE LOAD, get all the Genpept accessions associated with gene at ZFIN, and those with multiple ZFIN genes ---------
        String sqlAllGenPeptWithGeneAfterLoad = String.format("""
            select dblink_acc_num, dblink_linked_recid
            from db_link
            where dblink_fdbcont_zdb_id = '%s'
              and (dblink_linked_recid like 'ZDB-GENE%%' or dblink_linked_recid like '%%RNAG%%')
            """, fdcontGenPept);

        NativeQuery<Tuple> queryAfterLoad = currentSession().createNativeQuery(sqlAllGenPeptWithGeneAfterLoad, Tuple.class);
        List<Tuple> resultsAfterLoad = queryAfterLoad.list();

        allGenPeptWithGeneAfterLoad = new HashMap<>();
        GenPeptWithMultipleZDBgeneAfterLoad = new HashMap<>();

        for (Tuple row : resultsAfterLoad) {
            String genPeptAcc = row.get(0, String.class);
            String geneZdbId = row.get(1, String.class);

            if (GenPeptWithMultipleZDBgeneAfterLoad.containsKey(genPeptAcc) ||
                (allGenPeptWithGeneAfterLoad.containsKey(genPeptAcc) && !allGenPeptWithGeneAfterLoad.get(genPeptAcc).equals(geneZdbId))) {
                GenPeptWithMultipleZDBgeneAfterLoad.computeIfAbsent(genPeptAcc, k -> {
                    List<String> list = new ArrayList<>();
                    if (allGenPeptWithGeneAfterLoad.containsKey(k) && !list.contains(allGenPeptWithGeneAfterLoad.get(k))) {
                        list.add(allGenPeptWithGeneAfterLoad.get(k));
                    }
                    return list;
                });
                if (!GenPeptWithMultipleZDBgeneAfterLoad.get(genPeptAcc).contains(geneZdbId)) {
                    GenPeptWithMultipleZDBgeneAfterLoad.get(genPeptAcc).add(geneZdbId);
                }
            }
            allGenPeptWithGeneAfterLoad.put(genPeptAcc, geneZdbId);
        }

        long ctAllGenPeptWithGeneZFINafterLoad = allGenPeptWithGeneAfterLoad.size();
        long ctGenPeptWithMultipleZDBgeneAfterLoad = GenPeptWithMultipleZDBgeneAfterLoad.size();

        print(LOG, "\nctAllGenPeptWithGeneZFINafterLoad = " + ctAllGenPeptWithGeneZFINafterLoad + "\n\n");
        print(LOG, "\nctGenPeptWithMultipleZDBgeneAfterLoad (count of accs) = " + ctGenPeptWithMultipleZDBgeneAfterLoad + "\n\n");

        print(STATS_PRIORITY2, "----- After the load, the GenBank accessions associated with multiple ZFIN genes----\n\n");
        print(STATS_PRIORITY2, "GenPept \t mapped gene \tall associated genes\n");
        print(STATS_PRIORITY2, "--------\t-------------\t-------------\n");

        // Re-calculate the count of lines printed for multiple ZDB gene after load
        long actualCtGenPeptWithMultipleZDBgeneAfterLoadPrinted = 0;
        for (String genPept : GenPeptWithMultipleZDBgeneAfterLoad.keySet().stream().sorted().collect(Collectors.toList())) {
            List<String> associatedGenes = GenPeptWithMultipleZDBgeneAfterLoad.get(genPept);
            String mappedGeneForPept = GenPeptsToLoad.getOrDefault(genPept, "[not in GenPeptsToLoad hash]");
            if ("[not in GenPeptsToLoad hash]".equals(mappedGeneForPept)) {
                print(LOG,"ERROR - GenPept " + genPept + " not in GenPeptsToLoad hash (during after-load report)\n");
            }
            print(STATS_PRIORITY2, String.format("%s\t%s\t%s\n",
                    genPept, mappedGeneForPept, String.join(" ", associatedGenes)));
            actualCtGenPeptWithMultipleZDBgeneAfterLoadPrinted++;
        }
        print(STATS_PRIORITY2, "-----------------------------------------\nTotal: " + actualCtGenPeptWithMultipleZDBgeneAfterLoadPrinted + "\n\n\n");
        print(LOG, "\nctGenPeptWithMultipleZDBgeneAfterLoad (printed lines) = " + actualCtGenPeptWithMultipleZDBgeneAfterLoadPrinted + "\n\n");


        // Record counts after the load
        String sqlGenesWithRefSeqAfter = String.format("""
            select mrkr_zdb_id, mrkr_abbrev from marker
            where (mrkr_zdb_id like 'ZDB-GENE%%' or mrkr_zdb_id like '%%RNAG%%')
              and exists (select 1 from db_link
                          where dblink_linked_recid = mrkr_zdb_id
                            and dblink_fdbcont_zdb_id in ('%s','%s','%s'))
            """, fdcontRefSeqRNA, fdcontRefPept, fdcontRefSeqDNA);

        NativeQuery<Tuple> queryGenesAfter = currentSession().createNativeQuery(sqlGenesWithRefSeqAfter, Tuple.class);
        List<Tuple> resultsGenesAfter = queryGenesAfter.list();
        genesWithRefSeqAfterLoad = new HashMap<>();
        for (Tuple row : resultsGenesAfter) {
            genesWithRefSeqAfterLoad.put(row.get(0, String.class), row.get(1, String.class));
        }
        ctGenesWithRefSeqAfter = genesWithRefSeqAfterLoad.size();

        // Individual counts
        String tempSql;
        tempSql = getSqlForGeneAndRnagDbLinksFromFdbContId(fdcontNCBIgeneId);
        numNCBIgeneIdAfter = PortSqlHelper.countData(currentSession(), tempSql);

        tempSql = getSqlForGeneAndRnagDbLinksFromFdbContId(fdcontRefSeqRNA);
        numRefSeqRNAAfter = PortSqlHelper.countData(currentSession(), tempSql);

        tempSql = getSqlForGeneAndRnagDbLinksFromFdbContId(fdcontRefPept);
        numRefPeptAfter = PortSqlHelper.countData(currentSession(), tempSql);

        tempSql = getSqlForGeneAndRnagDbLinksFromFdbContId(fdcontRefSeqDNA);
        numRefSeqDNAAfter = PortSqlHelper.countData(currentSession(), tempSql);

        tempSql = PortSqlHelper.getSqlForGeneAndRnagDbLinksSupportedByLoadPubsFromFdbContId(fdcontGenBankRNA, pubMappedbasedOnRNA, pubMappedbasedOnVega, pubMappedbasedOnNCBISupplement);
        numGenBankRNAAfter = PortSqlHelper.countData(currentSession(), tempSql);

        tempSql = PortSqlHelper.getSqlForGeneAndRnagDbLinksSupportedByLoadPubsFromFdbContId(fdcontGenPept, pubMappedbasedOnRNA, pubMappedbasedOnVega, pubMappedbasedOnNCBISupplement);
        numGenPeptAfter = PortSqlHelper.countData(currentSession(), tempSql);

        tempSql = PortSqlHelper.getSqlForGeneAndRnagDbLinksSupportedByLoadPubsFromFdbContId(fdcontGenBankDNA, pubMappedbasedOnRNA, pubMappedbasedOnVega, pubMappedbasedOnNCBISupplement);
        numGenBankDNAAfter = PortSqlHelper.countData(currentSession(), tempSql);

        tempSql = String.format("select distinct dblink_linked_recid from db_link where dblink_fdbcont_zdb_id = '%s' and dblink_acc_num like 'NM_%%' and (dblink_linked_recid like 'ZDB-GENE%%' or dblink_linked_recid like '%%RNAG%%')", fdcontRefSeqRNA);
        numGenesRefSeqRNAAfter = PortSqlHelper.countData(currentSession(), tempSql);

        tempSql = String.format("select distinct dblink_linked_recid from db_link where dblink_fdbcont_zdb_id = '%s' and dblink_acc_num like 'NP_%%' and (dblink_linked_recid like 'ZDB-GENE%%' or dblink_linked_recid like '%%RNAG%%')", fdcontRefPept);
        numGenesRefSeqPeptAfter = PortSqlHelper.countData(currentSession(), tempSql);

        tempSql = "select distinct dblink_linked_recid from db_link, foreign_db_contains, foreign_db where dblink_fdbcont_zdb_id = fdbcont_zdb_id and fdbcont_fdb_db_id = fdb_db_pk_id and fdb_db_name = 'GenBank' and (dblink_linked_recid like 'ZDB-GENE%%' or dblink_linked_recid like '%RNAG%')";
        numGenesGenBankAfter = PortSqlHelper.countData(currentSession(), tempSql);

        // Print statistics to STATS_PRIORITY1
        print(STATS_PRIORITY1, "\n********* Percentage change of various categories of records *************\n\n");
        print(STATS_PRIORITY1, "number of db_link records with gene     \tbefore load\tafter load\tpercentage change\n");
        print(STATS_PRIORITY1, "----------------------------------------\t-----------\t-----------\t-------------------------\n");

        BiConsumer<String, Number[]> printStatLine = (label, values) -> {
            String line = String.format("%-40s\t%-11s\t%-11s\t", label, values[0], values[1]);
            if (values[0].doubleValue() > 0) {
                double change = (values[1].doubleValue() - values[0].doubleValue()) / values[0].doubleValue() * 100;
                line += String.format("%.2f", change);
            }
            print(STATS_PRIORITY1, line + "\n");
        };

        printStatLine.accept("NCBI gene Id", new Number[]{numNCBIgeneIdBefore, numNCBIgeneIdAfter});
        printStatLine.accept("RefSeq RNA", new Number[]{numRefSeqRNABefore, numRefSeqRNAAfter});
        printStatLine.accept("RefPept", new Number[]{numRefPeptBefore, numRefPeptAfter});
        printStatLine.accept("RefSeq DNA", new Number[]{numRefSeqDNABefore, numRefSeqDNAAfter});
        printStatLine.accept("GenBank RNA", new Number[]{numGenBankRNABefore, numGenBankRNAAfter});
        printStatLine.accept("GenPept", new Number[]{numGenPeptBefore, numGenPeptAfter});
        printStatLine.accept("GenBank DNA", new Number[]{numGenBankDNABefore, numGenBankDNAAfter});

        print(STATS_PRIORITY1, "\n\n");
        print(STATS_PRIORITY1, "number of genes                              \tbefore load\tafter load\tpercentage change\n");
        print(STATS_PRIORITY1, "----------------------------------------\t-----------\t-----------\t-------------------------\n");

        printStatLine.accept("with RefSeq", new Number[]{ctGenesWithRefSeqBefore, ctGenesWithRefSeqAfter});
        printStatLine.accept("with RefSeq NM", new Number[]{numGenesRefSeqRNABefore, numGenesRefSeqRNAAfter});
        printStatLine.accept("with RefSeq NP", new Number[]{numGenesRefSeqPeptBefore, numGenesRefSeqPeptAfter});
        printStatLine.accept("with GenBank", new Number[]{numGenesGenBankBefore, numGenesGenBankAfter});

        List<String> keysSortedByValues = geneZDBidsSymbols.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(String.CASE_INSENSITIVE_ORDER))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        print(STATS_PRIORITY1, "\n\nList of genes used to have RefSeq acc but no longer having any:\n");
        print(STATS_PRIORITY1, "-------------------------------------------------------------------\n");
        long ctGenesLostRefSeq = 0;
        for (String zdbGeneId : keysSortedByValues) {
            if (genesWithRefSeqBeforeLoad.containsKey(zdbGeneId) && !genesWithRefSeqAfterLoad.containsKey(zdbGeneId)) {
                ctGenesLostRefSeq++;
                print(STATS_PRIORITY1, zdbGeneId + "\n"); // Symbol will be added later
            }
        }
        print(STATS_PRIORITY1, "\ntotal: " + ctGenesLostRefSeq + "\n\n");

        print(STATS_PRIORITY1, "\n\nList of genes now having RefSeq acc but used to have none ReSeq:\n");
        print(STATS_PRIORITY1, "-------------------------------------------------------------------\n");
        long ctGenesGainRefSeq = 0;
        for (String zdbGeneId : keysSortedByValues) {
            if (genesWithRefSeqAfterLoad.containsKey(zdbGeneId) && !genesWithRefSeqBeforeLoad.containsKey(zdbGeneId)) {
                ctGenesGainRefSeq++;
                print(STATS_PRIORITY1, zdbGeneId + "\n"); // Symbol will be added later
            }
        }
        print(STATS_PRIORITY1, "\ntotal: " + ctGenesGainRefSeq + "\n\n\n");

        // Close STATS_PRIORITY1 and STATS_PRIORITY2 before combining
        try {
            if (STATS_PRIORITY1 != null) STATS_PRIORITY1.close();
            if (STATS_PRIORITY2 != null) STATS_PRIORITY2.close();
        } catch (IOException e) { reportErrAndExit("Error closing STATS_PRIORITY files: " + e.getMessage()); }


        // Combine into STATS
        StringBuilder outputBuffer = new StringBuilder();
        try {
            outputBuffer.append(FileUtils.readFileToString(new File(workingDir, "reportStatistics_p1"), StandardCharsets.UTF_8));
            outputBuffer.append(FileUtils.readFileToString(new File(workingDir, "reportStatistics_p2"), StandardCharsets.UTF_8));
        } catch (IOException e) { reportErrAndExit("Error reading reportStatistics_p1 or _p2: " + e.getMessage());}

        String finalReportContent = outputBuffer.toString();
        // Replace ZDB IDs with ID(Symbol)
        for (String zdbGeneId : keysSortedByValues) {
            String symbol = geneZDBidsSymbols.get(zdbGeneId);
            // Regex to match ZDB ID not followed by a digit (to avoid altering parts of other ZDB IDs)
            // and ensure it's not already in ZDBID(SYMBOL) format
            String regex = "\\b" + Pattern.quote(zdbGeneId) + "\\b(?!\\s*\\()";
            String replacement = Matcher.quoteReplacement(zdbGeneId + "(" + symbol + ")");
            try {
                finalReportContent = finalReportContent.replaceAll(regex, replacement);
            } catch (Exception e) {
                print(LOG, "WARN: Regex replacement failed for " + zdbGeneId + ": " + e.getMessage() + "\n");
            }
        }

        print(STATS, getArtifactComparisonURLs());
        print(STATS, finalReportContent);

        writeHtmlReport();


        // Delete the two files
        new File(workingDir, "reportStatistics_p1").delete();
        new File(workingDir, "reportStatistics_p2").delete();
    }

    public void writeHtmlReport() {
        //break down changes into subsets using the provided before and after files
        String outputPrefix = new File(workingDir, "ncbi_compare_").toString();
        CSVDiff diff = new CSVDiff(outputPrefix,
                new String[]{"dblink_linked_recid", "dblink_acc_num", "dblink_fdbcont_zdb_id"},
                new String[]{"dblink_info", "dblink_zdb_id"});
        CSVRecord beforeAfterSummary = null;
        Map<String, List<CSVRecord>> beforeAfterComparison = null;
        try {
            beforeAfterComparison = diff.processToMap(beforeFile.getAbsolutePath(), afterFile.getAbsolutePath());
            System.out.println("Generated before-after comparison with " + beforeAfterComparison.size() + " categories.");
            System.out.println(beforeAfterComparison.keySet().stream().sorted().collect(Collectors.joining("; ")));
            List<File> csvs = diff.writeMapToCSVs(workingDir, "before_after_cmp_", beforeAfterComparison);
            System.out.println("Generated " + csvs.size() + " CSV files for before-after comparison.");
            List<CSVRecord> beforeAfterSummaryList = beforeAfterComparison.get("summary");
            beforeAfterSummary = beforeAfterSummaryList.remove(0);
            combineCsvsToExcelReport(csvs);
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
            print(LOG, "ERROR: Can't create before after comparison\n");
        }

        NCBIReportBuilder builder = new NCBIReportBuilder();
        builder.setInstance(ZfinPropertiesEnum.INSTANCE.toString());

        NCBIReportBuilder.SummaryTableBuilder table = builder.addSummaryTable("number of db_link records with gene");
        table.setHeaders(List.of("Category", "Before Load", "After Load", "Percentage Change"));
        table.addBeforeAfterCountSummaryRow("NCBI gene Id", numNCBIgeneIdBefore, numNCBIgeneIdAfter);
        table.addBeforeAfterCountSummaryRow("RefSeq RNA", numRefSeqRNABefore, numRefSeqRNAAfter);
        table.addBeforeAfterCountSummaryRow("RefPept", numRefPeptBefore, numRefPeptAfter);
        table.addBeforeAfterCountSummaryRow("RefSeq DNA", numRefSeqDNABefore, numRefSeqDNAAfter);
        table.addBeforeAfterCountSummaryRow("GenBank RNA", numGenBankRNABefore, numGenBankRNAAfter);
        table.addBeforeAfterCountSummaryRow("GenPept", numGenPeptBefore, numGenPeptAfter);
        table.addBeforeAfterCountSummaryRow("GenBank DNA", numGenBankDNABefore, numGenBankDNAAfter);

        NCBIReportBuilder.SummaryTableBuilder table2 = builder.addSummaryTable("number of genes");
        table2.setHeaders(List.of("Category", "Before Load", "After Load", "Percentage Change"));
        table2.addBeforeAfterCountSummaryRow("with RefSeq", ctGenesWithRefSeqBefore, ctGenesWithRefSeqAfter);
        table2.addBeforeAfterCountSummaryRow("with RefSeq NM", numGenesRefSeqRNABefore, numGenesRefSeqRNAAfter);
        table2.addBeforeAfterCountSummaryRow("with RefSeq NP", numGenesRefSeqPeptBefore, numGenesRefSeqPeptAfter);
        table2.addBeforeAfterCountSummaryRow("with GenBank", numGenesGenBankBefore, numGenesGenBankAfter);

        if (beforeAfterSummary != null) {
            NCBIReportBuilder.SummaryTableBuilder table3 = builder.addSummaryTable("totals before and after load");
            table3.setHeaders(List.of("Category", "Count"));
            table3.addSummaryRow(List.of("Number of db_link records before load", beforeAfterSummary.get("beforeCount")));
            table3.addSummaryRow(List.of("Number of db_link records after load", beforeAfterSummary.get("afterCount")));
            table3.addSummaryRow(List.of("Number of db_link records unchanged", beforeAfterSummary.get("retainedCount")));
            table3.addSummaryRow(List.of("Number of db_link records changed superficially", beforeAfterSummary.get("ignoredCount1")));
            table3.addSummaryRow(List.of("Number of db_link records updated", beforeAfterSummary.get("updated1Count")));
            table3.addSummaryRow(List.of("Number of db_link records added", beforeAfterSummary.get("addedCount")));
            table3.addSummaryRow(List.of("Number of db_link records deleted", beforeAfterSummary.get("deletedCount")));
        }

        builder.addActions(createActions(beforeAfterComparison));
        builder.addActions(manyToManyWarningActions);
        builder.addActions(oneToManyWarningActions);
        builder.addActions(manyToOneWarningActions);

        ObjectNode report = builder.build();

        try {
            String jsonString = builder.getJsonString(report);

            // Write to file
            writeToFileOrZip(new File(workingDir, "ncbi_report.json.zip"), jsonString, "UTF-8");
            writeOutputReportFile(jsonString);

        } catch (IOException e) {
            print(LOG, "ERROR: JSON reporting failed: " + e.getMessage());
        }
        beforeAfterComparison.clear();
    }

    private List<LoadReportAction> postProcessActions(List<LoadReportAction> actions) {
        return actions.stream().map(this::modifyDeleteActionForNotInCurrentAnnotationRelease).toList();
    }

    private LoadReportAction modifyDeleteActionForNotInCurrentAnnotationRelease(LoadReportAction action) {
        if (!action.getType().equals(LoadReportAction.Type.DELETE)) {
            return action;
        }
        if (!action.getDbName().equals(DBName.NCBI.getDisplayName())) {
            return action;
        }

        if (this.geneIDsNotInCurrentAnnotationRelease == null) {
            System.out.println("Fetching gene IDs not in current annotation release...");
            this.fetchGeneIDsNotInCurrentAnnotationReleaseSet();
            System.out.println(String.join(",", this.geneIDsNotInCurrentAnnotationRelease));
        }

        String accession = action.getAccession();
        if (this.geneIDsNotInCurrentAnnotationRelease.contains(accession)) {
            action.setDetails("This NCBI Gene ID is not in the current annotation release.");
            action.addTag(new LoadReportActionTag("Not In Current Annotation Release", "This NCBI Gene ID is not in the current annotation release."));
        }

        return action;
    }

    private void appendToFile(File file, String line) {
        try {
            FileUtils.writeLines(file, Collections.singletonList(line), true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void fetchGeneIDsNotInCurrentAnnotationReleaseSet() {
        if (this.geneIDsNotInCurrentAnnotationRelease != null) {
            return;
        }
        geneIDsNotInCurrentAnnotationRelease = new ArrayList<>();
        File cachedListOfNCBIGeneIDs = new File(workingDir, "notInCurrentReleaseGeneIDs.unl");
        if (envTrue("SKIP_DOWNLOADS")) {
            if (cachedListOfNCBIGeneIDs.exists()) {
                try {
                    System.out.println("Reading cached gene IDs from: " + cachedListOfNCBIGeneIDs.getAbsolutePath());
                    geneIDsNotInCurrentAnnotationRelease = FileUtils.readLines(cachedListOfNCBIGeneIDs, StandardCharsets.UTF_8);
                } catch (IOException e) {
                    System.out.println("ERROR: SKIP_DOWNLOADS set, yet failed to read cached gene IDs: " + e.getMessage());
                    print(LOG, "ERROR: SKIP_DOWNLOADS set, yet failed to read cached gene IDs: " + e.getMessage());
                    List<String> fetchedIDs = NCBIEfetch.fetchGeneIDsNotInCurrentAnnotationReleaseSet();
                    print(LOG, "Fetched " + fetchedIDs.size() + " gene IDs from NCBI: \n" + String.join("\n", fetchedIDs));
                    geneIDsNotInCurrentAnnotationRelease = fetchedIDs;
                }
            } else {
                System.out.println("WARNING: Cached gene IDs file not found. Will skip fetch from NCBI as SKIP_DOWNLOADS is set.");
                System.out.println(cachedListOfNCBIGeneIDs.getAbsolutePath());
                print(LOG, "WARNING: Cached gene IDs file not found. Will skip fetch from NCBI as SKIP_DOWNLOADS is set.");
            }
        } else {
            geneIDsNotInCurrentAnnotationRelease = NCBIEfetch.fetchGeneIDsNotInCurrentAnnotationReleaseSet();
        }

        System.out.println("Total of " + geneIDsNotInCurrentAnnotationRelease.size() + " gene IDs not in current annotation release.");

        try {
            if (geneIDsNotInCurrentAnnotationRelease.size() > 0) {
                //write the list of gene IDs not in current annotation release to a file
                FileUtils.writeLines(cachedListOfNCBIGeneIDs, geneIDsNotInCurrentAnnotationRelease);
            }
        } catch (IOException e) {
            //ignore (just caching)
        }
    }

    private List<LoadReportAction> createActions(Map<String, List<CSVRecord>> beforeAfterComparison) {
        List<LoadReportAction> actions = new ArrayList<>();

        actions.addAll(createDeleteActions(beforeAfterComparison));
        actions.addAll(createLoadActions(beforeAfterComparison));
        actions.addAll(createUpdateActions(beforeAfterComparison));

        return postProcessActions(actions);
    }

    private List<LoadReportAction> createLoadActions(Map<String, List<CSVRecord>> beforeAfterComparison) {
        List<LoadReportAction> actions = new ArrayList<>();
        List<CSVRecord> addedRecords = beforeAfterComparison.getOrDefault("added", Collections.emptyList());
        for(CSVRecord record : addedRecords) {
            LoadReportAction action = csvRecordToAction(record, LoadReportAction.Type.LOAD);
            actions.add(action);
        }
        return actions;
    }

    private List<LoadReportAction> createDeleteActions(Map<String, List<CSVRecord>> beforeAfterComparison) {
        List<LoadReportAction> actions = new ArrayList<>();
        List<CSVRecord> deletedRecords = beforeAfterComparison.getOrDefault("deleted", Collections.emptyList());
        for(CSVRecord record : deletedRecords) {
            LoadReportAction action = csvRecordToAction(record, LoadReportAction.Type.DELETE);
            actions.add(action);
        }
        return actions;
    }

    private List<LoadReportAction> createUpdateActions(Map<String, List<CSVRecord>> beforeAfterComparison) {
        List<CSVRecord> updatedRecordsBefore = beforeAfterComparison.getOrDefault("updated1", Collections.emptyList());
        List<CSVRecord> updatedRecordsAfter = beforeAfterComparison.getOrDefault("updated2", Collections.emptyList());
        if (updatedRecordsBefore.isEmpty() && updatedRecordsAfter.isEmpty()) {
            return Collections.emptyList();
        }
        if (updatedRecordsBefore.size() != updatedRecordsAfter.size()) {
            print(LOG, "WARNING: Mismatched number of updated records before and after comparison.\n");
            throw new IllegalStateException("Mismatched number of updated records before and after comparison.");
        }
        Iterator<CSVRecord> iterator1 = updatedRecordsBefore.iterator();
        Iterator<CSVRecord> iterator2 = updatedRecordsAfter.iterator();
        List<LoadReportAction> actions = new ArrayList<>();
        while(iterator1.hasNext()) {
            CSVRecord record1 = iterator1.next();
            CSVRecord record2 = iterator2.next();
            assertRecordsMatch(record1, record2);
            LoadReportAction action = csvRecordToAction(record1, LoadReportAction.Type.UPDATE);
            String details = """
                    ZDB ID          : %s
                    Accession or ID : %s
                    DBLink ID       : %s -> %s
                    Length          : %s -> %s
                    Pub ZDB ID      : %s -> %s
                    """.formatted(
                    record1.get("dblink_linked_recid"),
                    record1.get("dblink_acc_num"),
                    record1.get("dblink_zdb_id"), record2.get("dblink_zdb_id"),
                    record1.get("dblink_length"), record2.get("dblink_length"),
                    record1.get("recattrib_source_zdb_id"), record2.get("recattrib_source_zdb_id"));
            action.setDetails(details);
            actions.add(action);
        }
        return actions;
    }

    private void assertRecordsMatch(CSVRecord record1, CSVRecord record2) {
        if (record1.get("dblink_linked_recid").equals(record2.get("dblink_linked_recid")) &&
            record1.get("dblink_acc_num").equals(record2.get("dblink_acc_num")) &&
            record1.get("dblink_fdbcont_zdb_id").equals(record2.get("dblink_fdbcont_zdb_id"))) {
            // Records match, proceed
        } else {
            print(LOG, "ERROR: Mismatched records in before-after comparison.\n");
            throw new IllegalStateException("Mismatched records in before-after comparison.");
        }
    }

    private LoadReportAction csvRecordToAction(CSVRecord record, LoadReportAction.Type type) {
        String zdbId = record.get("dblink_linked_recid");
        String accNum = record.get("dblink_acc_num");
        String fdbcontZdbId = record.get("dblink_fdbcont_zdb_id");

        LoadReportAction action = new LoadReportAction();
        action.setType(type);

        String dbName = DBName.getDisplayNameForForeignDB(fdbcontZdbId);
        action.setDbName(dbName);
        action.setRelatedEntityFields(Map.of("Database", dbName, "Pub", record.get("recattrib_source_zdb_id")));

        String subType = switch(type) {
            case DELETE -> "Lost ";
            case LOAD -> "New ";
            case UPDATE -> "Updated ";
            default -> "Unknown ";
        };
        String dbLinkType = switch(fdbcontZdbId) {
            case fdcontNCBIgeneId -> "GeneID";
            default -> "Accession";
        };

        action.setSubType(subType + dbName + " " + dbLinkType);
        action.setAccession(accNum);
        action.setGeneZdbID(zdbId);
        action.setId(accNum + zdbId + fdbcontZdbId);
        String details = """
        %s: %s
        Gene ZDB ID: %s
        Database: %s (%s)
        """.formatted(dbLinkType, accNum, zdbId, dbName, fdbcontZdbId);
        action.setDetails(details);
        action.setSupplementalDataKeys(Collections.emptyList());
        action.addZdbIdLink(zdbId);
        action.addNcbiGeneIdLink(accNum);
        action.setRelatedActionsKeys(List.of(zdbId));
        action.addLink(new LoadReportActionLink(record.get("recattrib_source_zdb_id"),
                "https://zfin.org/" + record.get("recattrib_source_zdb_id")));
        return action;
    }

    private void combineCsvsToExcelReport(List<File> csvs) {
        CSVToXLSXConverter converter = new CSVToXLSXConverter();
        List<String> sheetNames = csvs.stream().map(File::getName)
                .map(name -> name.replace(".csv", ""))
                .map(name -> name.replace("before_after_cmp_", ""))
                .collect(Collectors.toList());
        converter.run(new File(workingDir, "before_after.xlsx"), csvs, sheetNames, true);
        print(LOG, "Combined CSVs into Excel report: before_after_cmp.xlsx\n");
    }

    private void writeOutputReportFile(String jsonString) {
        String sourceRoot = ZfinPropertiesEnum.SOURCEROOT.value();
        if (sourceRoot == null) {
            sourceRoot = System.getenv("SOURCEROOT");
        }
        File reportFile = new File(workingDir, "ncbi_report.html");
        try {
            String template = sourceRoot + "/home/uniprot/zfin-report-template.html";
            String templateContents = FileUtils.readFileToString(new File(template));
            String filledTemplate = templateContents.replace(JSON_PLACEHOLDER_IN_TEMPLATE, jsonString);
            FileUtils.writeStringToFile(reportFile, filledTemplate);
            //check report file size and compress if too big
            if (reportFile.length() > MAX_REPORT_FILE_SIZE) {
                print(LOG, "Report file size is " + reportFile.length() + " bytes, compressing into zip file.\n");
                writeToFileOrZip(new File(workingDir, "ncbi_report.html.zip"), filledTemplate, "UTF-8");
                if (!reportFile.delete()) {
                    print(LOG, "WARNING: Failed to delete large report file after zipping: " + reportFile.getAbsolutePath() + "\n");
                }
            }
        } catch (IOException e) {
            print(LOG, "ERROR: Could not write report file: " + e.getMessage());
        }
    }

    private void captureAfterState() {
        afterFile = new File(workingDir, "after_load.csv");
        captureState(afterFile);
    }

    private void emailLoadReports() {
        String subjectPrefix = "Auto from " + instance + ": NCBI_gene_load.pl :: ";
        sendMailWithAttachedReport(env("SWISSPROT_EMAIL_REPORT"), subjectPrefix + "Statistics", "reportStatistics", workingDir);
        sendMailWithAttachedReport(env("SWISSPROT_EMAIL_ERR"), subjectPrefix + "log file", "logNCBIgeneLoad", workingDir);
    }

    private String[] getZdbGeneIdAndAttributionByNCBIgeneId(String ncbiGeneId) {
        String zdbGeneId = null;
        String attributionPub = null;

        if (mappedReversed.containsKey(ncbiGeneId)) {
            zdbGeneId = mappedReversed.get(ncbiGeneId);
            attributionPub = pubMappedbasedOnRNA;
        } else if (oneToOneViaVega.containsKey(ncbiGeneId)) {
            zdbGeneId = oneToOneViaVega.get(ncbiGeneId);
            attributionPub = pubMappedbasedOnVega;
        } else if (ncbiSupplementMapReversed != null && ncbiSupplementMapReversed.containsKey(ncbiGeneId)) {
            zdbGeneId = ncbiSupplementMapReversed.get(ncbiGeneId);
            attributionPub = pubMappedbasedOnNCBISupplement;
        }
        return new String[]{zdbGeneId, attributionPub};
    }

    private File findRefSeqCatalogFile() {

        File fullCatalog = new File(workingDir, "RefSeqCatalog.gz");
        String fullCatalogMd5 = null;
        if (fullCatalog.exists()) {
            fullCatalogMd5 = md5File(fullCatalog, LOG);

            //now find a danioFile that matches the md5 of the full catalog
            File[] danioFiles = workingDir.listFiles((dir, name) -> name.startsWith("RefSeqCatalog.danio.") && name.endsWith(".gz"));
            if (danioFiles != null && danioFiles.length > 0) {
                for (File danioFile : danioFiles) {
                    if (danioFile.getName().endsWith("RefSeqCatalog.danio." + fullCatalogMd5 + ".gz")) {
                        print(LOG, "Found filtered RefSeq catalog: " + danioFile.getName() + "\n");
                        System.out.println("Found filtered RefSeq catalog: " + danioFile.getName());
                        return danioFile;
                    }
                }
            }
            print(LOG, "WARN: Filtered RefSeqCatalog.danio.*.gz not found. Processing full RefSeqCatalog.gz. This might be slow and consume more memory.\n");
            return fullCatalog;
        }

        print(LOG, "ERROR: No RefSeq catalog file found (RefSeqCatalog.danio.*.gz or RefSeqCatalog.gz). This may lead to missing sequence lengths.\n");
        return null;
    }

    private File findGene2AccessionFile() {
        File fullFile = new File(workingDir, "gene2accession.gz");
        String fullFileMd5 = null;
        if (fullFile.exists()) {
            File[] danioFiles = workingDir.listFiles((dir, name) -> name.startsWith("gene2accession.danio.") && name.endsWith(".gz"));
            if (danioFiles != null && danioFiles.length > 0) {
                fullFileMd5 = md5File(fullFile, LOG);

                //now find a danioFile that matches the md5 of the full file
                System.out.println("Looking for gene2accession.danio." + fullFileMd5 + ".gz");
                for (File danioFile : danioFiles) {
                    if (danioFile.getName().endsWith("gene2accession.danio." + fullFileMd5 + ".gz")) {
                        print(LOG, "Found filtered gene2accession file: " + danioFile.getName() + "\n");
                        System.out.println("Found filtered gene2accession file: " + danioFile.getName());
                        return danioFile;
                    } else {
                        print(LOG, "Filtered gene2accession.danio.*.gz file found but does not match md5: " + danioFile.getName() + "\n");
                        System.out.println("Filtered gene2accession.danio.*.gz file found but does not match md5: " + danioFile.getName());
                    }
                }
            }

            print(LOG, "Filtered gene2accession.danio.*.gz not found. Using full gene2accession.gz.\n");
            System.out.println("Filtered gene2accession.danio.*.gz not found. Using full gene2accession.gz.\n");
            return fullFile;
        }

        print(LOG, "ERROR: No gene2accession file found (gene2accession.danio.*.gz or gene2accession.gz). This is critical for the load.\n");
        return null;
    }

    private void writeMapOfStringsToFileForDebug(String filename, Map<String, String> mapToPrint) {
        if (!debug) {
            return;
        }
        File file = new File(workingDir, filename);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            List<String> sortedKeys = mapToPrint.keySet().stream().sorted().collect(Collectors.toList());
            for (String key : sortedKeys) {
                writer.write(key + "\t" + mapToPrint.get(key) + "\n");
            }
        } catch (IOException e) {
            System.err.println("Cannot open or write to debug file " + filename + ": " + e.getMessage());
        }
    }

    private void writeMapOfListsToFileForDebug(String filename, Map<String, ? extends Collection<String>> mapToPrint) {
        if (!debug) {
            return;
        }
        File file = new File(workingDir, filename);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            List<String> sortedKeys = mapToPrint.keySet().stream().sorted().collect(Collectors.toList());
            for (String key : sortedKeys) {
                Collection<String> values = mapToPrint.get(key);
                if (values != null) {
                    writer.write(key + "\t" + String.join(" ", values) + "\n");
                } else {
                    writer.write(key + "\t\n"); // Handle null list case
                }
            }
        } catch (IOException e) {
            System.err.println("Cannot open or write to debug file " + filename + ": " + e.getMessage());
        }
    }

    private void writeMapOfMapsToFileForDebug(String filename, Map<String, Map<String, String>> mapToPrint) {
        if (!debug) {
            return;
        }
        File file = new File(workingDir, filename);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            List<String> sortedOuterKeys = mapToPrint.keySet().stream().sorted().collect(Collectors.toList());
            for (String outerKey : sortedOuterKeys) {
                Map<String, String> innerMap = mapToPrint.get(outerKey);
                String innerMapStr = innerMap.entrySet().stream()
                        .map(entry -> entry.getKey() + ":" + entry.getValue())
                        .collect(Collectors.joining(" "));
                writer.write(outerKey + "\t" + innerMapStr + "\n");
            }
        } catch (IOException e) {
            System.err.println("Cannot open or write to debug file " + filename + ": " + e.getMessage());
        }
    }

    private void writeDebug16Output() {
        //write for debugging purposes
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode rootNode = mapper.createObjectNode();

            rootNode.set("GenBankDNAncbiGeneIds", mapper.valueToTree(GenBankDNAncbiGeneIds));
            rootNode.set("GenPeptNCBIgeneIds", mapper.valueToTree(GenPeptNCBIgeneIds));
            rootNode.set("RefPeptNCBIgeneIds", mapper.valueToTree(RefPeptNCBIgeneIds));
            rootNode.set("RefSeqDNAncbiGeneIds", mapper.valueToTree(RefSeqDNAncbiGeneIds));
            rootNode.set("RefSeqRNAncbiGeneIds", mapper.valueToTree(RefSeqRNAncbiGeneIds));
            rootNode.set("supportedGeneNCBI", mapper.valueToTree(supportedGeneNCBI));
            rootNode.set("supportingAccNCBI", mapper.valueToTree(supportingAccNCBI));
            rootNode.set("noLength", mapper.valueToTree(noLength)); // Assuming keys in noLength are strings or have consistent toString())

        try {
            mapper.writeValue(new File(workingDir,"debug16.json"), rootNode);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void doSystemCommand(List<String> commandWithArguments, String stdoutRedirect, String stderrRedirect) {
        try {
            ExecProcess execProcess = new ExecProcess(commandWithArguments, false); // false for not verbose by default
            execProcess.setWorkingDirectory(workingDir); // Set working directory for the process

            File stdoutFile = new File(workingDir, stdoutRedirect);
            File stderrFile = new File(workingDir, stderrRedirect);

            stdoutFile.getParentFile().mkdirs();
            stderrFile.getParentFile().mkdirs();

            try (FileOutputStream fileOutputStream = new FileOutputStream(stdoutFile);
                 FileOutputStream fileErrorStream = new FileOutputStream(stderrFile)) {

                execProcess.setOutputStream(fileOutputStream);
                execProcess.setErrorStream(fileErrorStream);

                String commandStringForLog = String.join(" ", commandWithArguments);
                print(LOG, String.format("%s: Executing [%s] \n", this.getClass().getSimpleName(), commandStringForLog));
                int result = execProcess.exec();

                if (result != 0) {
                    String subjectLine = "Auto from " + instance + ": " + this.getClass().getSimpleName() + " :: failed at: " + commandStringForLog;
                    print(LOG, "\nFailed to execute system command, " + commandStringForLog + "\nExit.\n\n");
                    if (commandStringForLog.contains("loadNCBIgeneAccs.sql")) {
                        sendLoadLogs();
                    }
                    reportErrAndExit(subjectLine); // This will call System.exit()
                }
            }
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore interrupted status
            String commandString = String.join(" ", commandWithArguments);
            String subjectLine = "Auto from " + instance + ": " + this.getClass().getSimpleName() + " :: failed at: " + commandString + ". " + e.getMessage();
            print(LOG, "\nFailed to execute system command (exception), " + commandString + "\nExit.\n\n");
            if (commandString.contains("loadNCBIgeneAccs.sql")) {
                sendLoadLogs();
            }
            reportErrAndExit(subjectLine); // This will call System.exit()
        }
    }


    public void assertFileExistsAndNotEmpty(String filename, String errorMessage) {
        PortHelper.assertFileExistsAndNotEmpty(workingDir, filename, errorMessage);
    }

    private void printTimingInformation(int step) {
        stepCount = (long) step;
        String logLine = "Step " + step + " timestamp: " + nowToString("yyyy-MM-dd HH:mm:ss"); // Using specific format

        long timeDiff = 0;
        if (STEP_TIMESTAMP == 0) {
            STEP_TIMESTAMP = time();
        } else {
            Long lastTimeStamp = STEP_TIMESTAMP;
            STEP_TIMESTAMP = time();

            timeDiff = STEP_TIMESTAMP - lastTimeStamp; // time() returns seconds
            logLine += ". Time since last step: " + timeDiff + " seconds.";
        }
        System.out.println(logLine);
        if (LOG != null) { // Ensure LOG is initialized
            print(LOG, logLine + "\n");
        }
    }

    private String getReleaseNumber() {
        File releaseNumberFile = new File(workingDir, "RELEASE_NUMBER");
        String releaseNum = "0";
        try {
            downloadOrUseLocalFile("ftp://ftp.ncbi.nlm.nih.gov/refseq/release/RELEASE_NUMBER", releaseNumberFile, workingDir, LOG);
            if (releaseNumberFile.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(releaseNumberFile))) {
                    String line = reader.readLine();
                    if (line != null && line.trim().matches("\\d+")) {
                        releaseNum = line.trim();
                    } else {
                        print(LOG, "Could not parse release number from RELEASE_NUMBER file. Content: [" + line + "]. Using default: " + releaseNum + "\n");
                    }
                }
            } else if (!envTrue("SKIP_DOWNLOADS")) {
                print(LOG, "RELEASE_NUMBER file does not exist after download attempt and SKIP_DOWNLOADS is false. Using default: " + releaseNum + "\n");
            }
            String md5 = md5File(releaseNumberFile, LOG);
            System.out.println("RELEASE_NUMBER md5: " + (md5 != null ? md5 : "N/A (file might be missing)")); // Mirrored Perl's print to STDOUT
        } catch (Exception e) {
            print(LOG, "Error in getReleaseNumber: " + e.getMessage() + ". Continuing with release number: " + releaseNum + "\n");
        }
        return releaseNum;
    }

    private void downloadNCBIFilesForRelease(String releaseNum) {
        String catalogFolder = "ftp://ftp.ncbi.nlm.nih.gov/refseq/release/release-catalog/";
        String catalogFileBase = "RefSeq-release" + releaseNum + ".catalog"; // without .gz for now
        String ftpNCBIRefSeqCatalog = catalogFolder + catalogFileBase + ".gz";
        if (envExists("OVERRIDE_REFSEQ_CATALOG")) {
            ftpNCBIRefSeqCatalog = env("OVERRIDE_REFSEQ_CATALOG");
            System.out.println("Using overridden RefSeq catalog: " + ftpNCBIRefSeqCatalog);
            print(LOG, "Using overridden RefSeq catalog: " + ftpNCBIRefSeqCatalog + "\n");
        }

        File refSeqCatalogGz = new File(workingDir, "RefSeqCatalog.gz"); // e.g. RefSeq-releaseXXX.catalog.gz
        File gene2accessionGz = new File(workingDir, "gene2accession.gz");
        File gene2vegaGz = new File(workingDir, "gene2vega.gz");
        File zfGeneInfoGz = new File(workingDir, "zf_gene_info.gz");

        try {
            downloadOrUseLocalFile(ftpNCBIRefSeqCatalog, refSeqCatalogGz, workingDir, LOG);
            String refSeqCatalogMd5 = md5File(refSeqCatalogGz, LOG);
            System.out.println(refSeqCatalogGz.getName() + " md5: " + (refSeqCatalogMd5 != null ? refSeqCatalogMd5 : "N/A") + " at " + nowToString("yyyy-MM-dd HH:mm:ss"));

            if (!envTrue("SKIP_DOWNLOADS") && refSeqCatalogGz.exists() && refSeqCatalogMd5 != null) {
                File refSeqDanioMarker = new File(workingDir, "RefSeqCatalog.danio." + refSeqCatalogMd5 + ".md5"); // Marker for original full file
                File expectedFilteredFile = findRefSeqCatalogFile(); // See if a suitable filtered file already exists based on current content

                boolean filterNeeded = true;
                if (expectedFilteredFile != null && expectedFilteredFile.getName().startsWith("RefSeqCatalog.danio.")) {
                    print(LOG, "RefSeqCatalog.danio...gz (" + expectedFilteredFile.getName() + ") exists, implies filtering already processed for this RefSeqCatalog.gz version.\n");
                    System.out.println("RefSeqCatalog.danio...gz (" + expectedFilteredFile.getName() + ") exists, implies filtering already processed for this RefSeqCatalog.gz version.");

                    filterNeeded = false;
                }


                if (filterNeeded) {
                    if (refSeqDanioMarker.createNewFile()) print(LOG, "Created marker file: " + refSeqDanioMarker.getName() + "\n");
                    File refSeqDanioTemp = new File(workingDir, "RefSeqCatalog.danio.temp.gz");

                    // Delete existing RefSeqCatalog.danio.*.gz files before creating a new one to avoid confusion
                    rmFile(workingDir,"RefSeqCatalog.danio.*.gz", true);

                    String command = String.format("gunzip -c %s | grep '^7955' | gzip -n > %s", refSeqCatalogGz.getAbsolutePath(), refSeqDanioTemp.getAbsolutePath());
                    doSystemCommand(List.of("bash", "-c", command), "refseq_filter_out.log", "refseq_filter_err.log");

                    if (refSeqDanioTemp.exists() && refSeqDanioTemp.length() > 0) {
                        File refSeqDanioFinal = new File(workingDir, "RefSeqCatalog.danio." + refSeqCatalogMd5 + ".gz");
                        Files.move(refSeqDanioTemp.toPath(), refSeqDanioFinal.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        print(LOG, "Created filtered " + refSeqDanioFinal.getName() + "\n");
                    } else {
                        print(LOG, "Could not calculate MD5 for filtered RefSeqCatalog or temp file empty/missing, skipping rename.\n");
                        Files.deleteIfExists(refSeqDanioTemp.toPath());
                    }
                }
            }

            String gene2accessionUrl = "ftp://ftp.ncbi.nlm.nih.gov/gene/DATA/gene2accession.gz";
            if (envExists("OVERRIDE_GENE2ACCESSION")) {
                gene2accessionUrl = env("OVERRIDE_GENE2ACCESSION");
                System.out.println("Using overridden gene2accession URL: " + gene2accessionUrl);
                print(LOG, "Using overridden gene2accession URL: " + gene2accessionUrl + "\n");
            }
            downloadOrUseLocalFile(gene2accessionUrl, gene2accessionGz, workingDir, LOG);
            String gene2accessionMd5 = md5File(gene2accessionGz, LOG);
            System.out.println("gene2accession.gz md5: " + (gene2accessionMd5 != null ? gene2accessionMd5 : "N/A") + " at " + nowToString("yyyy-MM-dd HH:mm:ss"));

            if (!envTrue("SKIP_DOWNLOADS") && gene2accessionGz.exists() && gene2accessionMd5 != null) {
                File gene2accessionDanioMarker = new File(workingDir, "gene2accession.danio." + gene2accessionMd5 + ".md5");
                boolean filterNeeded = true;
                if (findGene2AccessionFile() != null && findGene2AccessionFile().getName().startsWith("gene2accession.danio.")) {
                    print(LOG, "gene2accession.danio...gz exists, implies filtering already processed for this gene2accession.gz version.\n");
                    filterNeeded = false;
                }

                if (filterNeeded) {
                    if (gene2accessionDanioMarker.createNewFile()) print(LOG, "Created marker file: " + gene2accessionDanioMarker.getName() + "\n");
                    File gene2accessionDanioTemp = new File(workingDir, "gene2accession.danio.temp.gz");
                    rmFile(workingDir,"gene2accession.danio.*.gz", true);
                    String command = String.format("gunzip -c %s | grep -E '^7955|^#tax_id' | gzip -n > %s", gene2accessionGz.getAbsolutePath(), gene2accessionDanioTemp.getAbsolutePath());
                    doSystemCommand(List.of("bash", "-c", command), "g2a_filter_out.log", "g2a_filter_err.log");

                    if (gene2accessionDanioTemp.exists() && gene2accessionDanioTemp.length() > 0) {
                        File gene2accessionDanioFinal = new File(workingDir, "gene2accession.danio." + gene2accessionMd5 + ".gz");
                        Files.move(gene2accessionDanioTemp.toPath(), gene2accessionDanioFinal.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        print(LOG, "Created filtered " + gene2accessionDanioFinal.getName() + "\n");
                    } else {
                        print(LOG, "Could not calculate MD5 for filtered gene2accession or temp file empty/missing, skipping rename.\n");
                        Files.deleteIfExists(gene2accessionDanioTemp.toPath());
                    }
                }
            }

            downloadOrUseLocalFile("ftp://ftp.ncbi.nlm.nih.gov/gene/DATA/ARCHIVE/gene2vega.gz", gene2vegaGz, workingDir, LOG);
            String gene2vegaMd5 = md5File(gene2vegaGz, LOG);
            System.out.println("gene2vega.gz md5: " + (gene2vegaMd5 != null ? gene2vegaMd5 : "N/A") + " at " + nowToString("yyyy-MM-dd HH:mm:ss"));

            downloadOrUseLocalFile("ftp://ftp.ncbi.nlm.nih.gov/gene/DATA/GENE_INFO/Non-mammalian_vertebrates/Danio_rerio.gene_info.gz", zfGeneInfoGz, workingDir, LOG);
            String zfGeneInfoMd5 = md5File(zfGeneInfoGz, LOG);
            System.out.println("zf_gene_info.gz md5: " + (zfGeneInfoMd5 != null ? zfGeneInfoMd5 : "N/A") + " at " + nowToString("yyyy-MM-dd HH:mm:ss"));

        } catch (Exception e) {
            String instanceName = (this.instance != null) ? this.instance : "UnknownInstance";
            reportErrAndExit("Auto from " + instanceName + ": NCBI_gene_load.pl :: Error during file download/processing: " + e.getMessage());
        }
    }
}