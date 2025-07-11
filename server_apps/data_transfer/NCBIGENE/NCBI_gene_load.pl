#!/opt/zfin/bin/perl
use strict;
use warnings FATAL => 'all';

#
# NCBI_gene_load.pl
#
# This script loads the following db_link records based on mapped gene records between ZFIN and NCBI:
# 1) NCBI Gene Ids
# 2) UniGene Ids     ## as of January, 2020, no more UniGene Ids will be loaded or kept at ZFIN.
# 3) RefSeq accessions (including RefSeq RNA, RefPept, RefSeq DNA)
# 4) GenBank accessions (including GenBank RNA, GenPept, GenBank DNA)
#
# The script execute the prepareNCBIgeneLoad.sql to generate the delete list and a set of ZFIN genes with RNA.
# Then, the script maps ZFIN gene records to NCBI gene records based on
# 1) common GenBank RNA accessions
# 2) common Vega Gene Id
# Then, the script execute the loadNCBIgeneAccs.sql to delete all the db_link records previously loaded
# (according to the delete list), and load all the accessions for the gene records mapped.
#
# The values of dblink_length are also processed and loaded.
# And statistics and various reports are generated and emailed.
#
# This script makes heavy use of global variables. They are all defined and documented just after the main subroutine.
#
# See README or http://fogbugz.zfin.org/default.asp?W1625
# for a detailed documentation of the steps.


use DBI;
use Cwd;
use POSIX;
use Try::Tiny;
use FindBin;

#relative path to library file(s) (ZFINPerlModules.pm)
use lib "$FindBin::Bin/../../perl_lib/";
use ZFINPerlModules qw(assertEnvironment trim getPropertyValue downloadOrUseLocalFile md5File assertFileExistsAndNotEmpty) ;
use JSON;

our $debug = 1;
#########################
# Global Variables Here #
#########################
our $pubMappedbasedOnRNA = "ZDB-PUB-020723-3";
our $pubMappedbasedOnVega = "ZDB-PUB-130725-2";
our $pubMappedbasedOnNCBISupplement = "ZDB-PUB-230516-87";

our $fdcontNCBIgeneId = "ZDB-FDBCONT-040412-1";
our $fdcontVega = "ZDB-FDBCONT-040412-14";
our $fdcontGenBankRNA = "ZDB-FDBCONT-040412-37";
our $fdcontGenPept = "ZDB-FDBCONT-040412-42";
our $fdcontGenBankDNA = "ZDB-FDBCONT-040412-36";
our $fdcontRefSeqRNA = "ZDB-FDBCONT-040412-38";
our $fdcontRefPept = "ZDB-FDBCONT-040412-39";
our $fdcontRefSeqDNA = "ZDB-FDBCONT-040527-1";

#used in eg. initializeDatabase
our $dbname;
our $dbhost;
our $instance;
our $username;
our $password;
our $handle;

#used in eg. getMetricsOfDbLinksToDelete
our %toDelete;
our $ctToDelete;

#used in eg. getRecordCounts
our %genesWithRefSeqBeforeLoad;
our $ctGenesWithRefSeqBefore;
our $numNCBIgeneIdBefore;
our $numRefSeqRNABefore;
our $numRefPeptBefore;
our $numRefSeqDNABefore;
our $numGenBankRNABefore;
our $numGenPeptBefore;
our $numGenBankDNABefore;
our $numGenesRefSeqRNABefore;
our $numGenesRefSeqPeptBefore;
our $numGenesGenBankBefore;

#used in eg. readZfinGeneInfoFile
our $ctVegaIdsNCBI;
our %NCBIgeneWithMultipleVega;
our %NCBIidsGeneSymbols;
our %geneSymbolsNCBIids;
our %vegaIdsNCBIids;
our %vegaIdwithMultipleNCBIids;

#used in eg. initializeSetsOfZfinRecords
our %supportedGeneZFIN;
our %supportingAccZFIN;
our %accZFINsupportingMoreThan1;
our %geneZFINwithAccSupportingMoreThan1;
our %accZFINsupportingOnly1;

#used in eg. initializeSequenceLengthHash, lots of other places
our %sequenceLength;

#used in eg. parseGene2AccessionFile
our $ctNoLength;
our $ctNoLengthRefSeq;
our $ctZebrafishGene2accession;
our %GenBankDNAncbiGeneIds;
our %GenPeptNCBIgeneIds;
our %RefPeptNCBIgeneIds;
our %RefSeqDNAncbiGeneIds;
our %RefSeqRNAncbiGeneIds;
our %noLength;
our %supportedGeneNCBI;
our %supportingAccNCBI;

# used in eg. initializeHashOfNCBIAccessionsSupportingMultipleGenes
our %accNCBIsupportingMoreThan1;
our %accNCBIsupportingOnly1;
our %geneNCBIwithAccSupportingMoreThan1;

# used in eg. initializeMapOfZfinToNCBIgeneIds
our %oneToNZFINtoNCBI;
our %oneToOneZFINtoNCBI;
our %genesZFINwithNoRNAFoundAtNCBI;

# used in eg. oneWayMappingNCBItoZfinGenes
our %oneToOneNCBItoZFIN;
our %oneToNNCBItoZFIN;

# used in eg. prepare2WayMappingResults
our %mapped; ## the list of 1:1; key: ZDB gene Id; value: NCBI gene Id
our %mappedReversed;
our $ctOneToOneNCBI;

our %ncbiSupplementMap;
our %ncbiSupplementMapReversed;

# used in eg. writeNCBIgeneIdsMappedBasedOnGenBankRNA
our $ctToLoad;

# used in eg. getOneToNNCBItoZFINgeneIds
our %nToOne;
our %oneToN;

# used in eg. getNtoOneAndNtoNfromZFINtoNCBI
our %zdbGeneIdsNtoOneAndNtoN;

# used in eg. buildVegaIDMappings
our %ZDBgeneAndVegaGeneIds;
our %VegaGeneAndZDBgeneIds;
our %ZDBgeneWithMultipleVegaGeneIds;
our %vegaGeneIdWithMultipleZFINgenes;

# used in eg. writeCommonVegaGeneIdMappings
our %oneToOneViaVega;

# used in eg. getGenBankAndRefSeqsWithZfinGenes
our %geneAccFdbcont;

# used in eg. initializeGenPeptAccessionsMap
our %GenPeptAttributedToNonLoadPub;
our %GenPeptDbLinkIdAttributedToNonLoadPub;

# used in eg. processGenBankAccessionsAssociatedToNonLoadPubs
our %GenPeptsToLoad;

# readZfinGeneInfoFile
our %geneZDBidsSymbols;

our $FASTA_LEN_COMMAND='./fasta_len.pl'; #was fasta_len.awk
if (exists($ENV{'FASTA_LEN_COMMAND'})) {
    $FASTA_LEN_COMMAND=$ENV{'FASTA_LEN_COMMAND'};
}
assertFileExistsAndNotEmpty($FASTA_LEN_COMMAND, "Could not find FASTA_LEN_COMMAND: $FASTA_LEN_COMMAND");

our $SWISSPROT_EMAIL_ERR;
if (exists($ENV{'SWISSPROT_EMAIL_ERR'})) {
    $SWISSPROT_EMAIL_ERR = $ENV{'SWISSPROT_EMAIL_ERR'};
} else {
    $SWISSPROT_EMAIL_ERR = 'root@localhost';
}

our $SWISSPROT_EMAIL_REPORT;
if (exists($ENV{'SWISSPROT_EMAIL_REPORT'})) {
    $SWISSPROT_EMAIL_REPORT = $ENV{'SWISSPROT_EMAIL_REPORT'};
} else {
    $SWISSPROT_EMAIL_REPORT = 'root@localhost';
}

our $stepCount = 0;
our $STEP_TIMESTAMP = 0;
our $START_TIMESTAMP = strftime("%Y-%m-%d_%H-%M-%S", localtime(time()));
my $dataReport = {d=>{success=>0,error=>"key is required"}};

###########################
# End Globals             #
###########################


###########################
# Data Load Entrypoint #
###########################
sub main {

    assertEnvironment('PGHOST', 'DB_NAME');

    assertExpectedFilesExist();

    system("/bin/date");

    if (exists($ENV{'WORKING_DIR'}) && $ENV{'WORKING_DIR'}) {
        chdir $ENV{'WORKING_DIR'};
    } else {
        chdir $ENV{'ROOT_PATH'} . "/server_apps/data_transfer/NCBIGENE/";
    }

    initializeDatabase();

    removeOldFiles();

    openLoggingFileHandles();
    printTimingInformation(1);

    #-------------------------------------------------------------------------------------------------
    # Step 1: Download NCBI data files
    #-------------------------------------------------------------------------------------------------
    downloadNCBIFiles();
    printTimingInformation(2);

    captureBeforeState();
    printTimingInformation(201);

    prepareNCBIgeneLoadDatabaseQuery();
    printTimingInformation(3);

    getMetricsOfDbLinksToDelete();
    printTimingInformation(4);

    # Get Record Counts using global variables
    getRecordCounts();
    printTimingInformation(5);

    readZfinGeneInfoFile();
    printTimingInformation(6);

    #----------------------------------------------------------------------------------------------------------------------
    # Step 5: Map ZFIN gene records to NCBI gene records based on GenBank RNA sequences
    #----------------------------------------------------------------------------------------------------------------------

    #-----------------------------------------
    # Step 5-1: initial set of ZFIN records
    #-----------------------------------------
    initializeSetsOfZfinRecordsPart1();
    initializeSetsOfZfinRecordsPart2();
    printTimingInformation(7);

    #--------------------------------------------------------------------------------------------------------------
    # Step 5-2: Get dblink_length values
    #
    # This section continues to deal with dblink_length field
    # There are 3 sources for length:
    # 1) the existing dblink_length for GenBank including GenPept records
    # 2) the length value of RefSeq sequences on NCBI's RefSeq-release#.catalog file
    # 3) calculated length
    # 1) and 2) will be done by the following section and before parsing the gene2accession file.
    # And the length value will be stored in hash %sequenceLength
    # During parsing gene2accession file, accessions still missing length will be stored in a hash named %noLength
    # 3) will be done after parsing gene2accession file.
    #---------------------------------------------------------------------------------------------------------------
    initializeSequenceLengthHash();
    printTimingInformation(8);

    #----------------------- 2) parse RefSeq-release#.catalog file to get the length for RefSeq sequences ----------------------

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

    #---------------- open a .unl file as the add list -----------------
    open(TOLOAD, ">toLoad.unl") || die "Cannot open toLoad.unl : $!\n";

    #---------------- open a .unl file as record_attributions to preserve -----------------
    open(TO_PRESERVE, ">toPreserve.unl") || die "Cannot open toPreserve.unl : $!\n";

    # -------- write the NCBI gene Ids mapped based on GenBank RNA accessions on toLoad.unl ------------
    writeNCBIgeneIdsMappedBasedOnGenBankRNA();
    printTimingInformation(21);

    # -------- write the NCBI gene Ids mapped based supplementary ncbi load logic ------------
    writeNCBIgeneIdsMappedBasedOnSupplementaryLoad();
    printTimingInformation(22);

    #------------------------ get 1:N list and N:N from ZFIN to NCBI -----------------------------
    getOneToNNCBItoZFINgeneIds();
    printTimingInformation(23);

    #------------------------ get N:1 list and N:N from ZFIN to NCBI -----------------------------
    getNtoOneAndNtoNfromZFINtoNCBI();
    printTimingInformation(24);

    #--------------------- report 1:N ---------------------------------------------
    reportOneToN();
    printTimingInformation(25);

    #------------------- report N:1 -------------------------------------------------
    reportNtoOne();
    printTimingInformation(26);

    ##-----------------------------------------------------------------------------------
    ## Step 6: map ZFIN gene records to NCBI gene Ids based on common Vega Gene Id
    ##-----------------------------------------------------------------------------------

    #---------------------------------------------------------------------------
    # prepare the list of ZFIN gene with Vega Ids to be mapped to NCBI records
    #---------------------------------------------------------------------------
    buildVegaIDMappings();
    printTimingInformation(27);

    ## ---------------------------------------------------------------------------------------------------------------------
    ## doing the mapping based on common Vega Gene Id
    ## ---------------------------------------------------------------------------------------------------------------------
    writeCommonVegaGeneIdMappings();
    printTimingInformation(28);

    #--------------------------------------------------------------------------------------------------------------
    # This section CONTINUES to deal with dblink_length field
    # There are 3 sources for length:
    # 1) the existing dblink_length for GenBank including GenPept records
    # 2) the length value of RefSeq sequences on NCBI's RefSeq-release#.catalog file
    # 3) calculated length
    # The first two have been done before parsing the gene2accession file.
    # During parsing gene2accession file, accessions still missing length are stored in a hash named %noLength
    #---------------------------------------------------------------------------------------------------------------

    #----------------------- 3) calculate the length for the those still with no length ---------------
    calculateLengthForAccessionsWithoutLength();
    printTimingInformation(29);

    #---------------------------------------------------------------------------------------------
    # Step 7: prepare the final add-list for RefSeq and GenBank records
    #---------------------------------------------------------------------------------------------
    getGenBankAndRefSeqsWithZfinGenes();
    printTimingInformation(30);

    #---------------------------------------------------------------------------
    #  write GenBank RNA accessions with mapped genes onto toLoad.unl
    #---------------------------------------------------------------------------
    writeGenBankRNAaccessionsWithMappedGenesToLoad();
    printTimingInformation(31);

    #---------------------------------------------------------------------------------------
    #  write GenPept accessions with mapped genes onto toLoad.unl
    #---------------------------------------------------------------------------------------
    initializeGenPeptAccessionsMap();
    printTimingInformation(32);

    processGenBankAccessionsAssociatedToNonLoadPubs();
    printTimingInformation(33);

    # ----- get all the Genpept accessions associated with gene at ZFIN, and those with multiple ZFIN genes ----------------------------
    printGenPeptsAssociatedWithGeneAtZFIN();
    printTimingInformation(34);

    #---------------------------------------------------------------------------
    #  write GenBank DNA accessions with mapped genes onto toLoad.unl
    #---------------------------------------------------------------------------
    writeGenBankDNAaccessionsWithMappedGenesToLoad();
    printTimingInformation(35);

    #---------------------------------------------------------------------------
    #  write RefSeq RNA accessions with mapped genes onto toLoad.unl
    #---------------------------------------------------------------------------
    writeRefSeqRNAaccessionsWithMappedGenesToLoad();
    printTimingInformation(36);

    #---------------------------------------------------------------------------
    #  write RefPept accessions with mapped genes onto toLoad.unl
    #---------------------------------------------------------------------------
    writeRefPeptAccessionsWithMappedGenesToLoad();
    printTimingInformation(37);

    #---------------------------------------------------------------------------
    #  write RefSeq DNA accessions with mapped genes onto toLoad.unl
    #---------------------------------------------------------------------------
    writeRefSeqDNAaccessionsWithMappedGenesToLoad();
    printTimingInformation(38);

    closeUnloadFiles();

    printStatsBeforeDelete();
    printTimingInformation(39);

    if ($ENV{'EARLY_EXIT'}) {
        print LOG "Early exit requested, skipping deletion and load steps.\n";
        print "Early exit requested, skipping deletion and load steps.\n";
        exit;
    }

    #-----------------------------------------------------------------------------------------------------------------------
    # Step 8: execute the SQL file to do the deletion according to delete list, and do the loading according to te add list
    #-----------------------------------------------------------------------------------------------------------------------
    executeDeleteAndLoadSQLFile();
    printTimingInformation(40);

    sendLoadLogs();
    printTimingInformation(41);

    #-------------------------------------------------------------------------------------------------
    # Step 9: Report the GenPept accessions associated with multiple ZFIN genes after the load.
    # Report GenPept accessions associated with ZFIN genes still attributed to a non-load pub.
    # And do the record counts after the load, and report statistics.
    #-------------------------------------------------------------------------------------------------

    reportAllLoadStatistics();
    printTimingInformation(42);

    captureAfterState();
    printTimingInformation(4201);

    compareBeforeAndAfterState();
    printTimingInformation(4202);

    emailLoadReports();
    printTimingInformation(43);

    # Sort noLength.unl so we can compare the results with the previous run.
    doSystemCommand("sort -o noLength.unl noLength.unl");

    system("/bin/date");

    print "\nAll done!\n";
    print LOG "\n\nAll done! \n\n\n";
    close LOG;

    exit;
}


###########################
# End of Global Variables #
###########################

#---------------------- subroutines  -------------------------------------------------------

# The return code from "system" isn't reliable when used in syntax of "system(...) or die ..."
# Use this subroutine to a better handling.
sub doSystemCommand {

  my $systemCommand = $_[0];

  print LOG "$0: Executing [$systemCommand] \n";

  my $returnCode = system( $systemCommand );

  if ( $returnCode != 0 ) {
     my $subjectLine = "Auto from $instance: " . "NCBI_gene_load.pl :: failed at: $systemCommand . $! ";
     print LOG "\nFailed to execute system command, $systemCommand\nExit.\n\n";

     if ($systemCommand =~ m/loadNCBIgeneAccs\.sql/) {
       sendLoadLogs();
     }
     reportErrAndExit($subjectLine);
  }
}

sub reportErrAndExit {
  my $subjectError = $_[0];
  ZFINPerlModules->sendMailWithAttachedReport($SWISSPROT_EMAIL_ERR,"$subjectError","logNCBIgeneLoad");
  close LOG;
  exit -1;
}

sub sendLoadLogs {
  my $subject = "Auto from $instance: NCBI_gene_load.pl :: loadLog1 file";
  ZFINPerlModules->sendMailWithAttachedReport($SWISSPROT_EMAIL_ERR,"$subject","loadLog1");
}

sub assertExpectedFilesExist {
    # We need these files to continue:
    # RELEASE_NUMBER gene2accession.gz RefSeqCatalog.gz gene2vega.gz zf_gene_info.gz seq.fasta
    # seq.fasta is optional if FORCE_EFETCH is set

    if (exists($ENV{'SKIP_DOWNLOADS'}) && $ENV{'SKIP_DOWNLOADS'}) {
        assertFileExistsAndNotEmpty("gene2accession.gz", "missing gene2accession.gz");
        assertFileExistsAndNotEmpty("RELEASE_NUMBER", "missing RELEASE_NUMBER");
        assertFileExistsAndNotEmpty("RefSeqCatalog.gz", "missing RefSeqCatalog.gz");
        assertFileExistsAndNotEmpty("gene2vega.gz", "missing gene2vega.gz");
        assertFileExistsAndNotEmpty("zf_gene_info.gz", "missing zf_gene_info.gz");

        unless (exists($ENV{'FORCE_EFETCH'}) && $ENV{'FORCE_EFETCH'}) {
            assertFileExistsAndNotEmpty("seq.fasta", "missing seq.fasta");
        }
    }
}

sub initializeDatabase {
    $dbname = $ENV{'DB_NAME'};
    $dbhost = $ENV{'PGHOST'};
    if (exists($ENV{'INSTANCE'})) {
        $instance = $ENV{'INSTANCE'};
    } else {
        $instance = "UNDEFINED_INSTANCE";
    }

    $username = "";
    $password = "";
    #open a handle on the db
    $handle = DBI->connect ("DBI:Pg:dbname=$dbname;host=$dbhost", $username, $password)
        or die "Cannot connect to database: $DBI::errstr\n";
}

sub removeOldFiles {
    #------------------------------------------------
    # remove old files
    #------------------------------------------------

    print "Removing prepareLog* loadLog* logNCBIgeneLoad debug* report* toDelete.unl toMap.unl toLoad.unl length.unl noLength.unl\n";

    system("/bin/rm -f prepareLog*");
    system("/bin/rm -f loadLog*");
    system("/bin/rm -f logNCBIgeneLoad");
    system("/bin/rm -f debug*");
    system("/bin/rm -f report*");
    system("/bin/rm -f *.html");
    system("/bin/rm -f *.csv");
    system("/bin/rm -f *.xlsx");
    system("/bin/rm -f *.json");
    system("/bin/rm -f toDelete.unl");
    system("/bin/rm -f toMap.unl");
    system("/bin/rm -f toLoad.unl");
    system("/bin/rm -f length.unl");
    system("/bin/rm -f noLength.unl");

    if (!$ENV{"SKIP_DOWNLOADS"}) {
        if (!$ENV{"NO_SLEEP"}) {
            print "Removing old files in 30 seconds...\n";
            sleep(30);
        }

        print "Removing seq.fasta zf_gene_info.gz gene2vega.gz gene2accession.gz RefSeqCatalog.gz RELEASE_NUMBER\n";

        if (!$ENV{"SKIP_EFETCH"}) {
            system("/bin/rm -f seq.fasta");
        }

        system("/bin/rm -f zf_gene_info.gz");
        system("/bin/rm -f gene2vega.gz");
        system("/bin/rm -f gene2accession.gz");
        system("/bin/rm -f RefSeqCatalog.gz");
        system("/bin/rm -f RELEASE_NUMBER");
        system("./clear-artifacts.sh");
    }
}

sub openLoggingFileHandles {
    open LOG, '>', "logNCBIgeneLoad" or die "can not open logNCBIgeneLoad: $! \n";
    open STATS_PRIORITY1, '>', "reportStatistics_p1" or die "can not open reportStatistics_p1" ;
    open STATS_PRIORITY2, '>', "reportStatistics_p2" or die "can not open reportStatistics_p2" ;
    open STATS, '>', "reportStatistics" or die "can not open reportStatistics" ;
    print LOG "Start ... \n";
}

sub printTimingInformation {
    $stepCount = shift;
    my $logLine = "Step $stepCount timestamp: " . strftime("%Y-%m-%d %H:%M:%S", localtime(time()));

    my $timeDiff = 0;
    if ($STEP_TIMESTAMP == 0) {
        $STEP_TIMESTAMP = time();
    } else {
        my $lastTimeStamp = $STEP_TIMESTAMP;
        $STEP_TIMESTAMP = time();

        $timeDiff = $STEP_TIMESTAMP - $lastTimeStamp;
        $logLine .= ". Time since last step: $timeDiff seconds.";
    }
    print "$logLine\n";
    print LOG "$logLine\n";
}

sub downloadNCBIFiles {
    ## only the following RefSeq catalog file may remain unchanged over a period of time
    ## the rest 3 are changing every day
    our $releaseNum = &getReleaseNumber();
    print LOG "RefSeq Catalog Release Number is $releaseNum.\n\n";

    downloadNCBIFilesForRelease($releaseNum);
    print LOG "Done with downloading.\n\n";
}

sub getReleaseNumber {
    downloadOrUseLocalFile("ftp://ftp.ncbi.nlm.nih.gov/refseq/release/RELEASE_NUMBER", "RELEASE_NUMBER");

    open (REFSEQRELEASENUM, "RELEASE_NUMBER") ||  die "Cannot open RELEASE_NUMBER : $!\n";

    my $releaseNum = 0;
    while (<REFSEQRELEASENUM>) {
        if($_ =~ m/(\d+)/) {
            $releaseNum = $1;
        }
    }

    close REFSEQRELEASENUM;

    my $hash = md5File('RELEASE_NUMBER');
    print "RELEASE_NUMBER md5: $hash\n";

    return $releaseNum;
}

sub downloadNCBIFilesForRelease {
    my $releaseNum = shift;
    my $catlogFolder = "ftp://ftp.ncbi.nlm.nih.gov/refseq/release/release-catalog/";
    my $catalogFile = "RefSeq-release" . $releaseNum . ".catalog.gz";
    my $ftpNCBIrefSeqCatalog = $catlogFolder . $catalogFile;

    try {
        my $hash;
        downloadOrUseLocalFile($ftpNCBIrefSeqCatalog, "RefSeqCatalog.gz");
        $hash = md5File('RefSeqCatalog.gz');
        print "RefSeqCatalog.gz md5: $hash at " . strftime("%Y-%m-%d %H:%M:%S", localtime(time())) . " \n";

        #filter to only zebrafish records (unless it already exists)
        if (!-e "RefSeqCatalog.danio.$hash.md5") {
            doSystemCommand("touch RefSeqCatalog.danio.$hash.md5");

            #delete file if exists:
            unlink "RefSeqCatalog.danio.gz" if -e "RefSeqCatalog.danio.gz";
            doSystemCommand("cat RefSeqCatalog.gz | gunzip -d | grep '^7955' | gzip -n > RefSeqCatalog.danio.gz");

            #rename file to include original md5 hash
            rename("RefSeqCatalog.danio.gz", "RefSeqCatalog.danio.$hash.gz");
        }

        downloadOrUseLocalFile("ftp://ftp.ncbi.nlm.nih.gov/gene/DATA/gene2accession.gz", "gene2accession.gz");
        $hash = md5File('gene2accession.gz');
        print "gene2accession.gz md5: $hash at " . strftime("%Y-%m-%d %H:%M:%S", localtime(time())) . " \n";

        #filter to only zebrafish records (unless it already exists)
        if (!-e "gene2accession.danio.$hash.md5") {
            doSystemCommand("touch gene2accession.danio.$hash.md5");

            #delete file if exists:
            unlink "gene2accession.danio.gz" if -e "gene2accession.danio.gz";
            doSystemCommand("cat gene2accession.gz | gunzip -d | grep -E '^7955|^#tax_id' | gzip -n > gene2accession.danio.gz");

            #rename file to include original md5 hash
            rename("gene2accession.danio.gz", "gene2accession.danio.$hash.gz");
        }

        downloadOrUseLocalFile("ftp://ftp.ncbi.nlm.nih.gov/gene/DATA/ARCHIVE/gene2vega.gz", "gene2vega.gz");
        $hash = md5File('gene2vega.gz');
        print "gene2vega.gz md5: $hash at " . strftime("%Y-%m-%d %H:%M:%S", localtime(time())) . " \n";

        downloadOrUseLocalFile("ftp://ftp.ncbi.nlm.nih.gov/gene/DATA/GENE_INFO/Non-mammalian_vertebrates/Danio_rerio.gene_info.gz", "zf_gene_info.gz");
        $hash = md5File('zf_gene_info.gz');
        print "zf_gene_info.gz md5: $hash at " . strftime("%Y-%m-%d %H:%M:%S", localtime(time())) . " \n";
    }
    catch {
        chomp $_;
        reportErrAndExit("Auto from $instance: NCBI_gene_load.pl :: $_");
    };

    #-------------------------------------------------------------------------------------------------
    # Check if all the downloaded and decompressed NCBI data files are in place.
    # If not, stop the process and send email to alert.
    #-------------------------------------------------------------------------------------------------

    if (!-e "zf_gene_info.gz" || !-e "gene2accession.gz" || !-e "RefSeqCatalog.gz" || !-e "gene2vega.gz") {
        my $subjectLine = "Auto from $instance: NCBI_gene_load.pl :: ERROR with download";
        print LOG "\nMissing one or more downloaded NCBI file(s)\n\n";
        reportErrAndExit($subjectLine);
    }
}

sub captureBeforeState {
    captureState("before_load.csv");
}

sub captureAfterState {
    captureState("after_load.csv");
}

sub captureState {
    my $outputFilename = shift;
    try {
        my $command = "psql --echo-all -v ON_ERROR_STOP=1 -h $ENV{'PGHOST'}  -d $ENV{'DB_NAME'} -a -c " .
            "\"\\copy (select d.*, string_agg(r.recattrib_source_zdb_id, '|' order by r.recattrib_source_zdb_id) as recattrib_source_zdb_id " .
            " from db_link d left join record_attribution r on d.dblink_zdb_id = r.recattrib_data_zdb_id " .
            " group by dblink_linked_recid,dblink_acc_num,dblink_info,dblink_zdb_id,dblink_acc_num_display,dblink_length,dblink_fdbcont_zdb_id " .
            " order by dblink_linked_recid, dblink_acc_num ) to '$outputFilename' with csv header \" >prepareLog1 2> prepareLog2";
        doSystemCommand($command);
    } catch {
        chomp $_;
        reportErrAndExit("Auto from $instance: NCBI_gene_load.pl :: failed at before capture - $_");
    } ;
}

sub compareBeforeAndAfterState {
    my $beforeFile = "before_load.csv";
    my $afterFile = "after_load.csv";

    if (!-e $beforeFile || !-e $afterFile) {
        reportErrAndExit("Auto from $instance: NCBI_gene_load.pl :: before or after file is missing");
    }

    my $currentDir = cwd;
    my $command = "gradle ncbiCharacterize --args=\"$currentDir/$beforeFile $currentDir/$afterFile\"";

    # Set the JAVA_HOME path to override the jenkins one
    $ENV{'JAVA_HOME'} = getPropertyValue("JAVA_HOME");
    if (exists($ENV{'OVERRIDE_JAVA_HOME'})) {
        print "Overriding JAVA_HOME with $ENV{'OVERRIDE_JAVA_HOME'}\n";
        $ENV{'JAVA_HOME'} = $ENV{'OVERRIDE_JAVA_HOME'};
    }

    my $cmdString = "cd " . $ENV{'SOURCEROOT'} . " ; " .
        $command . " ; " .
        "cd $currentDir";
    print "Executing $cmdString\n";
    print LOG "Executing $cmdString\n";

    doSystemCommand($cmdString);


    if (-s "before_after.xlsx") {
        print LOG "Differences found between before and after state. See before_after.xlsx for details.\n";
    } else {
        print LOG "Difference generation failed for before and after state.\n";
    }
}

sub prepareNCBIgeneLoadDatabaseQuery {
    # Global: $dbname
    #--------------------------------------------------------------------------------------------------------------------
    # Step 2: execute prepareNCBIgeneLoad.sql to prepare
    #    1) a delete list, toDelete.unl
    #    2) a list of ZFIN genes to be mapped, toMap.unl
    #--------------------------------------------------------------------------------------------------------------------

    try {
        doSystemCommand("psql --echo-all -v ON_ERROR_STOP=1 -d $ENV{'DB_NAME'} -a -f prepareNCBIgeneLoad.sql >prepareLog1 2> prepareLog2");
    } catch {
        chomp $_;
        reportErrAndExit("Auto from $instance: NCBI_gene_load.pl :: faile at prepareNCBIgeneLoad.sql - $_");
    } ;

    print LOG "Done with preparing the delete list and the list for mapping.\n\n";

    my $subject = "Auto from $instance: NCBI_gene_load.pl :: prepareLog1 file";
    ZFINPerlModules->sendMailWithAttachedReport($SWISSPROT_EMAIL_ERR,"$subject","prepareLog1");

    $subject = "Auto from $instance: NCBI_gene_load.pl :: prepareLog2 file";
    ZFINPerlModules->sendMailWithAttachedReport($SWISSPROT_EMAIL_ERR,"$subject","prepareLog2");
}

sub getMetricsOfDbLinksToDelete {
    # This is a hash to store the zdb ids of db_link record to be deleted; used at later step
    # key: dblink zdb id
    # value: 1
    # Global: $ctToDelete
    # Global: %toDelete
    %toDelete = ();
    $ctToDelete = 0;

    open (TODELETE, "toDelete.unl") ||  die "Cannot open toDelete.unl : $!\n";

    while (<TODELETE>) {
        chomp;
        if ($_) {
            $ctToDelete++;
            my $dblinkIdToBeDeleted = $_;
            $toDelete{$dblinkIdToBeDeleted} = 1;
        }
    }

    close TODELETE;

    if ($ctToDelete == 0) {
        my $subjectLine = "Auto from $instance: " . "NCBI_gene_load.pl :: the delete list, toDelete.unl, is empty";
        # print LOG "\nThe delete list, toDelete.unl is empty. Something is wrong.\n\n";
        reportErrAndExit($subjectLine);
    }
}

sub getRecordCounts {
    #--------------------------------------------------------------------------------------
    # Step 3: record counts
    #--------------------------------------------------------------------------------------
    
    # globals: $dbhost          
    #           %genesWithRefSeqBeforeLoad
    #           $ctGenesWithRefSeqBefore          
    #           $numNCBIgeneIdBefore          
    #           $numRefSeqRNABefore          
    #           $numRefPeptBefore          
    #           $numRefSeqDNABefore          
    #           $numGenBankRNABefore          
    #           $numGenPeptBefore          
    #           $numGenBankDNABefore          
    #           $numGenesRefSeqRNABefore          
    #           $numGenesRefSeqPeptBefore          
    #           $numGenesGenBankBefore


    my $sql = "select mrkr_zdb_id, mrkr_abbrev from marker
         where (mrkr_zdb_id like 'ZDB-GENE%' or mrkr_zdb_id like '%RNAG%')
           and exists (select 1 from db_link
         where dblink_linked_recid = mrkr_zdb_id
           and dblink_fdbcont_zdb_id in ('$fdcontRefSeqRNA','$fdcontRefPept','$fdcontRefSeqDNA'));";

    my $curGenesWithRefSeq = $handle->prepare($sql);

    $curGenesWithRefSeq->execute;

    my ($geneId, $geneSymbol);
    $curGenesWithRefSeq->bind_columns(\$geneId,\$geneSymbol);

    %genesWithRefSeqBeforeLoad = ();
    while ($curGenesWithRefSeq->fetch) {
        $genesWithRefSeqBeforeLoad{$geneId} = $geneSymbol;
    }

    $curGenesWithRefSeq->finish();

    $ctGenesWithRefSeqBefore = scalar(keys %genesWithRefSeqBeforeLoad);


    # NCBI Gene Id
    $sql = getSqlForGeneAndRnagDbLinksFromFdbContId($fdcontNCBIgeneId);
    $numNCBIgeneIdBefore = ZFINPerlModules->countData($sql);

    #RefSeq RNA
    $sql = getSqlForGeneAndRnagDbLinksFromFdbContId($fdcontRefSeqRNA);
    $numRefSeqRNABefore = ZFINPerlModules->countData($sql);

    # RefPept
    $sql = getSqlForGeneAndRnagDbLinksFromFdbContId($fdcontRefPept);
    $numRefPeptBefore = ZFINPerlModules->countData($sql);

    #RefSeq DNA
    $sql = getSqlForGeneAndRnagDbLinksFromFdbContId($fdcontRefSeqDNA);
    $numRefSeqDNABefore = ZFINPerlModules->countData($sql);

    # GenBank RNA (only those loaded - excluding curated ones)
    $sql = getSqlForGeneAndRnagDbLinksSupportedByLoadPubsFromFdbContId($fdcontGenBankRNA);
    $numGenBankRNABefore = ZFINPerlModules->countData($sql);

    # GenPept (only those loaded - excluding curated ones)
    $sql = getSqlForGeneAndRnagDbLinksSupportedByLoadPubsFromFdbContId($fdcontGenPept);
    $numGenPeptBefore = ZFINPerlModules->countData($sql);

    # GenBank DNA (only those loaded - excluding curated ones)
    $sql = getSqlForGeneAndRnagDbLinksSupportedByLoadPubsFromFdbContId($fdcontGenBankDNA);
    $numGenBankDNABefore = ZFINPerlModules->countData($sql);

    # number of genes with RefSeq RNA
    $sql = "select distinct dblink_linked_recid
          from db_link
         where dblink_fdbcont_zdb_id = '$fdcontRefSeqRNA'
           and dblink_acc_num like 'NM_%'
           and (dblink_linked_recid like 'ZDB-GENE%' or dblink_linked_recid like '%RNAG%');";

     $numGenesRefSeqRNABefore = ZFINPerlModules->countData($sql);

    # number of genes with RefPept
    $sql = "select distinct dblink_linked_recid
          from db_link
         where dblink_fdbcont_zdb_id = '$fdcontRefPept'
           and dblink_acc_num like 'NP_%'
           and (dblink_linked_recid like 'ZDB-GENE%' or dblink_linked_recid like '%RNAG%');";

     $numGenesRefSeqPeptBefore = ZFINPerlModules->countData($sql);

    # number of genes with GenBank
    $sql = "select distinct dblink_linked_recid
          from db_link, foreign_db_contains, foreign_db
         where dblink_fdbcont_zdb_id = fdbcont_zdb_id
           and fdbcont_fdb_db_id = fdb_db_pk_id
           and fdb_db_name = 'GenBank'
           and (dblink_linked_recid like 'ZDB-GENE%' or dblink_linked_recid like '%RNAG%');";

     $numGenesGenBankBefore = ZFINPerlModules->countData($sql);

}

sub readZfinGeneInfoFile {
    #--------------------------------------------------------------------------------------------
    # Step 4: Parse zf_gene_info file to get the NCBI records with gene Id, symbol, and Vega Id
    #         Since zf_gene_info file no loger has Vega IDs, have to parse gene2vega instead
    #--------------------------------------------------------------------------------------------
    # Global: $ctVegaIdsNCBI
    # Global: %NCBIgeneWithMultipleVega
    # Global: %NCBIidsGeneSymbols
    # Global: %geneSymbolsNCBIids
    # Global: %vegaIdsNCBIids
    # Global: %vegaIdwithMultipleNCBIids
    my $ctlines;

    # a hash to store Vega Gene Id and the ONLY related NCBI gene Id
    # key: Vega Gene Id
    # value: NCBI Gene Id

    %vegaIdsNCBIids = ();

    # a hash to store NCBI Gene Id and the corresponding NCBI gene symbol; will be used in reports
    # key: NCBI Gene Id
    # value: NCBI Gene Symbol

    %NCBIidsGeneSymbols = ();

    # a hash to store Vega Gene Ids and the multiple related NCBI gene Ids
    # key: Vega Gene Id
    # value: reference to an array of NCBI Gene Ids

    %vegaIdwithMultipleNCBIids = ();

    # a hash to store NCBI Gene Ids and the multiple related Vega gene Ids
    # key: NCBI Gene Id
    # value: the parsed field of dbXrefs containing multiple Vega Gene Ids

    %NCBIgeneWithMultipleVega = ();

    $ctVegaIdsNCBI = 0;
    $ctlines = 0;

    open(ZFGENEINFO, "cat zf_gene_info.gz | gunzip -c |") || die("Cannot open zf_gene_info : $!\n");

    #Format: tax_id GeneID Symbol LocusTag Synonyms dbXrefs chromosome map_location description type_of_gene Symbol_from_nomenclature_authority Full_name_from_nomenclature_authority Nomenclature_status Other_designations Modification_date

    # Sample record:
    # 7955    30037   tnc     CH211-166O17.1  tenc|wu:fk04d02 ZFIN:ZDB-GENE-980526-104|Ensembl:ENSDARG00000021948|Vega:OTTDARG00000032698     5       -       tenascin C      protein-coding  tnc     tenascin C      O       etID309720.5|tenascin   20130529

    my @fields;
    while (<ZFGENEINFO>) {
        chomp;

        if ($_) {
            $ctlines++;

            ## the first line is just description of the fields (format, as show above), not the data
            next if $ctlines < 2;

            undef @fields;
            @fields = split("\t");

            my $taxId = $fields[0];

            ## don't process if it is not zebrafish gene
            next if $taxId ne "7955";

            my $NCBIgeneId = $fields[1];
            my $symbol = $fields[2];

            $geneSymbolsNCBIids{$symbol} = $NCBIgeneId;
            $NCBIidsGeneSymbols{$NCBIgeneId} = $symbol;

            my $synonyms = $fields[4];
            my $dbXrefs = $fields[5];
            my $chr = $fields[6];
            my $typeOfGene = $fields[9];
            my $modDate = $fields[14];

            if ($_ =~ m/Vega(.+)Vega(.+)/) {
                $NCBIgeneWithMultipleVega{$NCBIgeneId} = $dbXrefs;
                print LOG "\nMultiple Vega: \n $NCBIgeneId \t $dbXrefs \n";
                next;
            }

            # Sample dbXrefs column: ZFIN:ZDB-GENE-980526-559|Ensembl:ENSDARG00000009351|Vega:OTTDARG00000027061
            # We ignore the ZFIN Gene ZDB Id and we need the Vega Id

            if ($dbXrefs =~ m/Vega:(OTTDARG[0-9]+)/) {  ### if VEGA Id is there
                my $VegaIdNCBI = $1;
                $ctVegaIdsNCBI++;

                ## if the Vega Gene Id is found in the hash of those with multiple NCBI gene ids
                ## or, if the Vega Gene Id is found in the hash of those with on;y 1 NCBI gene id,
                ## but the corresponding NCBI gene id is not the same as the NCBI gene id of this row,
                ## it means the Vega Id here must correspond to multiple NCBI gene Ids

                if (exists($vegaIdwithMultipleNCBIids{$VegaIdNCBI}) ||
                    (exists($vegaIdsNCBIids{$VegaIdNCBI}) && $vegaIdsNCBIids{$VegaIdNCBI} ne $NCBIgeneId)) {

                    ## if the Vega Gene Id is not found in the hash of those with multiple NCBI gene ids yet,
                    ## get the corresponding NCBI gene Id from the hash of %vegaIdsNCBIids, put it and
                    ## the NCBI gene id of the current row into an anonymous array;
                    ## set the reference to this anonymous array as the value of %vegaIdwithMultipleNCBIids
                    my $ref_arrayNCBIGenes;
                    if (!exists($vegaIdwithMultipleNCBIids{$VegaIdNCBI})) {
                        my $firstNCBIgeneIdFound = $vegaIdsNCBIids{$VegaIdNCBI};
                        $ref_arrayNCBIGenes = [$firstNCBIgeneIdFound,$NCBIgeneId];
                        $vegaIdwithMultipleNCBIids{$VegaIdNCBI} = $ref_arrayNCBIGenes;
                    } else {
                        ## otherwise, get the value of %vegaIdwithMultipleNCBIids, which is a reference to an anonymous array
                        ## and push the NCBI gene Id at current row to this array

                        $ref_arrayNCBIGenes = $vegaIdwithMultipleNCBIids{$VegaIdNCBI};
                        push(@$ref_arrayNCBIGenes, $NCBIgeneId);
                    }
                }

                $vegaIdsNCBIids{$VegaIdNCBI} = $NCBIgeneId;
            }
        }

    }
    close ZFGENEINFO;

    $ctlines--;    ## because the first line is just the description of the fileds


    print LOG "\nTotal number of records on NCBI's Danio_rerio.gene_info file: $ctlines\n\n";
    print LOG "\nctVegaIdsNCBI:  $ctVegaIdsNCBI\n\n" if $ctVegaIdsNCBI > 0;

    print STATS_PRIORITY1 "\nTotal number of records on NCBI's Danio_rerio.gene_info file: $ctlines\n";
    print STATS_PRIORITY1 "\nNumber of Vega Gene Id/NCBI Gene Id pairs on Danio_rerio.gene_info file: $ctVegaIdsNCBI\n\n" if $ctVegaIdsNCBI > 0;

    if ($ctVegaIdsNCBI == 0) {
        $ctlines = $ctVegaIdsNCBI = 0;

        open(VEGAINFO, "cat gene2vega.gz | gunzip -c |") || die("Cannot open gene2vega : $!\n");

        #Format: #tax_id GeneID  Vega_gene_identifier    RNA_nucleotide_accession.version        Vega_rna_identifier     protein_accession.version       Vega_protein_identifier

        # Sample record:
        # 7955    30037   OTTDARG00000032698      NM_130907.2     OTTDART00000045738      NP_570982.2     OTTDARP00000036100

        while (<VEGAINFO>) {
            chomp;

            if ($_) {
                $ctlines++;

                ## the first line is just description of the fields (format, as show above), not the data
                next if $ctlines < 2;

                undef @fields;
                @fields = split("\t");

                my $taxId = $fields[0];

                ## don't process if it is not zebrafish gene
                next if $taxId ne "7955";

                my $NCBIgeneId = $fields[1];
                my $VegaIdNCBI = $fields[2];

                $ctVegaIdsNCBI++;
                my $ref_arrayNCBIGenes;
                ## if the Vega Gene Id is found in the hash of those with multiple NCBI gene ids
                ## or, if the Vega Gene Id is found in the hash of those with on;y 1 NCBI gene id,
                ## but the corresponding NCBI gene id is not the same as the NCBI gene id of this row,
                ## it means the Vega Id here must correspond to multiple NCBI gene Ids

                if (exists($vegaIdwithMultipleNCBIids{$VegaIdNCBI}) ||
                    (exists($vegaIdsNCBIids{$VegaIdNCBI}) && $vegaIdsNCBIids{$VegaIdNCBI} ne $NCBIgeneId)) {

                    ## if the Vega Gene Id is not found in the hash of those with multiple NCBI gene ids yet,
                    ## get the corresponding NCBI gene Id from the hash of %vegaIdsNCBIids, put it and
                    ## the NCBI gene id of the current row into an anonymous array;
                    ## set the reference to this anonymous array as the value of %vegaIdwithMultipleNCBIids

                    if (!exists($vegaIdwithMultipleNCBIids{$VegaIdNCBI})) {
                        my $firstNCBIgeneIdFound = $vegaIdsNCBIids{$VegaIdNCBI};
                        $ref_arrayNCBIGenes = [$firstNCBIgeneIdFound,$NCBIgeneId];
                        $vegaIdwithMultipleNCBIids{$VegaIdNCBI} = $ref_arrayNCBIGenes;
                    } else {
                        ## otherwise, get the value of %vegaIdwithMultipleNCBIids, which is a reference to an anonymous array
                        ## and push the NCBI gene Id at current row to this array

                        $ref_arrayNCBIGenes = $vegaIdwithMultipleNCBIids{$VegaIdNCBI};
                        push(@$ref_arrayNCBIGenes, $NCBIgeneId);
                    }
                }

                $vegaIdsNCBIids{$VegaIdNCBI} = $NCBIgeneId;
            }
        }

        close(VEGAINFO);

        $ctlines--;    ## because the first line is just the description of the fileds

        print LOG "\nTotal number of records on NCBI's gene2vega file: $ctlines\n\n";
        print LOG "\nctVegaIdsNCBI:  $ctVegaIdsNCBI\n\n" if $ctVegaIdsNCBI > 0;

        print STATS_PRIORITY1 "\nTotal number of records on NCBI's gene2vega file: $ctlines\n";
        print STATS_PRIORITY1 "\nNumber of Vega Gene Id/NCBI Gene Id pairs on gene2vega file: $ctVegaIdsNCBI\n\n" if $ctVegaIdsNCBI > 0;
    }

    if($ctVegaIdsNCBI > 0) {
        print STATS_PRIORITY2 "On NCBI's gene2vega file, the following Vega Ids correspond to more than 1 NCBI genes\n\n";
        print LOG "On NCBI's gene2vega file, the following Vega Ids correspond to more than 1 NCBI genes\n";

        my $ctVegaIdWithMultipleNCBIgene = scalar(keys %vegaIdwithMultipleNCBIids);
        foreach my $vega (sort keys %vegaIdwithMultipleNCBIids) {
            my $ref_arrayNCBIGenes = $vegaIdwithMultipleNCBIids{$vega};
            print LOG "$vega @$ref_arrayNCBIGenes\n";
            print STATS_PRIORITY2 "$vega @$ref_arrayNCBIGenes\n";
        }

        print LOG "\nctVegaIdWithMultipleNCBIgene = $ctVegaIdWithMultipleNCBIgene\n\n";
    }

    ##-------------------------------------------------------------------------------------------
    ## Get and store ZFIN gene zdb id and symbol
    ## The ZFIN gene symbols will be looked up and printed in various reports

    # key: gene zdb id
    # value: gene symbol at ZFIN

    %geneZDBidsSymbols = ();

    my $sqlGeneZDBidsSymbols = "select mrkr_zdb_id, mrkr_abbrev from marker where (mrkr_zdb_id like 'ZDB-GENE%' or mrkr_zdb_id like '%RNAG%') and mrkr_abbrev not like 'WITHDRAWN%';";

    my $curGeneZDBidsSymbols = $handle->prepare($sqlGeneZDBidsSymbols);
    my $zdbId;
    my $symbol;

    $curGeneZDBidsSymbols->execute();
    $curGeneZDBidsSymbols->bind_columns(\$zdbId,\$symbol);

    while ($curGeneZDBidsSymbols->fetch()) {
        $geneZDBidsSymbols{$zdbId} = $symbol;
    }

    $curGeneZDBidsSymbols->finish();

    # Global: $ctVegaIdsNCBI
    # Global: %NCBIgeneWithMultipleVega
    # Global: %NCBIidsGeneSymbols
    # Global: %geneSymbolsNCBIids
    # Global: %vegaIdsNCBIids
    # Global: %vegaIdwithMultipleNCBIids
    my $debugHash = {
        ctVegaIdsNCBI             => $ctVegaIdsNCBI,
        NCBIgeneWithMultipleVega  => \%NCBIgeneWithMultipleVega,
        NCBIidsGeneSymbols        => \%NCBIidsGeneSymbols,
        geneSymbolsNCBIids        => \%geneSymbolsNCBIids,
        vegaIdsNCBIids            => \%vegaIdsNCBIids,
        vegaIdwithMultipleNCBIids => \%vegaIdwithMultipleNCBIids
    };

    my $debugFile = "debug_readZfinGeneInfoFile";
    open(my $debugOutputFile, ">$debugFile") || die("Cannot open  : $!\n");
    print $debugOutputFile encode_json($debugHash);
    close($debugOutputFile);
}

sub initializeSetsOfZfinRecordsPart1 {
    #----------------------------------------------------------------------------------------------------------------------
    # Step 5: Map ZFIN gene records to NCBI gene records based on GenBank RNA sequences
    #----------------------------------------------------------------------------------------------------------------------

    #-----------------------------------------
    # Step 5-1: initial set of ZFIN records
    #-----------------------------------------
    #
    # Global: %supportedGeneZFIN
    # Global: %supportingAccZFIN
    # Global: $debug
    # Global: %accZFINsupportingMoreThan1
    # Global: %geneZFINwithAccSupportingMoreThan1
    # Global: %accZFINsupportingOnly1

    ## %supportedGeneZFIN is a hash to store references to arrays of GenBank RNA accession(s) supporting ZDB gene Id
    ## key:    zdb gene id
    ## value:  reference to the array of accession(s) that support the gene
    ## example1:  $supportedGeneZFIN{$zdbGeneId1} = [$acc1, $acc2]
    ## example2:  $supportedGeneZFIN{$zdbGeneId2} = [$acc3, $acc4, $acc5]
    %supportedGeneZFIN = ();

    ## %supportingAccZFIN is a hash to store references to arrays of ZDB gene Ids supported by GenBank accession
    ## key:    GenBank RNA accession
    ## value:  reference to the array of zdb gene id(s) that is supported by the GenBank RNA accession
    ## example1:  $supportingAccZFIN{$acc1} = [$zdbGene1]            -- potential 1:1 if same on NCBI end
    ## example2:  $supportingAccZFIN{$acc2} = [$zdbGene2, $zdbGene3] -- 1 acc supporting 2 genes, which won't be used as evidence in mapping
    %supportingAccZFIN = ();

    my $ref_arrayGenes;
    my $ref_arrayAccs;

    ## Use the output from the SQL query in prepareNCBIgeneLoad.sql to create the initial mapping set at ZFIN
    ## between GenBank RNA accessions and ZFIN gene records
    open (ZFINGENESUPPORTED, "toMap.unl") ||  die "Cannot open toMap.unl : $!\n";
    my $ctSupportedZFINgenes = 0;
    my %zfinGenes = ();
    while (<ZFINGENESUPPORTED>) {
        if ($_) {
            $ctSupportedZFINgenes++;
            chop;

            #split pairs to gene and acc by |
            my ($geneZDBid, $acc) = split(/\|/);
            $zfinGenes{$geneZDBid} = 1;

            ## if the array of ZDB gene Ids supported by this GenBank RNA accession has not been created yet
            push(@{$supportingAccZFIN{$acc} //= []}, $geneZDBid);

            ## if the array of the GenBank RNA accession(s) supporting the ZDB gene Id has not been created yet
            push(@{$supportedGeneZFIN{$geneZDBid} //= []}, $acc);
        }
    }
    close ZFINGENESUPPORTED;

    print LOG "ctSupportedZFINgenes::: $ctSupportedZFINgenes\n\n";
    print LOG "Total number of ZFIN gene records supported by GenBank RNA: $ctSupportedZFINgenes\n\n";

    writeHashOfArraysToFileForDebug("debug1", \%supportedGeneZFIN);
    writeHashOfArraysToFileForDebug("debug2", \%supportingAccZFIN);
}

sub initializeSetsOfZfinRecordsPart2 {
    #-----------------------------------------------------------------------------------------------------------------------------------
    #   Get the real initial mapping set at ZFIN end

    ## %accZFINsupportingMoreThan1 is a hash storing the references to array of genes for GenBank RNA accessions that supports more than 1 genes at ZFIN
    ## key:   GenBank RNA accession that supports more than 1 genes at ZFIN
    ## value: reference to array of more than 1 zdb gene ids supported by the GenBank RNA accession
    ## example: $accZFINsupportingMoreThan1{$acc1} = [$zdbGeneId1, $zdbGeneId2]
    ## %accZFINsupportingMoreThan1 is a subset of %supportingAccZFIN

    %accZFINsupportingMoreThan1 = ();

    ## %geneZFINwithAccSupportingMoreThan1 is a hash storing the references to array of GenBank RNA accessions for genes
    ## that are supported by accession(s) at ZFIN with at least 1 of them supporting more than 1 genes
    ## key:    gene zdb id of the gene with at least 1 supporting GenBank RNA accessions supporting more than 1 genes
    ## value:  reference to array of GenBank RNA accessions supporting the gene, at least 1 of which supports other gene(s)
    ## example:  $geneZFINwithAccSupportingMoreThan1{$zdbGeneId1} = [$acc1, $acc2, $acc3]  (at least 1 of the 3 accs supporting more than 1 genes)
    ## %geneZFINwithAccSupportingMoreThan1 is a subset of %supportedGeneZFIN

    %geneZFINwithAccSupportingMoreThan1 = ();

    ## %accZFINsupportingOnly1 is a hash storing the genes at ZFIN supported by the GenBank RNA accession that does NOT support another gene
    ## key:    GenBank RNA accession
    ## value:  gene zdb id of the gene
    ## example:  $accZFINsupportingOnly1{$acc1} = zdbGeneId1

    %accZFINsupportingOnly1 = ();

    ##-------------------------------------------------------------------------------------------------------------------------------------
    ## traverse the hash of %supportingAccZFIN to find and mark those GenBank RNA accessions stored at ZFIN supporting more than 1 genes
    ## also mark those accs that support only 1 gene at ZFIN
    ## also mark those genes supported by 2 or more GenBank RNA accessions
    ##-------------------------------------------------------------------------------------------------------------------------------------

    my $ctAllSupportingAccZFIN = 0;
    my $ctAccZFINSupportingMoreThan1 = 0;
    my $ctAccZFINSupportingOnly1 = 0;
    my $ctGenesZFINwithAccSupportingMoreThan1 = 0;

    foreach my $acc (keys %supportingAccZFIN) {
        $ctAllSupportingAccZFIN++;
        my $ref_arrayOfGenes = $supportingAccZFIN{$acc};
        my @zdbGeneIDs = @$ref_arrayOfGenes;

        if (@zdbGeneIDs > 1) {
            $ctAccZFINSupportingMoreThan1++;
            $accZFINsupportingMoreThan1{$acc} = \@zdbGeneIDs;

            foreach my $gene (@zdbGeneIDs) {
                $geneZFINwithAccSupportingMoreThan1{$gene} = $supportedGeneZFIN{$gene};
            }
        } else {  # Only supports one gene
            $ctAccZFINSupportingOnly1++;
            $accZFINsupportingOnly1{$acc} = $zdbGeneIDs[0];
        }
    }

    print STATS_PRIORITY2 "\n\nThe following GenBank RNA accessions found at ZFIN are associated with multiple ZFIN genes.";
    print STATS_PRIORITY2 "\nThe ZDB Gene Ids associated with these GenBank RNAs are excluded from mapping and hence the loading.\n\n";

    my $ctGenBankRNAsupportingMultipleZFINgenes = scalar(keys %accZFINsupportingMoreThan1);
    foreach my $accSupportingMoreThan1 (sort keys %accZFINsupportingMoreThan1) {
        my $ref_accSupportingMoreThan1 = $accZFINsupportingMoreThan1{$accSupportingMoreThan1};
        print STATS_PRIORITY2 "$accSupportingMoreThan1\t@$ref_accSupportingMoreThan1\n";
    }
    print STATS_PRIORITY2 "\nTotal: $ctGenBankRNAsupportingMultipleZFINgenes\n\n";

    $ctGenesZFINwithAccSupportingMoreThan1 = scalar(keys %geneZFINwithAccSupportingMoreThan1);

    if ($debug) {
        writeHashOfArraysToFileForDebug("debug3", \%accZFINsupportingMoreThan1);
        writeHashOfArraysToFileForDebug("debug4", \%geneZFINwithAccSupportingMoreThan1);
    }

    print LOG "\nThe following should add up \nctAccZFINSupportingOnly1 + ctAccZFINSupportingMoreThan1 = ctAllSupportingAccZFIN \nOtherwise there is bug.\n";
    print LOG "$ctAccZFINSupportingOnly1 + $ctAccZFINSupportingMoreThan1 = $ctAllSupportingAccZFIN\n\n";

    ## if the numbers don't add up, stop the whole process
    if ($ctAccZFINSupportingOnly1 + $ctAccZFINSupportingMoreThan1 != $ctAllSupportingAccZFIN) {
        close STATS_PRIORITY2;
        my $subjectLine = "Auto from $instance: " . "NCBI_gene_load.pl :: some numbers don't add up";
        reportErrAndExit($subjectLine);
    }

    print LOG "ctGenesZFINwithAccSupportingMoreThan1 = $ctGenesZFINwithAccSupportingMoreThan1\n\n";
}

sub initializeSequenceLengthHash {

    # use the following hash to store db_link sequence accession length
    # key: seqence accession
    # value: length
    # Global: %sequenceLength

    %sequenceLength = ();

    #---------------------- 1) store the dblink_length of GenBank accessions -----------------------

    my $sqlGenBankAccessionLength = "select dblink_acc_num, dblink_length
                                from db_link
                               where dblink_length is not null
                                 and (dblink_linked_recid like 'ZDB-GENE%' or dblink_linked_recid like '%RNAG%')
                                 and dblink_fdbcont_zdb_id in ('$fdcontGenBankRNA','$fdcontGenPept','$fdcontGenBankDNA');";


    my $curGenBankAccessionLength = $handle->prepare($sqlGenBankAccessionLength);

    $curGenBankAccessionLength->execute;

    my ($GenBankAcc,$seqLength);
    $curGenBankAccessionLength->bind_columns(\$GenBankAcc,\$seqLength);

    my $ctGenBankSeqLengthAtZFIN = 0;
    while ($curGenBankAccessionLength->fetch) {
        $ctGenBankSeqLengthAtZFIN++;
        $sequenceLength{$GenBankAcc} = $seqLength;
    }

    $curGenBankAccessionLength->finish();

    print LOG "\nctGenBankSeqLengthAtZFIN = $ctGenBankSeqLengthAtZFIN\n\n";
}

sub parseRefSeqCatalogFileForSequenceLength {
    # Global: %sequenceLength
    my $ctRefSeqLengthFromCatalog = 0;
    my @fields;
    my $refSeqLength;
    my $taxId;
    my $refSeqAcc;

    open(REFSEQCATALOG, "cat RefSeqCatalog.gz | gunzip -c | grep 7955 |") || die("Cannot open RefSeqCatalog.gz : $!\n");

    ## Sample record (last column is length of the sequence):
    ## 7955    Danio rerio     NP_001001398.2  89191828        complete|vertebrate_other       PROVISIONAL     205

    while (<REFSEQCATALOG>) {
        chomp;

        if ($_) {

            undef @fields;
            undef $refSeqLength;
            @fields = split("\t");

            $taxId = $fields[0];

            ## don't process if it is not zebrafish gene
            next if $taxId ne "7955";

            $refSeqAcc = $fields[2];
            $refSeqAcc =~ s/\.\d+//;     # truncate version number

            $refSeqLength = $fields[6];

            if ($refSeqLength) {
                $ctRefSeqLengthFromCatalog++;
                $sequenceLength{$refSeqAcc} = $refSeqLength
            }

        }

    }

    print LOG "\nctRefSeqLengthFromCatalog = $ctRefSeqLengthFromCatalog\n\n";

    close REFSEQCATALOG;
}

sub printSequenceLengthsCount {
    # Global: %sequenceLength
    my $ctAccWithLength = scalar(keys %sequenceLength);
    print LOG "\nctAccWithLength = $ctAccWithLength";
}

sub parseGene2AccessionFile {
    # globals:
    # $ctNoLength
    # $ctNoLengthRefSeq
    # $ctZebrafishGene2accession
    # $ctlines
    # %GenBankDNAncbiGeneIds
    # %GenPeptNCBIgeneIds
    # %RefPeptNCBIgeneIds
    # %RefSeqDNAncbiGeneIds
    # %RefSeqRNAncbiGeneIds
    # %noLength
    # %supportedGeneNCBI
    # %supportingAccNCBI
    
    #------------------------------------------------------------------------------------------------------------------------------------
    # Step 5-3: initial set of NCBI records
    #
    # This section of code parses the NCBI's gene2accession file and
    # 1) store GenBank RNA accessions as supporting RNA evidence used for mapping
    # 2) store GenPept, GenBank DNA, RefSeq RNA, RefPept, and RefSeq DNA accessions as well
    # 3) look up length for all these accessions; if not found, put them in the hash %noLength
    #------------------------------------------------------------------------------------------------------------------------------------

    ## %supportedGeneNCBI is a hash to store references to arrays of GenBank RNA accessions supporting one NCBI zebrafish gene
    ## key:    NCBI zebrafish gene id
    ## value:  reference to the array of accession(s) that support the gene
    ## example1:  $supportedGeneNCBI{$ncbiGeneId1} = [$acc1, $acc2]
    ## example2:  $supportedGeneNCBI{$ncbiGeneId2} = [$acc3, $acc4, $acc5]

    %supportedGeneNCBI = ();

    ## %supportingAccNCBI is a hash to store references to arrays of NCBI zf gene Ids supported by one GenBank accession
    ## key:    GenBank RNA accession
    ## value:  reference to the array of NCBI zf gene id(s) that is supported by the GenBank RNA accession
    ## example1:  $supportingAccNCBI{$acc1} = [$ncbiGeneId1]               -- potential 1:1 if same on ZFIN end
    ## example2:  $supportingAccNCBI{$acc2} = [$ncbiGeneId2, $ncbiGeneId3] -- 1 acc supporting 2 genes, which won't be used as evidence in mapping

    %supportingAccNCBI = ();

    # Use the following hashes to store all kinds of RefSeq and GenBank accessions on gene2accession file,
    # except for GenBank RNA accessions, which are stored in the hash, %supportingAccNCBI, documented above.

    # key: sequence accession
    # value: NCBI gene Id

    %GenPeptNCBIgeneIds = ();
    %GenBankDNAncbiGeneIds = ();
    %RefSeqRNAncbiGeneIds = ();
    %RefPeptNCBIgeneIds = ();
    %RefSeqDNAncbiGeneIds = ();

    # Use the following hash to store those sequence accessions with which length value could not be found in the hash, %sequenceLength
    # key: sequence accession
    # value: NCBI gene Id

    %noLength = ();
    $ctNoLength = 0;
    $ctNoLengthRefSeq = 0;
    $ctZebrafishGene2accession = 0;
    my $ctlines = 0;
    my @fields;
    my $taxId;
    my $NCBIgeneId;
    my $status;
    my $GenBankRNAaccNCBI;
    my $ref_arrayAccs;
    my $ref_arrayGenes;
    my $GenPeptAcc;
    my $GenBankDNAacc;
    my $RefSeqRNAacc;
    my $RefPeptAcc;
    my $RefSeqDNAacc;

    print LOG "\nParsing NCBI gene2accession file at " . strftime("%Y-%m-%d %H:%M:%S", localtime(time())) . " \n";

    open(GENE2ACC, "cat gene2accession.gz | gunzip -c | grep '^7955' |") || die("Cannot open gene2accession.gz : $!\n");

    ##Format: tax_id GeneID status RNA_nucleotide_accession.version RNA_nucleotide_gi protein_accession.version protein_gi genomic_nucleotide_accession.version genomic_nucleotide_gi start_position_on_the_genomic_accession end_position_on_the_genomic_accession orientation assembly mature_peptide_accession.version mature_peptide_gi Symbol

    while (<GENE2ACC>) {
        chomp;
        if ($_) {
            $ctlines++;
            undef @fields;
            @fields = split("\t");

            $taxId = $fields[0];

            ## don't process if it is not zebrafish gene
            next if $taxId ne "7955";

            $ctZebrafishGene2accession++;
            $NCBIgeneId = $fields[1];
            $status = $fields[2];

            if ($status eq "-") {
                if (ZFINPerlModules->stringStartsWithLetter($fields[3])) {
                    $GenBankRNAaccNCBI = $fields[3];
                    $GenBankRNAaccNCBI =~ s/\.\d+//;  ## truncate version number

                    if (!exists($sequenceLength{$GenBankRNAaccNCBI})) {
                        $noLength{$GenBankRNAaccNCBI} = $NCBIgeneId;
                        $ctNoLength++;
                    }

                    ## if the array of the GenBank RNA accession(s) supporting the NCBI gene Id has not been created yet
                    $supportedGeneNCBI{$NCBIgeneId} //= [];
                    $ref_arrayAccs = $supportedGeneNCBI{$NCBIgeneId};
                    # add it only when it is not the same as the last item
                    push(@$ref_arrayAccs, $GenBankRNAaccNCBI) if isEmpty($supportedGeneNCBI{$NCBIgeneId}) || $supportedGeneNCBI{$NCBIgeneId}[-1] ne $GenBankRNAaccNCBI;

                    ## if the array of NCBI gene Ids supported by this GenBank RNA accession has not been created yet
                    $supportingAccNCBI{$GenBankRNAaccNCBI} //= [];
                    $ref_arrayGenes = $supportingAccNCBI{$GenBankRNAaccNCBI};
                    # add it only when it is not the same as the last item
                    push(@$ref_arrayGenes, $NCBIgeneId) if isEmpty($supportingAccNCBI{$GenBankRNAaccNCBI}) || $supportingAccNCBI{$GenBankRNAaccNCBI}[-1] ne $NCBIgeneId;
                }  ## ending if (stringStartsWithLetter$fields[3]))

                if (ZFINPerlModules->stringStartsWithLetter($fields[5])) {
                    $GenPeptAcc = $fields[5];
                    $GenPeptAcc =~ s/\.\d+//;
                    $GenPeptNCBIgeneIds{$GenPeptAcc} = $NCBIgeneId;

                    if (!exists($sequenceLength{$GenPeptAcc})) {
                        $noLength{$GenPeptAcc} = $NCBIgeneId;
                        $ctNoLength++;
                    }
                }

                if (ZFINPerlModules->stringStartsWithLetter($fields[7])) {
                    $GenBankDNAacc = $fields[7];
                    $GenBankDNAacc =~ s/\.\d+//;

                    #initialize the hash value to empty array if it is empty
                    $GenBankDNAncbiGeneIds{$GenBankDNAacc} //= [];
                    #push the NCBI gene id into the array
                    push(@{$GenBankDNAncbiGeneIds{$GenBankDNAacc}}, $NCBIgeneId);

                    if (!exists($sequenceLength{$GenBankDNAacc})) {
                        $noLength{$GenBankDNAacc} = $NCBIgeneId;
                        $ctNoLength++;
                    }
                }
            } else {  # there is value of "status" field for all RefSeq accessions
                if (ZFINPerlModules->stringStartsWithLetter($fields[3])) {
                    $RefSeqRNAacc = $fields[3];
                    $RefSeqRNAacc =~ s/\.\d+//;
                    $RefSeqRNAncbiGeneIds{$RefSeqRNAacc} = $NCBIgeneId if $RefSeqRNAacc =~ m/^NM/ or $RefSeqRNAacc =~ m/^XM/ or $RefSeqRNAacc =~ m/^NR/ or $RefSeqRNAacc =~ m/^XR/;

                    if (!exists($sequenceLength{$RefSeqRNAacc})) {
                        $noLength{$RefSeqRNAacc} = $NCBIgeneId;
                        $ctNoLength++;
                        $ctNoLengthRefSeq++;
                    }
                }

                if (ZFINPerlModules->stringStartsWithLetter($fields[5])) {
                    $RefPeptAcc = $fields[5];
                    $RefPeptAcc =~ s/\.\d+//;
                    $RefPeptNCBIgeneIds{$RefPeptAcc} = $NCBIgeneId;

                    if (!exists($sequenceLength{$RefPeptAcc})) {
                        $noLength{$RefPeptAcc} = $NCBIgeneId;
                        $ctNoLength++;
                        $ctNoLengthRefSeq++;
                    }
                }

                if (ZFINPerlModules->stringStartsWithLetter($fields[7])) {
                    $RefSeqDNAacc = $fields[7];
                    $RefSeqDNAacc =~ s/\.\d+//;
                    $RefSeqDNAncbiGeneIds{$RefSeqDNAacc} = $NCBIgeneId;

                    if (!exists($sequenceLength{$RefSeqDNAacc})) {
                        $noLength{$RefSeqDNAacc} = $NCBIgeneId;
                        $ctNoLength++;
                        $ctNoLengthRefSeq++;
                    }
                }
            }

        }  # ending if ($status eq "-")

    }

    close GENE2ACC;

    print LOG "\n\nNumber of lines on gene2accession file:  $ctlines\n\n";
    print LOG "\nctZebrafishGene2accession:  $ctZebrafishGene2accession\n\n";
    print LOG "\nctNoLength = $ctNoLength\nctNoLengthRefSeq = $ctNoLengthRefSeq\n\n";

    if ($debug) {
        open (DBG16, ">debug16") ||  die "Cannot open debug16 : $!\n";

        print DBG16 "\nGenBankDNAncbiGeneIds\n==================================================\n";
        foreach my $acc (keys %GenBankDNAncbiGeneIds) {print DBG16 "$acc\t" . joinElementsFromMaybeArray($GenBankDNAncbiGeneIds{$acc}) . "\n";}

        print DBG16 "\nGenPeptNCBIgeneIds\n==================================================\n";
        foreach my $acc (keys %GenPeptNCBIgeneIds) {print DBG16 "$acc\t" . joinElementsFromMaybeArray($GenPeptNCBIgeneIds{$acc}) . "\n";}

        print DBG16 "\nRefPeptNCBIgeneIds\n==================================================\n";
        foreach my $acc (keys %RefPeptNCBIgeneIds) {print DBG16 "$acc\t" . joinElementsFromMaybeArray($RefPeptNCBIgeneIds{$acc}) . "\n";}

        print DBG16 "\nRefSeqDNAncbiGeneIds\n==================================================\n";
        foreach my $acc (keys %RefSeqDNAncbiGeneIds) {print DBG16 "$acc\t" . joinElementsFromMaybeArray($RefSeqDNAncbiGeneIds{$acc}) . "\n";}

        print DBG16 "\nRefSeqRNAncbiGeneIds\n==================================================\n";
        foreach my $acc (keys %RefSeqRNAncbiGeneIds) {print DBG16 "$acc\t" . joinElementsFromMaybeArray($RefSeqRNAncbiGeneIds{$acc}) . "\n";}

        print DBG16 "\nsupportedGeneNCBI\n==================================================\n";
        foreach my $acc (keys %supportedGeneNCBI) {print DBG16 "$acc\t" . joinElementsFromMaybeArray($supportedGeneNCBI{$acc}) . "\n";}

        print DBG16 "\nsupportingAccNCBI\n==================================================\n";
        foreach my $acc (keys %supportingAccNCBI) {print DBG16 "$acc\t" . joinElementsFromMaybeArray($supportingAccNCBI{$acc}) . "\n";}

        close DBG16;
    }
    
    if ($debug) {
        my $debugHash = {
            "GenBankDNAncbiGeneIds", => \%GenBankDNAncbiGeneIds,
            "GenPeptNCBIgeneIds",    => \%GenPeptNCBIgeneIds,
            "RefPeptNCBIgeneIds",    => \%RefPeptNCBIgeneIds,
            "RefSeqDNAncbiGeneIds",  => \%RefSeqDNAncbiGeneIds,
            "RefSeqRNAncbiGeneIds",  => \%RefSeqRNAncbiGeneIds,
            "supportedGeneNCBI",     => \%supportedGeneNCBI,
            "supportingAccNCBI",     => \%supportingAccNCBI,
            "noLength",              => \%noLength
        };

        my $debugFile = "debug16.json";
        open(my $debugOutputFile, ">$debugFile") || die("Cannot open  : $!\n");
        print $debugOutputFile encode_json($debugHash);
        close($debugOutputFile);
    }

}

sub joinElementsFromMaybeArray {
    my $acc = shift;
    my $separator = shift || ";";

    if (ref($acc) eq "ARRAY") {
        return join($separator, @$acc);
    } else {
        if (ref($acc) eq "HASH") {
            my $buf = "";
            foreach my $key (keys %$acc) {
                $buf .= "$key:" . $acc->{$key} . " ";
            }
            chomp($buf);
            return $buf;
        } else {
            return $acc;
        }
    }
}

sub countNCBIGenesWithSupportingGenBankRNA {
    my $ctGeneIdsNCBIonGene2accession = scalar(keys %supportedGeneNCBI);
    writeHashOfArraysToFileForDebug("debug5", \%supportedGeneNCBI);

    print LOG "\nThe number of NCBI genes with supporting GenBank RNA: $ctGeneIdsNCBIonGene2accession\n\n";
    print STATS_PRIORITY2 "\n\nThe number of NCBI genes with supporting GenBank RNA: $ctGeneIdsNCBIonGene2accession\n\n";
}

sub logGenBankDNAncbiGeneIds {
    # Global: %GenBankDNAncbiGeneIds
    # Global: $debug


    if ($debug) {
        open(DBG5A, ">debug5a") || die "Cannot open debug5a : $!\n";

        foreach my $genBankAccession (sort keys %GenBankDNAncbiGeneIds) {
            my $refArrayNcbiGeneIds = $GenBankDNAncbiGeneIds{$genBankAccession};
            print DBG5A "$genBankAccession\t";
            my $buffer = "";
            foreach my $ncbiGeneId (@$refArrayNcbiGeneIds) {
                my $zfinGeneId = $oneToOneNCBItoZFIN{$ncbiGeneId};

                $buffer .= "$ncbiGeneId";
                $buffer .= "/$zfinGeneId" if $zfinGeneId;
                $buffer .= " ";
            }
            chomp($buffer);
            print DBG5A "$buffer\n";
        }

        close DBG5A;
    }

}

sub logSupportingAccNCBI {
    # Global: $debug
    # Global: %supportingAccNCBI
    writeHashOfArraysToFileForDebug("debug6", \%supportingAccNCBI);
}

sub initializeHashOfNCBIAccessionsSupportingMultipleGenes {
    # Globals: %accNCBIsupportingMoreThan1
    #             %accNCBIsupportingOnly1
    #             %geneNCBIwithAccSupportingMoreThan1

    #---------------------------------------------------------------------------------------------------------------------------------

    ## %accNCBIsupportingMoreThan1 is a hash storing the references to array of genes for GenBank RNA accessions that supports more than 1 genes at NCBI
    ## key:   GenBank RNA accession that supports more than 2 genes at NCBI
    ## value: reference to array of more than 1 NCBI gene ids supported by the GenBank RNA accession
    ## example: $accNCBIsupportingMoreThan1{$acc1} = [$ncbiGene1, $ncbiGene2]
    ## %accNCBIsupportingMoreThan1 is a subset of %supportingAccNCBI

    %accNCBIsupportingMoreThan1 = ();

    ## %geneNCBIwithAccSupportingMoreThan1 is a hash storing the references to array of GenBank RNA accessions for genes
    ## that are supported by accession(s) at NCBI with at least 1 of them supporting more than 1 genes
    ## key:    NCBI gene id of the gene with at least 1 supporting GenBank RNA accessions supporting more than 1 genes
    ## value:  reference to array of GenBank RNA accessions supporting the gene, at least 1 of which supports other gene(s)
    ## example:  $geneNCBIwithAccSupportingMoreThan1{$ncbiGeneId1} = [$acc1, $acc2, $acc3]  (at least 1 of the 3 accs supporting more than 1 genes)
    ## %geneNCBIwithAccSupportingMoreThan1 is a subset of %supportedGeneNCBI; can be called n's at NCBI;

    %geneNCBIwithAccSupportingMoreThan1 = ();

    ## %accNCBIsupportingOnly1 is a hash storing the genes at NCBI supported by the GenBank RNA accession that does NOT support another gene
    ## key:    GenBank RNA accession
    ## value:  NCBI gene id of the gene
    ## example:  $accNCBIsupportingOnly1{$acc1} = ncbiGeneId1

    %accNCBIsupportingOnly1 = ();

    ##------------------------------------------------------------------------------------------------------------------------------
    ## traverse the hash of %supportingAccNCBI to find and mark those GenBank RNA accessions at NCBI supporting more than 1 genes
    ## and also mark those accs that support only 1 gene at NCBI, which is vital for 1:1 mapping between NCBI and ZFIN
    ## also mark those genes supported by 2 or more GenBank RNA accessions
    ##------------------------------------------------------------------------------------------------------------------------------

    my $ctAllSupportingAccNCBI = 0;
    my $ctAccNCBISupportingMoreThan1 = 0;
    my $ctAccNCBISupportingOnly1 = 0;
    my $ref_arrayOfGenes;
    my $ref_arrayOfAccs;

    foreach my $acc (keys %supportingAccNCBI) {
        $ctAllSupportingAccNCBI++;

        $ref_arrayOfGenes = $supportingAccNCBI{$acc};

        if ($#$ref_arrayOfGenes > 0) {  ## if the last index > 0, indicating more than 1 genes supported
            $ctAccNCBISupportingMoreThan1++;
            $accNCBIsupportingMoreThan1{$acc} = $ref_arrayOfGenes;

            foreach my $genesInQuestion (@$ref_arrayOfGenes) {
                $ref_arrayOfAccs = $supportedGeneNCBI{$genesInQuestion};
                $geneNCBIwithAccSupportingMoreThan1{$genesInQuestion} = $ref_arrayOfAccs;
            }
        } else {  ## the acc only supports 1 gene

            $ctAccNCBISupportingOnly1++;

            foreach my $geneWithAccSupportingOnly1 (@$ref_arrayOfGenes) { ## only 1 element in the array
                $accNCBIsupportingOnly1{$acc} = $geneWithAccSupportingOnly1;
            }
        }

    }

    print STATS_PRIORITY2 "\nThe following GenBank accession found on NCBI's gene2accession file support more than 1 NCBI genes\n";
    print LOG "\nThe following GenBank accession found on NCBI's gene2accession file support more than 1 NCBI genes\n";

    foreach my $accSupportingMoreThan1 (sort keys %accNCBIsupportingMoreThan1) {
        my $ref_accSupportingMoreThan1 = $accNCBIsupportingMoreThan1{$accSupportingMoreThan1};
        print STATS_PRIORITY2 "$accSupportingMoreThan1\t@$ref_accSupportingMoreThan1\n";
        print LOG "$accSupportingMoreThan1\t@$ref_accSupportingMoreThan1\n";
    }

    print STATS_PRIORITY2 "\nThe following NCBI's Gene Ids have at least 1 supporting GenBank accession that supports more than 1 NCBI genes\n";
    print LOG "\nThe following NCBI's Gene Ids have at least 1 supporting GenBank accession that supports more than 1 NCBI genes\n";

    foreach my $geneWithAtLeast1accSupportingMoreThan1 (sort keys %geneNCBIwithAccSupportingMoreThan1) {
        my $ref_accs = $geneNCBIwithAccSupportingMoreThan1{$geneWithAtLeast1accSupportingMoreThan1};
        print LOG "$geneWithAtLeast1accSupportingMoreThan1\t@$ref_accs\n";
        print STATS_PRIORITY2 "$geneWithAtLeast1accSupportingMoreThan1\t@$ref_accs\n";
    }

    print LOG "\nThe following should add up \nctAccNCBISupportingOnly1 + ctAccNCBISupportingMoreThan1 = ctAllSupportingAccNCBI \nOtherwise there is bug.\n";
    print LOG "$ctAccNCBISupportingOnly1 + $ctAccNCBISupportingMoreThan1 = $ctAllSupportingAccNCBI\n\n";

    ## if the numbers don't add up, stop the whole process
    if ($ctAccNCBISupportingOnly1 + $ctAccNCBISupportingMoreThan1 != $ctAllSupportingAccNCBI) {
        close STATS_PRIORITY2;
        my $subjectLine = "Auto from $instance: " . "NCBI_gene_load.pl :: some numbers don't add up";
        reportErrAndExit($subjectLine);
    }
}

sub initializeMapOfZfinToNCBIgeneIds {
    #--------------------------------------------------------------------------
    # Step 5-4: get 1:1, 1:N and 1:0 (from ZFIN to NCBI) lists
    #
    # pass 1 of the mapping: one-way mapping of ZFIN genes onto NCBI genes
    #--------------------------------------------------------------------------

    # Globals:
    #     %oneToNZFINtoNCBI
    #     %oneToOneZFINtoNCBI
    #     %genesZFINwithNoRNAFoundAtNCBI


    ## %oneToNZFINtoNCBI is the hash to store the 1:n one-way mapping result of ZDB gene zdb id onto NCBI gene zdb id
    ## %oneToNZFINtoNCBI include those 1:N (ZFIN to NCBI) and N:N (ZFIN to NCBI)
    ## key:    zdb gene Id
    ## value:  referec to the hash of 2 or more NCBI gene Ids that are mapped to zdb gene id
    ## example: $oneToNZFINtoNCBI{$zdbGeneId1} = \%mappedNCBIgeneIdsSet1

    %oneToNZFINtoNCBI = ();

    ## %oneToOneZFINtoNCBI is the hash to store the 1:1 one-way mapping result of NCBI gene zdb id onto ZFIN gene zdb id
    ## key:    zdb gene Id
    ## value:  NCBI gene Id that is mapped to the ZDB gene id and not mapped to another ZDB gene id
    ## example: $oneToOneZFINtoNCBI{$zdbGeneId1} = $NCBIgeneId1

    %oneToOneZFINtoNCBI = ();

    ## %genesZFINwithNoRNAFoundAtNCBI is the hash to store the ZFIN genes with supporting accessions all of which are not found at NCBI's gene2accession file
    ## key:    zdb gene Id
    ## value:  reference to the array of accession(s) that support the gene at ZFIN but not found at NCBI's gene2accession file
    ## example: $genesZFINwithNoRNAFoundAtNCBI{$zdbGeneId1} = {acc1, acc2}

    %genesZFINwithNoRNAFoundAtNCBI = ();

    ## doing the mapping of ZDB Gene Id to NCBI Gene Id based on the data in the following 3 hashes established before
    ## 1) %supportedGeneZFIN                     -- ZFIN genes that are supported by GenBank RNA accessions
    ## 2) %geneZFINwithAccSupportingMoreThan1    -- RNA-supported ZFIN genes having at least 1 RNA accession that supports other ZFIN gene
    ## those in 1) but not in 2) get processed
    ## 3) %accNCBIsupportingOnly1                -- GenBank accessions/NCBI gene Id pairs


    my $ct1to1ZFINtoNCBI = 0;
    my $ct1toNZFINtoNCBI = 0;
    my $ctProcessedZFINgenes = 0;
    my $ctZFINgenesSupported = 0;
    my $ctZFINgenesWithAllAccsNotFoundAtNCBI = 0;
    my %NCBIgeneIdsSaved;
    my $ref_mappedNCBIgeneIds;


    foreach my $zfinGene (keys %supportedGeneZFIN) {
        $ctZFINgenesSupported++;

        ## those genes with even just 1 supporting RNA sequence that supports another gene won't be processed
        if (!exists($geneZFINwithAccSupportingMoreThan1{$zfinGene})) {

            $ctProcessedZFINgenes++;

            ## refence to the array of supporting GenBank RNA accessions
            my $ref_arrayOfAccs = $supportedGeneZFIN{$zfinGene};

            my $ctAccsForGene = 0;
            my $mapped1to1 = 1;
            my $firstMappedNCBIgeneIdSaved = "None";

            my $ct = 0;
            foreach my $acc (@$ref_arrayOfAccs) {
                $ct++;

                ## only map ZFIN genes to NCBI genes that are with supporting RNA accessions supporting only 1 NCBI gene
                ## may have supporting acc at ZFIN that is not found at NCBI or not supporting any gene at NCBI; do nothing in such cases

                if (exists($accNCBIsupportingOnly1{$acc}) && !exists($accNCBIsupportingMoreThan1{$acc})) {

                    $ctAccsForGene++;
                    my $NCBIgeneId = $accNCBIsupportingOnly1{$acc};  ## this is the NCBI gene Id that is mapped to ZDB gene Id based on the common RNA acc
                    if ($ctAccsForGene == 1) {   # first acc in the supporting acc list for ZFIN gene which is also found at NCBI
                        $firstMappedNCBIgeneIdSaved = $NCBIgeneId;
                        $ref_mappedNCBIgeneIds = {$firstMappedNCBIgeneIdSaved => $acc};  ## anonymous hash to be put as value in an outer hash
                        %NCBIgeneIdsSaved = %$ref_mappedNCBIgeneIds;                ## to be looked up to avoid redundant NCBI gene id
                    } else {
                        if (!exists($NCBIgeneIdsSaved{$NCBIgeneId})) {  ## if the gene is not found in the save hash, it means mapped to another NCBI gene
                            ## do nothing if it is found in the save hash
                            $mapped1to1 = 0;
                            $NCBIgeneIdsSaved{$NCBIgeneId} = $acc;         ## add it to the save hash
                            $ref_mappedNCBIgeneIds->{$NCBIgeneId} = $acc;   ## add it to the hash for mapped NCBI gene ids
                        }
                    }

                }

            }  # of foreach $acc (@$ref_arrayOfAccs)

            if ($mapped1to1 == 1 && $firstMappedNCBIgeneIdSaved ne "None") {
                $oneToOneZFINtoNCBI{$zfinGene} = $firstMappedNCBIgeneIdSaved;
                $ct1to1ZFINtoNCBI++;
            }

            if ($mapped1to1 == 0) {
                $ct1toNZFINtoNCBI++;
                $oneToNZFINtoNCBI{$zfinGene} = $ref_mappedNCBIgeneIds;
            }

            if ($mapped1to1 == 1 && $firstMappedNCBIgeneIdSaved eq "None") {
                $ctZFINgenesWithAllAccsNotFoundAtNCBI++;
                $genesZFINwithNoRNAFoundAtNCBI{$zfinGene} = $ref_arrayOfAccs;
            }
        }

    }    # end of foreach $zfinGene (keys %supportedGeneZFIN)

    writeHashOfArraysToFileForDebug("debug9", \%oneToOneZFINtoNCBI);
    writeHashOfArraysToFileForDebug("debug10", \%oneToNZFINtoNCBI);

    print LOG "\nctZFINgenesSupported = $ctZFINgenesSupported \nctProcessedZFINgenes = $ctProcessedZFINgenes\n\n";
    print LOG "\nct1to1ZFINtoNCBI = $ct1to1ZFINtoNCBI \n ct1toNZFINtoNCBI = $ct1toNZFINtoNCBI\n\n";

    print LOG "\nThe following should add up \nct1to1ZFINtoNCBI + ct1toNZFINtoNCBI + ctZFINgenesWithAllAccsNotFoundAtNCBI = ctProcessedZFINgenes \nOtherwise there is bug.\n";
    print LOG "$ct1to1ZFINtoNCBI + $ct1toNZFINtoNCBI + $ctZFINgenesWithAllAccsNotFoundAtNCBI = $ctProcessedZFINgenes\n\n";

    ## if the numbers don't add up, stop the whole process
    if ($ct1to1ZFINtoNCBI + $ct1toNZFINtoNCBI + $ctZFINgenesWithAllAccsNotFoundAtNCBI != $ctProcessedZFINgenes) {
        close STATS_PRIORITY2;
        my $subjectLine = "Auto from $instance: " . "NCBI_gene_load.pl :: some numbers don't add up";
        reportErrAndExit($subjectLine);
    }
}

sub logOneToZeroAssociations {
    # Global: %genesZFINwithNoRNAFoundAtNCBI
    open (ONETOZERO, ">reportOneToZero") ||  die "Cannot open ONETOZERO : $!\n" if $debug;
    print ONETOZERO getArtifactComparisonURLs();

    my $ctOneToZero = 0;
    foreach my $geneAtZFIN (sort keys %genesZFINwithNoRNAFoundAtNCBI) {
        my $ref_arrayAccsNotFoundAtNCBI = $genesZFINwithNoRNAFoundAtNCBI{$geneAtZFIN} if $debug;
        print ONETOZERO "$geneAtZFIN\t@$ref_arrayAccsNotFoundAtNCBI \n" if $debug;
        $ctOneToZero++;
    }

    close ONETOZERO if $debug;

    print LOG "\nctOneToZero = $ctOneToZero\n\n";

    print STATS_PRIORITY2 "\nMapping result statistics: number of 1:0 (ZFIN to NCBI) - $ctOneToZero\n\n";
}

sub oneWayMappingNCBItoZfinGenes {
    #--------------------------------------------------------------------------------
    # Step 5-5: get 1:1, 1:N and 1:0 (from NCBI to ZFIN) lists
    #
    # pass 2 of the mapping: one-way mapping of NCBI genes onto ZFIN genes
    #--------------------------------------------------------------------------------

    # Global: %oneToOneNCBItoZFIN
    #         %oneToNNCBItoZFIN
    #         %supportedGeneNCBI

    ## %oneToNNCBItoZFIN is the hash to store the 1:n one-way mapping result of NCBI gene zdb id onto ZFIN gene zdb id
    ## key:    NCBI gene Id
    ## value:  referec to the hash of 2 or more ZDB gene Ids that are mapped to NCBI gene id
    ## example: $oneToNNCBItoZFIN{$ncbiGeneId1} = \%mappedZDBgeneIdsSet1

    %oneToNNCBItoZFIN = ();

    ## %oneToOneNCBItoZFIN is the hash to store the 1:1 one-way mapping results of NCBI gene zdb id onto ZFIN gene zdb id
    ## %oneToOneNCBItoZFIN include those 1:N (NCBI to ZFIN) and N:N (NCBI to ZFIN)
    ## key:    NCBI gene Id
    ## value:  ZDB gene Id that is mapped to the NCBI gene id and not mapped to another NCBI gene id
    ## example: $oneToOneNCBItoZFIN{$zdbGeneId1} = $NCBIgeneId1

    %oneToOneNCBItoZFIN = ();

    ## %genesNCBIwithAllAccsNotFoundAtZFIN is the hash to store the NCBI genes with supporting accessions all of which are not found at ZFIN
    ## key:    zdb gene Id
    ## value:  reference to the array of accession(s) that support the gene at NCBI but not found at ZFIN
    ## example: $genesNCBIwithAllAccsNotFoundAtZFIN{$zdbGeneId1} = {acc1, acc2}

    my %genesNCBIwithAllAccsNotFoundAtZFIN = ();

    ## doing the mapping of NCBI Gene Id to ZFIN Gene Id based on the data in the following 3 hashes populated before
    ## 1) %supportedGeneNCBI                     -- NCBI genes that are supported by GenBank RNA accessions
    ## 2) %geneNCBIwithAccSupportingMoreThan1    -- RNA-supported NCBI genes having at least 1 RNA accession that supports other NCBI gene
    ## those in 1) but not in 2) get processed
    ## 3) %accZFINsupportingOnly1                -- GenBank accessions/ZDB gene Id pairs


    my $ct1to1NCBItoZFIN = 0;
    my $ct1toNNCBItoZFIN = 0;
    my $ctProcessedNCBIgenes = 0;
    my $ctNCBIgenesSupported = 0;
    my $ctNCBIgenesWithAllAccsNotFoundAtZFIN = 0;
    my $ref_arrayOfAccs;
    my $ref_mappedZDBgeneIds;
    my %ZDBgeneIdsSaved;

    foreach my $ncbiGene (keys %supportedGeneNCBI) {
        $ctNCBIgenesSupported++;

        ## those genes with even just 1 supporting RNA sequence that supports another gene won't be processed
        if (!exists($geneNCBIwithAccSupportingMoreThan1{$ncbiGene})) {

            $ctProcessedNCBIgenes++;

            ## refence to the array of supporting GenBank RNA accessions
            $ref_arrayOfAccs = $supportedGeneNCBI{$ncbiGene};

            my $ctAccsForGene = 0;
            my $mapped1to1 = 1;
            my $firstMappedZFINgeneIdSaved = "None";

            my $ct = 0;
            foreach my $acc (@$ref_arrayOfAccs) {
                $ct++;

                ## only map NCBI genes to ZFIN genes that are with supporting RNA accessions supporting only 1 ZFIN gene
                ## may have supporting acc at NCBI that is not found at ZFIN yet or not associated with any gene at ZFIN yet; do nothing in such cases
                if (exists($accZFINsupportingOnly1{$acc}) && !exists($accZFINsupportingMoreThan1{$acc})) {

                    $ctAccsForGene++;
                    my $ZDBgeneId = $accZFINsupportingOnly1{$acc};  ## this is the ZDB gene Id that is mapped to NCBI gene Id based on the common RNA acc

                    if ($ctAccsForGene == 1) {   # first acc in the supporting acc list for NCBI gene which is also found at ZFIN
                        $firstMappedZFINgeneIdSaved = $ZDBgeneId;
                        $ref_mappedZDBgeneIds = {$firstMappedZFINgeneIdSaved => $acc};  ## anonymous hash to be expanded and saved
                        %ZDBgeneIdsSaved = %$ref_mappedZDBgeneIds;                ## to be looked up to avoid redundant ZDB gene id
                    } else {
                        if (!exists($ZDBgeneIdsSaved{$ZDBgeneId})) {  ## if the gene is not found in the save hash, it means mapped to > 1 ZFIN genes
                            ## do nothing if it is found in the save hash
                            $mapped1to1 = 0;
                            $ZDBgeneIdsSaved{$ZDBgeneId} = $acc;         ## add it to the save hash
                            $ref_mappedZDBgeneIds->{$ZDBgeneId} = $acc;   ## add it to the hash for mapped ZFIN gene ids
                        }

                    }

                }

            }  # of foreach $acc (@$ref_arrayOfAccs)

            if ($mapped1to1 == 1 && $firstMappedZFINgeneIdSaved ne "None") {
                $oneToOneNCBItoZFIN{$ncbiGene} = $firstMappedZFINgeneIdSaved;
                $ct1to1NCBItoZFIN++;
            }

            if ($mapped1to1 == 0) {
                $ct1toNNCBItoZFIN++;
                $oneToNNCBItoZFIN{$ncbiGene} = $ref_mappedZDBgeneIds;
            }

            if ($mapped1to1 == 1 && $firstMappedZFINgeneIdSaved eq "None") {
                $ctNCBIgenesWithAllAccsNotFoundAtZFIN++;
                $genesNCBIwithAllAccsNotFoundAtZFIN{$ncbiGene} = $ref_arrayOfAccs;
            }
        }

    }    # end of foreach $ncbiGene (keys %supportedGeneNCBI)

    writeHashOfArraysToFileForDebug("debug12", \%oneToOneNCBItoZFIN);
    writeHashOfArraysToFileForDebug("debug13", \%oneToNNCBItoZFIN);


    my $ctzeroToOne = scalar(keys %genesNCBIwithAllAccsNotFoundAtZFIN);
    writeHashOfArraysToFileForDebug("debug14", \%genesNCBIwithAllAccsNotFoundAtZFIN);

    print LOG "\nctzeroToOne = $ctzeroToOne\n\n";

    print LOG "\nctNCBIgenesSupported = $ctNCBIgenesSupported \nctProcessedNCBIgenes = $ctProcessedNCBIgenes\n\n";
    print LOG "\nct1to1NCBItoZFIN = $ct1to1NCBItoZFIN \n ct1toNNCBItoZFIN = $ct1toNNCBItoZFIN\n\n";

    print LOG "\nThe following should add up \nct1to1NCBItoZFIN + ct1toNNCBItoZFIN + ctNCBIgenesWithAllAccsNotFoundAtZFIN = ctProcessedNCBIgenes \nOtherwise there is bug.\n";
    print LOG "$ct1to1NCBItoZFIN + $ct1toNNCBItoZFIN + $ctNCBIgenesWithAllAccsNotFoundAtZFIN = $ctProcessedNCBIgenes\n\n";

    ## if the numbers don't add up, stop the whole process
    if ($ct1to1NCBItoZFIN + $ct1toNNCBItoZFIN + $ctNCBIgenesWithAllAccsNotFoundAtZFIN != $ctProcessedNCBIgenes) {
        close STATS_PRIORITY2;
        my $subjectLine = "Auto from $instance: " . "NCBI_gene_load.pl :: some numbers don't add up";
        reportErrAndExit($subjectLine);
    }

    print STATS_PRIORITY2 "\nMapping result statistics: number of 0:1 (ZFIN to NCBI) - $ctzeroToOne\n\n";
}

sub prepare2WayMappingResults {
    #------------------------------------------------------------------------------------------------------------------------
    # Step 5-6: compare the 2-way mapping results and get the final 1:1, 1:N, N:1, and N:N lists
    #
    # pass 3 of the mapping: compare the results of both of the one-way mappings and make the final 1:1, 1:N, N:1, N:N lists
    #------------------------------------------------------------------------------------------------------------------------
    # Globals:
    #   %oneToOneZFINtoNCBI
    #   %mapped
    #   %mappedReversed
    #   $ctOneToOneNCBI

    %mapped = ();  ## the list of 1:1; key: ZDB gene Id; value: NCBI gene Id
    %mappedReversed = ();  ## the list of 1:1; key: NCBI gene Id; value: ZDB gene Id
    $ctOneToOneNCBI = 0;

    my $ctAllpotentialOneToOneZFIN = 0;
    my $ctOneToOneZFIN = 0;
    my $ncbiId;

    foreach my $zdbid (keys %oneToOneZFINtoNCBI) {
        $ctAllpotentialOneToOneZFIN++;
        $ncbiId = $oneToOneZFINtoNCBI{$zdbid};
        if (exists($oneToOneNCBItoZFIN{$ncbiId})) {
            $ctOneToOneZFIN++;
            $mapped{$zdbid} = $ncbiId;
        }
    }

    print LOG "\n ctAllpotentialOneToOneZFIN = $ctAllpotentialOneToOneZFIN \n ctOneToOneZFIN = $ctOneToOneZFIN\n\n";

    my $ctAllpotentialOneToOneNCBI = 0;
    foreach $ncbiId (keys %oneToOneNCBItoZFIN) {
        $ctAllpotentialOneToOneNCBI++;
        my $zdbId = $oneToOneNCBItoZFIN{$ncbiId};
        if (exists($oneToOneZFINtoNCBI{$zdbId})) {
            $ctOneToOneNCBI++;    ## this number should be the same as $ctOneToOneZFIN
            $mappedReversed{$ncbiId} = $zdbId;
        }
    }

    if ($debug) {
        open (DBG17, ">debug17") ||  die "Cannot open debug17 : $!\n";
        print DBG17 "ZFIN to NCBI\n===================\n";
        foreach my $zdbId (sort keys %mapped) {
            print DBG17 "$zdbId\t$mapped{$zdbId}\n";
        }
        print DBG17 "\n\nNCBI to ZFIN\n===================\n";
        foreach my $ncbiId (sort keys %mappedReversed) {
            print DBG17 "$ncbiId\t$mappedReversed{$ncbiId}\n";
        }
        close DBG17;
    }

    print LOG "\n ctAllpotentialOneToOneNCBI = $ctAllpotentialOneToOneNCBI \n ctOneToOneNCBI = $ctOneToOneNCBI\n\n";
    print STATS_PRIORITY2 "\nMapping result statistics: number of 1:1 based on GenBank RNA - $ctOneToOneNCBI\n\n";
}

sub addReverseMappedGenesFromNCBItoZFINFromSupplementaryLoad {
    #
    # Run the report of genes that are mapped from NCBI to ZFIN without being mapped back from ZFIN to NCBI
    # Enable this load with LOAD_NCBI_ONE_WAY_GENES=1 environment variable
    # (ZFIN-8517)

    if ("true" ne $ENV{'LOAD_NCBI_ONE_WAY_GENES'}) {
        print LOG "Skipping the load of genes that are mapped from NCBI to ZFIN without being mapped back from ZFIN to NCBI\n";
        print LOG "Enable by setting LOAD_NCBI_ONE_WAY_GENES=true environment variable. (LOAD_NCBI_ONE_WAY_GENES='" . $ENV{'LOAD_NCBI_ONE_WAY_GENES'} . "')\n";
        return;
    }
    print LOG "Running the load of genes that are mapped from NCBI to ZFIN without being mapped back from ZFIN to NCBI\n";
    print LOG "Enabled by LOAD_NCBI_ONE_WAY_GENES=true environment variable\n";
    print LOG "More information in debug15 file\n";

    my $debugBuffer = "";
    my $file = $ENV{'SOURCEROOT'} . "/ncbi_matches_through_ensembl.csv";

    #check if environment variable is set to give a specific report file to parse so we don't have to run the gradle task
    if (!exists($ENV{'LOAD_NCBI_ONE_WAY_REPORT'}) || $ENV{'LOAD_NCBI_ONE_WAY_REPORT'} eq "") {
        my $currentDir = cwd;

        # Set the JAVA_HOME path to override the jenkins one
        $ENV{'JAVA_HOME'} = getPropertyValue("JAVA_HOME");
        if (exists($ENV{'OVERRIDE_JAVA_HOME'})) {
            print "Overriding JAVA_HOME with $ENV{'OVERRIDE_JAVA_HOME'}\n";
            $ENV{'JAVA_HOME'} = $ENV{'OVERRIDE_JAVA_HOME'};
        }

            my $cmdString = "cd " . $ENV{'SOURCEROOT'} . " ; " .
                "gradle '-DncbiFileUrl=file://$currentDir/zf_gene_info.gz' " .
                "         ncbiMatchThroughEnsemblTask ; " .
                "cd $currentDir";
            print "Executing $cmdString\n";
            print LOG "Executing $cmdString\n";
            doSystemCommand($cmdString);
    } else {
        $file = $ENV{'LOAD_NCBI_ONE_WAY_REPORT'};
        print "Skipping gradle and using provided $file through environment variable LOAD_NCBI_ONE_WAY_REPORT\n";
        print LOG "Skipping gradle and using provided $file through environment variable LOAD_NCBI_ONE_WAY_REPORT\n";
    }


    #
    # Read the results
    #
    print LOG "Reading $file\n";
    doSystemCommand("cp $file .");
    open(FILE, "<$file") or die "Can't open $file: $!\n";
    my $line = <FILE>; # skip header (ncbi_id, zdb_id, ensembl_id, symbol, dblinks, publications, rna_accessions)
    my $ncbiSupplementMapCount = 0;
    while ($line = <FILE>) {
        chomp $line;
        $line = trim($line);
        my ($ncbi_id, $zdb_id, $ensembl_id, $symbol, $dblinks, $publications, $rna_accessions) = split(/,/, $line);

        if (exists($mappedReversed{$ncbi_id}) || exists($mapped{$zdb_id})) {
            $debugBuffer .= "Skip Duplicate: $ncbi_id, $zdb_id, $ensembl_id, $symbol, $dblinks, $publications, $rna_accessions\n";
            next;
        }

        # only add the mapping if the RNA accessions are empty or the ZFIN ID is a miRNA
        # see ZFIN-8517 comments for details
        if ($rna_accessions ne "" && $zdb_id !~ /ZDB-MIRNAG-/) {
            $debugBuffer .= "Skip NON-blank NON-MIRNAG: $ncbi_id, $zdb_id, $ensembl_id, $symbol, $dblinks, $publications, $rna_accessions\n";
            next;
        }
        $debugBuffer .= "Supplemental mapping: $line\n";

        # add to the regular mapping?
        # $mapped{$zdb_id} = $ncbi_id;
        # $mappedReversed{$ncbi_id} = $zdb_id;

        # add to the supplemental mapping
        $ncbiSupplementMap{$zdb_id} = $ncbi_id;
        $ncbiSupplementMapReversed{$ncbi_id} = $zdb_id;
        $ncbiSupplementMapCount++;
    }
    close FILE;
    print LOG "ncbiSupplementMapCount = $ncbiSupplementMapCount\n";
    $debugBuffer .= "ncbiSupplementMapCount = $ncbiSupplementMapCount\n";

    if ($debug) {
        open(DBG15, ">debug15") || die "Cannot open debug15 : $!\n";
        print DBG15 "$debugBuffer\n";
        close DBG15;
    }

}

sub writeNCBIgeneIdsMappedBasedOnGenBankRNA {
    # -------- write the NCBI gene Ids mapped based on GenBank RNA accessions on toLoad.unl ------------
    # Globals:
    #  %mapped
    #  $ctToLoad
    $ctToLoad = scalar(keys %mapped);

    foreach my $zdbId (sort keys %mapped) {
        my $mappedNCBIgeneId = $mapped{$zdbId};
        print TOLOAD "$zdbId|$mappedNCBIgeneId|||$fdcontNCBIgeneId|$pubMappedbasedOnRNA\n";
    }
}

sub writeNCBIgeneIdsMappedBasedOnSupplementaryLoad {
    # -------- write the NCBI gene Ids mapped based on GenBank RNA accessions on toLoad.unl ------------
    # Globals:
    #  %ncbiSupplementMap
    #  $ctToLoad
    if ("true" ne $ENV{'LOAD_NCBI_ONE_WAY_GENES'}) {
        return;
    }

    foreach my $zdbId (sort keys %ncbiSupplementMap) {
        my $mappedNCBIgeneId = $ncbiSupplementMap{$zdbId};

        print TOLOAD "$zdbId|$mappedNCBIgeneId|||$fdcontNCBIgeneId|$pubMappedbasedOnNCBISupplement\n";

        $ctToLoad++;
    }
}

sub getOneToNNCBItoZFINgeneIds {
    #------------------------ get 1:N list and N:N from ZFIN to NCBI -----------------------------
    # Globals:
    #  %nToOne
    #  %oneToN


    # %nToOne is a hash storing NCBI gene Ids in 1 to N mapping results mapped from NCBI to ZFIN
    # 1 to N from NCBI to ZFIN is equivalent to N to 1 from ZFIN to NCBI
    # key: NCBI gene Id
    # value: reference to hash of mapped ZDB gene Ids

    %nToOne = ();

    # %oneToN is a hash storing ZDB gene Ids in 1 to N mapping results mapped from ZFIN to NCBI
    # key: ZDB gene Id
    # value: reference to hash of mapped NCBI gene Ids

    %oneToN = ();

    my $ctOneToN = 0;
    my $ctNtoNfromZFIN = 0;
    my $mappedNCBIgene;
    my $refArrayAccs;
    my $refAssociatedNCBIgenes;
    my $refAssociatedZFINgenes;
    my $mappedZFINgene;

    # report N:N
    open (NTON, ">reportNtoN") ||  die "Cannot open reportNtoN : $!\n";
    print NTON getArtifactComparisonURLs();

    foreach my $geneZFINtoMultiNCBI (sort keys %oneToNZFINtoNCBI) {

        # %zdbIdsOfNtoN is a hash storing ZDB gene Ids in N to N mapping results mapped from ZFIN to NCBI and back to ZFIN
        # key: ZDB gene Id
        # value: reference to hash of associated NCBI gene Id(s)

        my %zdbIdsOfNtoN = ();

        ## set on the flag of 1 to N (ZFIN to NCBI)
        my $oneToNflag = 1;

        # get the reference to the hash of mapped NCBI genes for this ZFIN gene
        my $ref_hashNCBIids = $oneToNZFINtoNCBI{$geneZFINtoMultiNCBI};

        ## for each 1 to N (ZFIN to NCBI), examine if there is 1 to N mapping the other way (NCBI to ZFIN)
        foreach my $ncbiId (sort keys %$ref_hashNCBIids) {

            ## if existing 1 to N the other way (NCBI to ZFIN), indicating N to N
            if (exists($oneToNNCBItoZFIN{$ncbiId})) {

                ## set off flag 1 to N (ZFIN to NCBI)
                $oneToNflag = 0;

                my $ref_hashZdbIds = $oneToNNCBItoZFIN{$ncbiId};
                foreach my $zdbId (keys %$ref_hashZdbIds) {
                    if (exists($oneToNZFINtoNCBI{$zdbId})) {
                        $zdbIdsOfNtoN{$zdbId} = $oneToNZFINtoNCBI{$zdbId};
                    } elsif (exists($oneToOneZFINtoNCBI{$zdbId})) {
                        $mappedNCBIgene = $oneToOneZFINtoNCBI{$zdbId};
                        $zdbIdsOfNtoN{$zdbId} = {$mappedNCBIgene => 1};
                    } else {                              ## impossible
                        print LOG "\n\nThere is a bug: $zdbId is one of the mapped ZDB Ids of $ncbiId but could not find a mapped NCBI Id?\n\n";
                    }
                }
            }
        }

        # print N to N if it is the case, otherwise, populate the 1 to N (ZFIN to NCBI) list
        if ($oneToNflag == 0) {  ## 1 to N flag off means N to N
            $ctNtoNfromZFIN++;

            print NTON "$ctNtoNfromZFIN) -------------------------------------------------------------------------------------------------\n";
            foreach my $zdbIdNtoN (sort keys %zdbIdsOfNtoN) {
                $refArrayAccs = $supportedGeneZFIN{$zdbIdNtoN};
                print NTON "$zdbIdNtoN ($geneZDBidsSymbols{$zdbIdNtoN}) [@$refArrayAccs]\n";

                $refAssociatedNCBIgenes = $zdbIdsOfNtoN{$zdbIdNtoN};
                # for each mapped NCBI gene
                foreach my $ncbiId (sort keys %$refAssociatedNCBIgenes) {
                    $refArrayAccs = $supportedGeneNCBI{$ncbiId};
                    print NTON "	$ncbiId ($NCBIidsGeneSymbols{$ncbiId}) [@$refArrayAccs]\n";
                }
            }

            print NTON "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n";
            foreach my $ncbiGene (sort keys %$ref_hashNCBIids) {
                $refArrayAccs = $supportedGeneNCBI{$ncbiGene};
                print NTON "$ncbiGene ($NCBIidsGeneSymbols{$ncbiGene}) [@$refArrayAccs]\n";

                # the following print the associated ZFIN records for each of the mapped NCBI gene

                if (exists($oneToNNCBItoZFIN{$ncbiGene})) {
                    $refAssociatedZFINgenes = $oneToNNCBItoZFIN{$ncbiGene};

                    # for each of the associated ZFIN gene
                    foreach my $zdbId (sort keys %$refAssociatedZFINgenes) {
                        $refArrayAccs = $supportedGeneZFIN{$zdbId};
                        print NTON "	$zdbId ($geneZDBidsSymbols{$zdbId}) [@$refArrayAccs]\n";
                    }
                } elsif (exists($oneToOneNCBItoZFIN{$ncbiGene})) {
                    $mappedZFINgene = $oneToOneNCBItoZFIN{$ncbiGene};
                    $refArrayAccs = $supportedGeneZFIN{$mappedZFINgene};
                    print NTON "	$mappedZFINgene ($geneZDBidsSymbols{$mappedZFINgene}) [@$refArrayAccs]\n";
                } else {                              ## impossible
                    print NTON "There is a bug: $ncbiGene is one of the mapped NCBI gene Ids of $geneZFINtoMultiNCBI but could not find a mapped ZDB Id?\n\n";
                }
            }

            print NTON "\n";

        } else {                 ## 1 to N (ZFIN to NCBI)
            $ctOneToN++;
            $oneToN{$geneZFINtoMultiNCBI} = $ref_hashNCBIids;
        }

    }

    print NTON "\n**** the above N to N are derived from mapping ZFIN records to NCBI records and then back to ZFIN records *****\n";
    print NTON "\n**** the following N to N are derived from mapping NCBI records to ZFIN record and then back to NCBI records ****\n";
    print NTON "\n******** redundancy between the two parts of reporting N to N is expected ***********\n\n";

    print LOG "\nctOneToN = $ctOneToN\nctNtoNfromZFIN = $ctNtoNfromZFIN\n\n";

    print STATS_PRIORITY2 "\nMapping result statistics: number of 1:N (ZFIN to NCBI) - $ctOneToN\n\n";
    print STATS_PRIORITY2 "\nMapping result statistics: number of N:N (ZFIN to NCBI) - $ctNtoNfromZFIN\n\n";
}

sub getNtoOneAndNtoNfromZFINtoNCBI {
    #------------------------ get N:1 list and N:N from ZFIN to NCBI -----------------------------
    # Globals:
    #  %oneToNNCBItoZFIN
    #  %zdbGeneIdsNtoOneAndNtoN


    my $ctNtoOne = 0;
    my $ctNtoNfromNCBI = 0;
    my $refArrayAccs;
    my $mappedNCBIgene;

    # the following hash stores those zdb gene ids that are involved in N:1 and N:N (ZFIN to NCBI)
    %zdbGeneIdsNtoOneAndNtoN = ();

    foreach my $geneNCBItoMultiZFIN (sort keys %oneToNNCBItoZFIN) {
        # %ncbiIdsOfNtoN is a hash storing NCBI gene Ids in N to N mapping results mapped from NCBI to ZFIN and back to NCBI
        # key: NCBI gene Id
        # value: reference to hash of associated ZDB gene Id(s)

        my %ncbiIdsOfNtoN = ();

        ## set on the flag of 1 to N (NCBI to ZFIN)
        my $oneToNflag = 1;

        # get the reference to the hash of mapped ZFIN genes for this NCBI gene
        my $ref_hashZFINids = $oneToNNCBItoZFIN{$geneNCBItoMultiZFIN};

        ## for each 1 to N (NCBI to ZFIN), examine if there is 1 to N mapping the other way (ZFIN to NCBI)
        foreach my $zfinId (sort keys %$ref_hashZFINids) {

            $zdbGeneIdsNtoOneAndNtoN{$zfinId} = $geneNCBItoMultiZFIN;

            ## if existing 1 to N the other way (ZFIN to NCBI), indicating N to N
            if (exists($oneToNZFINtoNCBI{$zfinId})) {

                ## set off flag 1 to N (NCBI to ZFIN)
                $oneToNflag = 0;

                my $ref_hashNcbiIds = $oneToNZFINtoNCBI{$zfinId};
                foreach my $ncbiId (keys %$ref_hashNcbiIds) {
                    if (exists($oneToNNCBItoZFIN{$ncbiId})) {
                        $ncbiIdsOfNtoN{$ncbiId} = $oneToNNCBItoZFIN{$ncbiId};
                    } elsif (exists($oneToOneNCBItoZFIN{$ncbiId})) {
                        my $mappedZFINgene = $oneToOneNCBItoZFIN{$ncbiId};
                        $ncbiIdsOfNtoN{$ncbiId} = {$mappedZFINgene => 1};
                    } else {                              ## impossible
                        print LOG "\n\nThere is a bug: $ncbiId is one of the mapped NCBI Ids of $zfinId but could not find a mapped ZDB Id?\n\n";
                    }
                }
            }
        }

        # print N to N if it is the case, otherwise, populate the 1 to N (ZFIN to NCBI) list, i.e. the N to 1 (ZFIN to NCBI) list
        if ($oneToNflag == 0) {  ## 1 to N flag off means N to N
            $ctNtoNfromNCBI++;

            print NTON "$ctNtoNfromNCBI -------------------------------------------------------------------------------------------------\n";
            foreach my $ncbiIdNtoN (sort keys %ncbiIdsOfNtoN) {
                $refArrayAccs = $supportedGeneNCBI{$ncbiIdNtoN};
                print NTON "$ncbiIdNtoN ($NCBIidsGeneSymbols{$ncbiIdNtoN}) [@$refArrayAccs]\n";

                my $refAssociatedZFINgenes = $ncbiIdsOfNtoN{$ncbiIdNtoN};
                # for each mapped ZFIN gene
                foreach my $zdbId (sort keys %$refAssociatedZFINgenes) {
                    $refArrayAccs = $supportedGeneZFIN{$zdbId};
                    print NTON "	$zdbId ($geneZDBidsSymbols{$zdbId}) [@$refArrayAccs]\n";
                }
            }

            print NTON "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n";
            foreach my $zdbId (sort keys %$ref_hashZFINids) {
                $refArrayAccs = $supportedGeneZFIN{$zdbId};
                print NTON "$zdbId ($geneZDBidsSymbols{$zdbId}) [@$refArrayAccs]\n";

                # the following print the associated NCBI records for each of the mapped ZDB gene

                if (exists($oneToNZFINtoNCBI{$zdbId})) {
                    my $refAssociatedNCBIgenes = $oneToNZFINtoNCBI{$zdbId};

                    # for each of the associated ZFIN gene
                    foreach my $ncbiGene (sort keys %$refAssociatedNCBIgenes) {
                        $refArrayAccs = $supportedGeneNCBI{$ncbiGene};
                        print NTON "	$ncbiGene ($NCBIidsGeneSymbols{$ncbiGene}) [@$refArrayAccs]\n";
                    }
                } elsif (exists($oneToOneZFINtoNCBI{$zdbId})) {
                    $mappedNCBIgene = $oneToOneZFINtoNCBI{$zdbId};
                    $refArrayAccs = $supportedGeneNCBI{$mappedNCBIgene};
                    print NTON "	$mappedNCBIgene ($NCBIidsGeneSymbols{$mappedNCBIgene}) [@$refArrayAccs]\n";
                } else {                              ## impossible
                    print NTON "There is a bug: $zdbId is one of the mapped ZFIN gene Ids of $geneNCBItoMultiZFIN but could not find a mapped NCBI Id?\n\n";
                }
            }

            print NTON "\n";

        } else {                 ## 1 to N (NCBI to ZFIN) i.e. N to 1 from ZFIN to NCBI
            $ctNtoOne++;
            $nToOne{$geneNCBItoMultiZFIN} = $ref_hashZFINids;
        }
    }

    close NTON;

    print LOG "\nctNtoOne = $ctNtoOne\nctNtoNfromNCBI = $ctNtoNfromNCBI\n\n";

    print STATS_PRIORITY2 "\nMapping result statistics: number of N:1 (ZFIN to NCBI) - $ctNtoOne\n\n";
    print STATS_PRIORITY2 "\nMapping result statistics: number of N:N (NCBI to ZFIN) - $ctNtoNfromNCBI\n\n";

    my $subject = "Auto from $instance: " . "NCBI_gene_load.pl :: List of N to N";
    ZFINPerlModules->sendMailWithAttachedReport($SWISSPROT_EMAIL_REPORT,"$subject","reportNtoN");
}

sub reportOneToN {
    #--------------------- report 1:N ---------------------------------------------
    # Globals:
    #  %oneToN
    #  %supportedGeneZFIN
    #  %supportedGeneNCBI
    #  %geneZDBidsSymbols
    #  %NCBIidsGeneSymbols
    open (ONETON, ">reportOneToN") ||  die "Cannot open reportOneToN : $!\n";
    print ONETON getArtifactComparisonURLs();

    my $ct = 0;
    foreach my $zdbId (sort keys %oneToN) {
        $ct++;
        print ONETON "$ct) ---------------------------------------------\n";
        my $refArrayAccs = $supportedGeneZFIN{$zdbId};
        print ONETON "$zdbId ($geneZDBidsSymbols{$zdbId}) [@$refArrayAccs]\n\n";

        my $refHashMultiNCBIgenes = $oneToN{$zdbId};
        foreach my $ncbiId (sort keys %$refHashMultiNCBIgenes) {
            $refArrayAccs = $supportedGeneNCBI{$ncbiId};
            my $geneSymbol = exists($NCBIidsGeneSymbols{$ncbiId}) ? $NCBIidsGeneSymbols{$ncbiId} : "<no gene symbol>";
            print ONETON "   $ncbiId ($geneSymbol) [@$refArrayAccs]\n\n";
        }
    }

    close ONETON;

    my $subject = "Auto from $instance: " . "NCBI_gene_load.pl :: List of 1 to N";
    ZFINPerlModules->sendMailWithAttachedReport($SWISSPROT_EMAIL_REPORT,"$subject","reportOneToN");
}

sub reportNtoOne {
    #------------------- report N:1 -------------------------------------------------
    # Globals:
    #  %nToOne
    #  %supportedGeneNCBI
    #  %NCBIidsGeneSymbols

    open (NTOONE, ">reportNtoOne") ||  die "Cannot open reportNtoOne : $!\n";
    print NTOONE getArtifactComparisonURLs();

    my $ct = 0;
    foreach my $ncbiId (sort keys %nToOne) {
        $ct++;
        print NTOONE "$ct) ---------------------------------------------\n";
        my $refArrayAccs = $supportedGeneNCBI{$ncbiId};
        print NTOONE "$ncbiId ($NCBIidsGeneSymbols{$ncbiId}) [@$refArrayAccs]\n\n";

        my $refHashMultiZFINgenes = $nToOne{$ncbiId};
        foreach my $zdbId (sort keys %$refHashMultiZFINgenes) {
            $refArrayAccs = $supportedGeneZFIN{$zdbId};
            print NTOONE "   $zdbId ($geneZDBidsSymbols{$zdbId}) [@$refArrayAccs]\n\n";
        }
    }

    close NTOONE;

    my $subject = "Auto from $instance: " . "NCBI_gene_load.pl :: List of N to 1";
    ZFINPerlModules->sendMailWithAttachedReport($SWISSPROT_EMAIL_REPORT,"$subject","reportNtoOne");
}

sub buildVegaIDMappings {
    ##-----------------------------------------------------------------------------------
    ## Step 6: map ZFIN gene records to NCBI gene Ids based on common Vega Gene Id
    ##-----------------------------------------------------------------------------------
    #---------------------------------------------------------------------------
    # prepare the list of ZFIN gene with Vega Ids to be mapped to NCBI records
    #---------------------------------------------------------------------------
    # Globals:
    #     %ZDBgeneAndVegaGeneIds
    #     %VegaGeneAndZDBgeneIds
    #     %ZDBgeneWithMultipleVegaGeneIds
    #     %vegaGeneIdWithMultipleZFINgenes

    my $sqlGetVEGAidAndGeneZDBId = "select mrel_mrkr_1_zdb_id, dblink_acc_num
                               from marker_relationship, db_link
                              where mrel_mrkr_2_zdb_id = dblink_linked_recid
                                and dblink_fdbcont_zdb_id = '$fdcontVega'
                                and (mrel_mrkr_1_zdb_id like 'ZDB-GENE%' or mrel_mrkr_1_zdb_id like '%RNAG%')
                                and dblink_acc_num like 'OTTDARG%'
                                and mrel_type = 'gene produces transcript';";

    my $curGetZDBgeneIdVegaGeneId = $handle->prepare($sqlGetVEGAidAndGeneZDBId);
    $curGetZDBgeneIdVegaGeneId->execute();

    my ($geneZdbId, $VegaGeneId);
    $curGetZDBgeneIdVegaGeneId->bind_columns(\$geneZdbId,\$VegaGeneId);

    ## store the ZDB Gene ID/VEGA Gene Id and VEGA Gene Id/ZDB Gene ID pairs in hashes
    ## also store those ZDB gene Ids with multiple corresponding VEGA Gene Ids

    %ZDBgeneAndVegaGeneIds = ();
    %VegaGeneAndZDBgeneIds = ();
    %ZDBgeneWithMultipleVegaGeneIds = ();
    %vegaGeneIdWithMultipleZFINgenes = ();

    my $ctTotalZDBgeneIdVegaGeneIds = 0;
    my $ref_arrayZDBgenes;

    while ($curGetZDBgeneIdVegaGeneId->fetch()) {
        $ctTotalZDBgeneIdVegaGeneIds++;
        my $ref_arrayVegaGeneIds;
        if (exists($ZDBgeneWithMultipleVegaGeneIds{$geneZdbId}) || (exists($ZDBgeneAndVegaGeneIds{$geneZdbId}) && $ZDBgeneAndVegaGeneIds{$geneZdbId} ne $VegaGeneId)) {
            if (!exists($ZDBgeneWithMultipleVegaGeneIds{$geneZdbId})) {
                my $firstVegaGeneIdFound = $ZDBgeneAndVegaGeneIds{$geneZdbId};
                $ref_arrayVegaGeneIds = [$firstVegaGeneIdFound,$VegaGeneId];
                $ZDBgeneWithMultipleVegaGeneIds{$geneZdbId} = $ref_arrayVegaGeneIds;
            } else {
                $ref_arrayVegaGeneIds = $ZDBgeneWithMultipleVegaGeneIds{$geneZdbId};
                push(@$ref_arrayVegaGeneIds, $VegaGeneId);
            }
        }

        if (exists($vegaGeneIdWithMultipleZFINgenes{$VegaGeneId})
            || (exists($VegaGeneAndZDBgeneIds{$VegaGeneId}) && $VegaGeneAndZDBgeneIds{$VegaGeneId} ne $geneZdbId)) {
            if (!exists($vegaGeneIdWithMultipleZFINgenes{$VegaGeneId})) {
                my $firstGeneZDBidFound = $VegaGeneAndZDBgeneIds{$VegaGeneId};
                $ref_arrayZDBgenes = [$firstGeneZDBidFound,$geneZdbId];
                $vegaGeneIdWithMultipleZFINgenes{$VegaGeneId} = $ref_arrayZDBgenes;
            } else {
                $ref_arrayZDBgenes = $vegaGeneIdWithMultipleZFINgenes{$VegaGeneId};
                push(@$ref_arrayZDBgenes, $geneZdbId);
            }
        }

        $ZDBgeneAndVegaGeneIds{$geneZdbId} = $VegaGeneId;
        $VegaGeneAndZDBgeneIds{$VegaGeneId} = $geneZdbId;
    }

    print LOG "\nctTotalZDBgeneIdVegaGeneIds = $ctTotalZDBgeneIdVegaGeneIds\n\n";

    print STATS_PRIORITY2 "\nThe total number of ZFIN genes with Vega Gene Id: $ctTotalZDBgeneIdVegaGeneIds\n\n";

    $curGetZDBgeneIdVegaGeneId->finish();

    my $ctVegaIdWithMultipleZDBgene = 0;
    print LOG "\nThe following Vega Gene Ids at ZFIN correspond to multiple ZDB Gene Ids\n";
    foreach my $vega (sort keys %vegaGeneIdWithMultipleZFINgenes) {
        $ctVegaIdWithMultipleZDBgene++;
        $ref_arrayZDBgenes = $vegaGeneIdWithMultipleZFINgenes{$vega};
        print LOG "$vega @$ref_arrayZDBgenes\n";
    }
    print LOG "\nctVegaIdWithMultipleZDBgene = $ctVegaIdWithMultipleZDBgene\n\n";

    open (ZDBGENENVEGA, ">reportZDBgeneIdWithMultipleVegaIds") ||  die "Cannot open reportZDBgeneIdWithMultipleVegaIds : $!\n";
    my $ctZDBgeneIdWithMultipleVegaId = 0;
    foreach my $zdbGene (sort keys %ZDBgeneWithMultipleVegaGeneIds) {
        $ctZDBgeneIdWithMultipleVegaId++;
        my $ref_arrayVegaIds = $ZDBgeneWithMultipleVegaGeneIds{$zdbGene};
        print ZDBGENENVEGA "$zdbGene @$ref_arrayVegaIds\n";
    }
    print LOG "\nctZDBgeneIdWithMultipleVegaId = $ctZDBgeneIdWithMultipleVegaId\n\n";
    close ZDBGENENVEGA;
}

sub writeCommonVegaGeneIdMappings {
    ## ---------------------------------------------------------------------------------------------------------------------
    ## doing the mapping based on common Vega Gene Id
    ## ---------------------------------------------------------------------------------------------------------------------
    # Globals:
    #   %geneZDBidsSymbols
    #   %mapped
    #   %oneToNZFINtoNCBI
    #   %zdbGeneIdsNtoOneAndNtoN
    #   %geneZFINwithAccSupportingMoreThan1
    #   %oneToOneViaVega

    %oneToOneViaVega = ();

    my $ctMappedViaVega = 0;

    foreach my $zdbId (sort keys %geneZDBidsSymbols) {
        ## exclude those in the final 1:1, 1:N, N:1, N:N lists and those in the list of those
        ## with at least one GenBank RNA accession supporting more than 1 genes

        if (!exists($mapped{$zdbId}) && !exists($oneToNZFINtoNCBI{$zdbId}) && !exists($zdbGeneIdsNtoOneAndNtoN{$zdbId})
            && !exists($geneZFINwithAccSupportingMoreThan1{$zdbId})) {

            if (exists($ZDBgeneAndVegaGeneIds{$zdbId}) && !exists($ZDBgeneWithMultipleVegaGeneIds{$zdbId})) {
                my $vegaGeneId = $ZDBgeneAndVegaGeneIds{$zdbId};

                ## exclude those NCBI gene Ids that are in the final 1:1, 1:N, N:1, N:N lists and
                ## those in the list of those with at least 1 GenBank RNA accession supporting more than 1 genes

                if (exists($vegaIdsNCBIids{$vegaGeneId})
                    && !exists($vegaIdwithMultipleNCBIids{$vegaGeneId})
                    && !exists($vegaGeneIdWithMultipleZFINgenes{$vegaGeneId})
                ) {

                    my $NCBIgeneIdMappedViaVega = $vegaIdsNCBIids{$vegaGeneId};

                    ## exclude those NCBI gene Ids with multiple Vega Gene Ids, and those with multiple GenBank RNAs,
                    ## and those already mapped

                    if (!exists($NCBIgeneWithMultipleVega{$NCBIgeneIdMappedViaVega})
                        && !exists($geneNCBIwithAccSupportingMoreThan1{$NCBIgeneIdMappedViaVega})
                        && !exists($mappedReversed{$NCBIgeneIdMappedViaVega})
                    ) {

                        ## ----- write the NCBI gene Ids mapped via Vega gene Id on toLoad.unl ---------------
                        print TOLOAD "$zdbId|$NCBIgeneIdMappedViaVega|||$fdcontNCBIgeneId|$pubMappedbasedOnVega\n";
                        $ctToLoad++;

                        $oneToOneViaVega{$NCBIgeneIdMappedViaVega} = $zdbId;
                        $ctMappedViaVega++;
                    }
                }
            }
        }
    }

    my $ctTotalMapped = $ctMappedViaVega + $ctOneToOneNCBI;
    print LOG "\nctMappedViaVega = $ctMappedViaVega\n\nTotal number of the gene records mapped: $ctMappedViaVega + $ctOneToOneNCBI = $ctTotalMapped\n\n";
    print STATS_PRIORITY2 "\nMapping result via Vega Gene Id: $ctMappedViaVega additional gene records are mapped\n\n";
    print STATS_PRIORITY2 "Total number of the gene records mapped: $ctMappedViaVega + $ctOneToOneNCBI = $ctTotalMapped\n\n";
}

sub calculateLengthForAccessionsWithoutLength {
    #--------------------------------------------------------------------------------------------------------------
    # This section CONTINUES to deal with dblink_length field
    # There are 3 sources for length:
    # 1) the existing dblink_length for GenBank including GenPept records
    # 2) the length value of RefSeq sequences on NCBI's RefSeq-release#.catalog file
    # 3) calculated length
    # The first two have been done before parsing the gene2accession file.
    # During parsing gene2accession file, accessions still missing length are stored in a hash named %noLength
    #---------------------------------------------------------------------------------------------------------------
    # Globals:
    #   %noLength

    #----------------------- 3) calculate the length for the those still with no length ---------------
    open (NOLENGTH, ">noLength.unl") ||  die "Cannot open noLength.unl : $!\n";

    foreach my $accWithNoLength (keys %noLength) {
        my $NCBIgeneId = $noLength{$accWithNoLength};

        # print to the noLength.unl only for those with mapped gene Id
        print NOLENGTH "$accWithNoLength\n" if exists($mappedReversed{$NCBIgeneId}) || exists($oneToOneViaVega{$NCBIgeneId});
    }

    close NOLENGTH;

    system("/bin/date");

    if (!-e "noLength.unl") {
        print LOG "\nCannot find noLength.unl as input file for efetch.\n\n";
        close STATS_PRIORITY2;
        my $subjectLine = "Auto from $instance: " . "NCBI_gene_load.pl :: no input file for efetch.r";
        reportErrAndExit($subjectLine);
    }

    print LOG "\nStart efetching at " . strftime("%Y-%m-%d %H:%M:%S", localtime(time())) . " \n";
    print "\nStart efetching " . strftime("%Y-%m-%d %H:%M:%S", localtime(time())) . " \n";

    # Using the above noLength.unl as input, call efetch to get the fasta sequences
    # and output to seq.fasta file. This step is time-consuming.

    my $forceEfetchStep = 0;
    if (exists($ENV{"FORCE_EFETCH"}) && $ENV{"FORCE_EFETCH"}) {
        $forceEfetchStep = 1;
    }
    if (!$forceEfetchStep && exists($ENV{"SKIP_DOWNLOADS"}) && $ENV{"SKIP_DOWNLOADS"}) {
        print LOG "\nSKIP_DOWNLOADS is set, so skipping the efetch step.\n\n";
        print "\nSKIP_DOWNLOADS is set, so skipping the efetch step.\n\n";
    } else {
        print LOG "\nRunning the gradle job for efetch step.\n";
        print "\nRunning the gradle job for efetch step.\n";

        my $currentDir = cwd;

        # Set the JAVA_HOME path to override the jenkins one
        $ENV{'JAVA_HOME'} = getPropertyValue("JAVA_HOME");
        if (exists($ENV{'OVERRIDE_JAVA_HOME'})) {
            print "Overriding JAVA_HOME with $ENV{'OVERRIDE_JAVA_HOME'}\n";
            $ENV{'JAVA_HOME'} = $ENV{'OVERRIDE_JAVA_HOME'};
        }

        if (!$ENV{'SKIP_EFETCH'}) {
            my $cmdEfetch = "cd " . $ENV{'SOURCEROOT'} . " ; " .
                "gradle '-DncbiLoadInput=$currentDir/noLength.unl' " .
                "       '-DncbiLoadOutput=$currentDir/seq.fasta' " .
                "         BatchNCBIFastaFetchTask ; " .
                "cd $currentDir";
            print "Executing $cmdEfetch\n";
            print LOG "Executing $cmdEfetch\n";

            doSystemCommand($cmdEfetch);
        } else {
            print "Skipping gradle task ncbiMatchThroughEnsemblTask due to SKIP_EFETCH environment variable\n";
            print LOG "Skipping gradle task ncbiMatchThroughEnsemblTask due to SKIP_EFETCH environment variable\n";
        }


    }

    print LOG "\nAfter efetching at " . strftime("%Y-%m-%d %H:%M:%S", localtime(time())) . " \n";
    print "\nAfter efetching at " . strftime("%Y-%m-%d %H:%M:%S", localtime(time())) . " \n";

    system("/bin/date");

    if (!-e "seq.fasta") {
        print LOG "\n No seq.fasta found (maybe issue with efetch): $! \n\n";
        close STATS_PRIORITY2;
        my $subjectLine = "Auto from $instance: " . "NCBI_gene_load.pl :: ERROR with efetch";
        reportErrAndExit($subjectLine);
    }

    #print some info on seq.fasta
    my $hash = md5File("seq.fasta");
    print("seq.fasta md5: $hash\n");

    my $size = -s "seq.fasta";
    print("seq.fasta size: $size\n");

    # FASTA_LEN_COMMAND is the script that does the calculation based on fasta sequence
    my $cmdCalLength = "$FASTA_LEN_COMMAND seq.fasta >length.unl";
    doSystemCommand($cmdCalLength);

    if (!-e "length.unl") {
        print LOG "\nError happened when execute $FASTA_LEN_COMMAND seq.fasta >length.unl: $! \n\n";
        close STATS_PRIORITY2;
        my $subjectLine = "Auto from $instance: NCBI_gene_load.pl :: ERROR with $FASTA_LEN_COMMAND";
        reportErrAndExit($subjectLine);
    }

    my $ctSeqLengthCalculated = 0;
    open (LENGTH, "length.unl") ||  die "Cannot open length.unl : $!\n";
    while (<LENGTH>) {
        chomp;
        if ($_ =~ m/^(\w+)\|(\d+)\|$/) {
            my $acc = $1;
            my $length = $2;

            $sequenceLength{$acc} = $length;

            $ctSeqLengthCalculated++;
        }
    }
    close LENGTH;

    print LOG "\nctSeqLengthCalculated = $ctSeqLengthCalculated\n\n";
}

sub getGenBankAndRefSeqsWithZfinGenes {
    #---------------------------------------------------------------------------------------------
    # Step 7: prepare the final add-list for RefSeq and GenBank records
    #---------------------------------------------------------------------------------------------
    # Globals:
    #   %geneAccFdbcont

    ## the following SQL is used to get all the existing GenBank and RefSeq records with gene and pseudogene at ZFIN
    ## these records should not and could not be loaded

    my $sqlGetGenBankAndRefSeqAccs = "select dblink_linked_recid, dblink_acc_num, dblink_fdbcont_zdb_id, dblink_zdb_id
                                 from db_link
                                where (dblink_linked_recid like 'ZDB-GENE%' or dblink_linked_recid like '%RNAG%')
                                  and dblink_fdbcont_zdb_id in ('$fdcontGenBankRNA','$fdcontGenPept',
                                                                '$fdcontGenBankDNA','$fdcontRefSeqRNA',
                                                                '$fdcontRefPept','$fdcontRefSeqDNA');";

    my $curGetGenBankAndRefSeqAccs = $handle->prepare($sqlGetGenBankAndRefSeqAccs);

    $curGetGenBankAndRefSeqAccs->execute();
    my ($gene,$acc,$fdbcont,$dblinkId);
    $curGetGenBankAndRefSeqAccs->bind_columns(\$gene,\$acc,\$fdbcont,\$dblinkId);

    # in order for the inserting not to violate the constraint unique (dblink_linked_recid,dblink_acc_num,dblink_fdbcont_zdb_id)
    # key: concatenated string of gene zdb id,accession number and zdb if of fdbcont
    # value: zdb id of the db_link record

    %geneAccFdbcont = ();

    my $ctGeneAccFdbcont = 0;
    while ($curGetGenBankAndRefSeqAccs->fetch()) {
        if (!exists($toDelete{$dblinkId})) {              # exclude those to be deleted first
            $geneAccFdbcont{$gene . $acc . $fdbcont} = $dblinkId;
            $ctGeneAccFdbcont++;
        }
    }

    print LOG "\nctGeneAccFdbcont = $ctGeneAccFdbcont\n\n";

    $curGetGenBankAndRefSeqAccs->finish();
}

sub getSqlForGeneAndRnagDbLinksFromFdbContId {
    my $fdbContId = shift;
    my $sql = "select distinct dblink_acc_num
          from db_link
         where dblink_fdbcont_zdb_id = '$fdbContId'
           and (dblink_linked_recid like 'ZDB-GENE%' or dblink_linked_recid like '%RNAG%')";
    return $sql;
}

sub getSqlForGeneAndRnagDbLinksSupportedByLoadPubsFromFdbContId {
    my $fdbContId = shift;
    my $sql = getSqlForGeneAndRnagDbLinksFromFdbContId($fdbContId) .
           " and exists (select 1 from record_attribution
               where recattrib_data_zdb_id = dblink_zdb_id
                 and recattrib_source_zdb_id in ('$pubMappedbasedOnRNA','$pubMappedbasedOnVega','$pubMappedbasedOnNCBISupplement'))";
    return $sql;
}

sub writeGenBankRNAaccessionsWithMappedGenesToLoad {
    #---------------------------------------------------------------------------
    #  write GenBank RNA accessions with mapped genes onto toLoad.unl
    #---------------------------------------------------------------------------
    # Globals:
    #   %accNCBIsupportingOnly1
    #   %mappedReversed
    #   %geneAccFdbcont
    #   %oneToOneViaVega

    foreach my $GenBankRNA (sort keys %accNCBIsupportingOnly1) {
        my $NCBIgeneId = $accNCBIsupportingOnly1{$GenBankRNA};
        my ($zdbGeneId, $attributionPub) = getZdbGeneIdAndAttributionByNCBIgeneId($NCBIgeneId);

        next if (!$zdbGeneId); # skip if no ZFIN gene ID found

        my $hashKey = $zdbGeneId . $GenBankRNA . $fdcontGenBankRNA;
        next if (exists($geneAccFdbcont{$hashKey})); # skip if already in the hash

        my $length = exists($sequenceLength{$GenBankRNA}) ? $sequenceLength{$GenBankRNA} : '';
        print TOLOAD "$zdbGeneId|$GenBankRNA||$length|$fdcontGenBankRNA|$attributionPub\n";
        $geneAccFdbcont{$hashKey} = 1;
        $ctToLoad++;
    }
}

sub initializeGenPeptAccessionsMap {
    #---------------------------------------------------------------------------------------
    #  write GenPept accessions with mapped genes onto toLoad.unl
    #---------------------------------------------------------------------------------------
    # Globals:
    #     %GenPeptAttributedToNonLoadPub
    #     %GenPeptDbLinkIdAttributedToNonLoadPub

    # get the Genpept accessions and the attribututed pulications that are not the load publications

    my $sqlGenPeptAttributedToNonLoadPub = "select dblink_acc_num, dblink_zdb_id, recattrib_source_zdb_id
                                       from record_attribution, db_link
                                      where recattrib_data_zdb_id = dblink_zdb_id
                                        and dblink_fdbcont_zdb_id = '$fdcontGenPept'
                                        and (dblink_linked_recid like 'ZDB-GENE%' or dblink_linked_recid like '%RNAG%')
                                        and recattrib_source_zdb_id not in ('$pubMappedbasedOnRNA','$pubMappedbasedOnVega');";

    my $curGenPeptAttributedToNonLoadPub = $handle->prepare($sqlGenPeptAttributedToNonLoadPub);

    $curGenPeptAttributedToNonLoadPub->execute;

    my ($GenPept,$dbLinkId,$nonLoadPub);
    $curGenPeptAttributedToNonLoadPub->bind_columns(\$GenPept,\$dbLinkId,\$nonLoadPub);

    # use the following hash to store GenPept accessions and the attributed pulications that are not one of the load publications
    # key: GenPept accession
    # value: publication zdb id

    %GenPeptAttributedToNonLoadPub = ();

    # use the following hash to store GenPept acc and db_link zdb Id that are attributed pulications that are not one of the load publications
    # key: GenPept accession
    # value: db_link zdb id

    %GenPeptDbLinkIdAttributedToNonLoadPub = ();

    my $ctGenPeptNonLoadPub = 0;
    while ($curGenPeptAttributedToNonLoadPub->fetch) {
        $GenPeptAttributedToNonLoadPub{$GenPept} = $nonLoadPub;
        $GenPeptDbLinkIdAttributedToNonLoadPub{$GenPept} = $dbLinkId;
        $ctGenPeptNonLoadPub++;
    }

    print LOG "\nNumber of GenPept accessions attributed to non-load pub: $ctGenPeptNonLoadPub\n\n";

    $curGenPeptAttributedToNonLoadPub->finish();

    my $ctGenPeptAttributedToNonLoadPub = scalar(keys %GenPeptAttributedToNonLoadPub);

    print LOG "\nctGenPeptAttributedToNonLoadPub = $ctGenPeptAttributedToNonLoadPub\n\n";
}

sub processGenBankAccessionsAssociatedToNonLoadPubs {
    #-------- deal with those GenBank accessions attributed to non-load publication -------
    # Globals:
    #  %GenPeptsToLoad
    #  %GenPeptNCBIgeneIds
    #  %mappedReversed
    #  %geneAccFdbcont
    #  %GenPeptAttributedToNonLoadPub
    #  %GenPeptDbLinkIdAttributedToNonLoadPub

    # use the following hash to store GenPept accessions to be loaded
    # key: GenPept accession number
    # value: zdb gene id
    %GenPeptsToLoad = ();

    open (MORETODELETE, ">>toDelete.unl") ||  die "Cannot open toDelete.unl : $!\n";

    my $ctToAttribute = 0;

    print LOG "\nThe GenPept accessions used to attribute to non-load publication now attribute to load pub:\n\n";
    print LOG "GenPept\tZFIN gene Id\tnon-load pub\tload pub\n";
    print LOG "-------\t------------\t------------\t--------\n";

    my $zdbGeneId;
    my $attributionPub;
    my $moreToDelete;
    foreach my $GenPept (sort keys %GenPeptNCBIgeneIds) {
        my $NCBIgeneId = $GenPeptNCBIgeneIds{$GenPept};
        ($zdbGeneId, $attributionPub) = getZdbGeneIdAndAttributionByNCBIgeneId($NCBIgeneId);

        next if (!$zdbGeneId); # skip if no zdb gene id found

        my $hashKey = $zdbGeneId . $GenPept . $fdcontGenPept;

        if (!exists($geneAccFdbcont{$hashKey})) {
            my $length = exists($sequenceLength{$GenPept}) ? $sequenceLength{$GenPept} : '';
            print TOLOAD "$zdbGeneId|$GenPept||$length|$fdcontGenPept|$attributionPub\n";
            $geneAccFdbcont{$hashKey} = 1;
            $ctToLoad++;
            $GenPeptsToLoad{$GenPept} = $zdbGeneId;
        } elsif (exists($GenPeptAttributedToNonLoadPub{$GenPept}) && exists($GenPeptDbLinkIdAttributedToNonLoadPub{$GenPept})) {
            $moreToDelete = $GenPeptDbLinkIdAttributedToNonLoadPub{$GenPept};
            print MORETODELETE "$moreToDelete\n";
            $toDelete{$moreToDelete} = 1;
            my $length = exists($sequenceLength{$GenPept}) ? $sequenceLength{$GenPept} : '';
            print TOLOAD "$zdbGeneId|$GenPept||$length|$fdcontGenPept|$attributionPub\n";
            $geneAccFdbcont{$hashKey} = 1;
            $ctToLoad++;
            print LOG "$GenPept\t$zdbGeneId\t$GenPeptAttributedToNonLoadPub{$GenPept}\t$attributionPub\n";
            $ctToAttribute++;
        }
    }

    close MORETODELETE;

    print LOG "---------------------------------------------------------------\nTotal: $ctToAttribute\n\n";

    print STATS_PRIORITY2 "\nNon-load attribution for the $ctToAttribute manually curated GenPept db_link records get replaced by;\n 1 of the 2 load pubs (depending on mapping type).\n\n";
}

sub printGenPeptsAssociatedWithGeneAtZFIN {
    # ----- get all the Genpept accessions associated with gene at ZFIN, and those with multiple ZFIN genes ----------------------------
    # Globals:
    #   %GenPeptsToLoad


    my $sqlAllGenPeptWithGeneZFIN = "select dblink_acc_num, dblink_linked_recid
                                from db_link
                               where dblink_fdbcont_zdb_id = '$fdcontGenPept'
                                 and (dblink_linked_recid like 'ZDB-GENE%' or dblink_linked_recid like '%RNAG%');";

    my $curAllGenPeptWithGeneZFIN = $handle->prepare($sqlAllGenPeptWithGeneZFIN);

    $curAllGenPeptWithGeneZFIN->execute;

    my ($GenPept,$geneZdbId);
    $curAllGenPeptWithGeneZFIN->bind_columns(\$GenPept,\$geneZdbId);

    # use the following hash to store all the GenPept accession stored at ZFIN that are assoctied with gene
    # key: GenPept accession
    # value: gene zdb id

    my %AllGenPeptWithGeneZFIN = ();

    # a hash to store GenPept accessions and the multiple related ZFIN gene Ids
    # key: GenPept accession
    # value: reference to an array of gene zdb id

    my %GenPeptWithMultipleZDBgene = ();
    my $ref_arrayZDBgeneIds;
    while ($curAllGenPeptWithGeneZFIN->fetch) {

        if (exists($GenPeptWithMultipleZDBgene{$GenPept}) ||
            (exists($AllGenPeptWithGeneZFIN{$GenPept}) && $AllGenPeptWithGeneZFIN{$GenPept} ne $geneZdbId)) {

            if (!exists($GenPeptWithMultipleZDBgene{$GenPept})) {
                my $firstGenPept = $AllGenPeptWithGeneZFIN{$GenPept};
                $ref_arrayZDBgeneIds = [$firstGenPept,$geneZdbId];
                $GenPeptWithMultipleZDBgene{$GenPept} = $ref_arrayZDBgeneIds;
            } else {
                $ref_arrayZDBgeneIds = $GenPeptWithMultipleZDBgene{$GenPept};
                push(@$ref_arrayZDBgeneIds, $geneZdbId);
            }
        }

        $AllGenPeptWithGeneZFIN{$GenPept} = $geneZdbId;
    }

    $curAllGenPeptWithGeneZFIN->finish();

    my $ctAllGenPeptWithGeneZFIN = scalar(keys %AllGenPeptWithGeneZFIN);

    my $ctGenPeptWithMultipleZDBgene = scalar(keys %GenPeptWithMultipleZDBgene);

    print LOG "\nctAllGenPeptWithGeneZFIN = $ctAllGenPeptWithGeneZFIN\n\n";

    print LOG "\nctGenPeptWithMultipleZDBgene = $ctGenPeptWithMultipleZDBgene\n\n";

    print LOG "-----The GenBank accessions to be loaded but also associated with multiple ZFIN genes----\n\n";
    print LOG "GenPept \t mapped gene \tall associated genes\n";
    print LOG "--------\t-------------\t-------------\n";

    my $ctGenPeptWithMultipleZDBgeneToLoad = 0;
    foreach $GenPept (sort keys %GenPeptWithMultipleZDBgene) {
        if (exists($GenPeptsToLoad{$GenPept})) {
            $ref_arrayZDBgeneIds = $GenPeptWithMultipleZDBgene{$GenPept};
            print LOG "$GenPept\t$GenPeptsToLoad{$GenPept}\t@$ref_arrayZDBgeneIds\n";
            $ctGenPeptWithMultipleZDBgeneToLoad++;
        }
    }
    print LOG "-----------------------------------------\nTotal: $ctGenPeptWithMultipleZDBgeneToLoad\n\n\n";

    print STATS_PRIORITY2 "\nBefore the load, the total number of GenPept accessions associated with multiple ZFIN genes: $ctGenPeptWithMultipleZDBgeneToLoad\n\n";

}

sub writeGenBankDNAaccessionsWithMappedGenesToLoad {
    #---------------------------------------------------------------------------
    #  write GenBank DNA accessions with mapped genes onto toLoad.unl
    #---------------------------------------------------------------------------
    # Globals:
    #   %GenBankDNAncbiGeneIds
    #   %mappedReversed
    #   %geneAccFdbcont
    #   %oneToOneViaVega
    my $zdbGeneId;
    my $NCBIgeneId;
    my $attributionPub;

    foreach my $GenBankDNA (sort keys %GenBankDNAncbiGeneIds) {
        my @multipleNCBIgeneIds = @{$GenBankDNAncbiGeneIds{$GenBankDNA}};

        if ($ENV{'DEBUG_BROKEN_LOGIC_7925'}) {
            #For recreating the broken logic from before zfin-7925 was fixed (for debugging purposes)
            print LOG "DEBUG_BROKEN_LOGIC_7925\n";
            my $tmpNcbiGeneId = pop(@multipleNCBIgeneIds);
            @multipleNCBIgeneIds = ($tmpNcbiGeneId);
        }
        print LOG "DEBUG: " . scalar(@multipleNCBIgeneIds) . " NCBI Gene IDs for " . $GenBankDNA . ":";

        foreach $NCBIgeneId (@multipleNCBIgeneIds) {
            print LOG " $NCBIgeneId";
            ($zdbGeneId, $attributionPub) = getZdbGeneIdAndAttributionByNCBIgeneId($NCBIgeneId);

            next if (!$zdbGeneId); # skip if no ZFIN gene ID found

            my $hashKey = $zdbGeneId . $GenBankDNA . $fdcontGenBankDNA;
            if (!exists($geneAccFdbcont{$hashKey})) {
                my $length = exists($sequenceLength{$GenBankDNA}) ? $sequenceLength{$GenBankDNA} : '';
                print TOLOAD "$zdbGeneId|$GenBankDNA||$length|$fdcontGenBankDNA|$attributionPub\n";
                $geneAccFdbcont{$hashKey} = 1;
                $ctToLoad++;
            } else {
                my $dbLinkToPreserve = $geneAccFdbcont{$hashKey};
                print TO_PRESERVE "$dbLinkToPreserve\n";

                # mark db_link ID as one to preserve from the record_attribution table
                print LOG "_DUPE<$dbLinkToPreserve>";
            }
        }
        print LOG "\n";
    }
}

sub writeRefSeqRNAaccessionsWithMappedGenesToLoad {
    #---------------------------------------------------------------------------
    #  write RefSeq RNA accessions with mapped genes onto toLoad.unl
    #---------------------------------------------------------------------------
    # Globals:
    #  %RefSeqRNAncbiGeneIds
    #  %mappedReversed
    #  %geneAccFdbcont
    #  $pubMappedbasedOnRNA
    #  $pubMappedbasedOnVega
    #  $ctToLoad
    #  $fdcontRefSeqRNA
    my $NCBIgeneId;
    my $zdbGeneId;
    my $attributionPub; #TODO search for others of this not scoped or assigned, but not read

    foreach my $RefSeqRNA (sort keys %RefSeqRNAncbiGeneIds) {
        $NCBIgeneId = $RefSeqRNAncbiGeneIds{$RefSeqRNA};

        ($zdbGeneId, $attributionPub) = getZdbGeneIdAndAttributionByNCBIgeneId($NCBIgeneId);

        next if (!$zdbGeneId); # skip if no ZFIN gene ID found

        my $hashKey = $zdbGeneId . $RefSeqRNA . $fdcontRefSeqRNA;
        next if (exists($geneAccFdbcont{$hashKey})); # skip if already in the hash

        my $length = exists($sequenceLength{$RefSeqRNA}) ? $sequenceLength{$RefSeqRNA} : '';
        print TOLOAD "$zdbGeneId|$RefSeqRNA||$length|$fdcontRefSeqRNA|$attributionPub\n";
        $geneAccFdbcont{$hashKey} = 1;
        $ctToLoad++;
    }
}

sub writeRefPeptAccessionsWithMappedGenesToLoad {
    #---------------------------------------------------------------------------
    #  write RefPept accessions with mapped genes onto toLoad.unl
    #---------------------------------------------------------------------------
    # Globals:
    #  %RefPeptNCBIgeneIds
    #  %mappedReversed
    #  %geneAccFdbcont
    #  $pubMappedbasedOnRNA
    #  $pubMappedbasedOnVega
    #  $ctToLoad
    #  $fdcontRefPept
    #  %oneToOneViaVega

    foreach my $RefPept (sort keys %RefPeptNCBIgeneIds) {
        my $NCBIgeneId = $RefPeptNCBIgeneIds{$RefPept};
        my ($zdbGeneId, $attributionPub) = getZdbGeneIdAndAttributionByNCBIgeneId($NCBIgeneId);

        next if (!$zdbGeneId); #didn't find a mapping to write to toLoad.unl

        my $hashKey = $zdbGeneId . $RefPept . $fdcontRefPept;
        next if (exists($geneAccFdbcont{$hashKey})); #already wrote this one to toLoad.unl

        # write to toLoad.unl
        my $length = exists($sequenceLength{$RefPept}) ? $sequenceLength{$RefPept} : '';
        print TOLOAD "$zdbGeneId|$RefPept||$length|$fdcontRefPept|$attributionPub\n";
        $geneAccFdbcont{$hashKey} = 1;
        $ctToLoad++;
    }
}

sub writeRefSeqDNAaccessionsWithMappedGenesToLoad {
    #---------------------------------------------------------------------------
    #  write RefSeq DNA accessions with mapped genes onto toLoad.unl
    #---------------------------------------------------------------------------
    # Globals:
    #  %RefSeqDNAncbiGeneIds
    #  %mappedReversed
    #  %geneAccFdbcont
    #  $pubMappedbasedOnRNA
    #  $pubMappedbasedOnVega
    #  $ctToLoad
    #  $fdcontRefSeqDNA
    #  %oneToOneViaVega
    #
    # Iterate over all the RefSeq DNA accessions that have been mapped to an NCBI gene ID.
    # Check if the NCBI gene ID has been mapped to a ZFIN gene ID via either %mappedReversed or %oneToOneViaVega.
    # If so, write the ZFIN gene ID and the RefSeq DNA accession to toLoad.unl with the appropriate pub attribution.

    foreach my $RefSeqDNA (sort keys %RefSeqDNAncbiGeneIds) {
        my $NCBIgeneId = $RefSeqDNAncbiGeneIds{$RefSeqDNA};

        #get corresponding ZFIN gene ID and pub attribution if it exists
        my ($zdbGeneId, $attributionPub) = getZdbGeneIdAndAttributionByNCBIgeneId($NCBIgeneId);

        next if (!$zdbGeneId); #didn't find a mapping to write to toLoad.unl

        my $hashKey = $zdbGeneId . $RefSeqDNA . $fdcontRefSeqDNA;
        next if (exists($geneAccFdbcont{$hashKey})); #already wrote this one to toLoad.unl

        # write to toLoad.unl
        my $length = exists($sequenceLength{$RefSeqDNA}) ? $sequenceLength{$RefSeqDNA} : '';
        print TOLOAD "$zdbGeneId|$RefSeqDNA||$length|$fdcontRefSeqDNA|$attributionPub\n";
        $geneAccFdbcont{$hashKey} = 1;
        $ctToLoad++;
    }
}

sub getZdbGeneIdAndAttributionByNCBIgeneId {
    #---------------------------------------------------------------------------
    #  write RefSeq DNA accessions with mapped genes onto toLoad.unl
    #---------------------------------------------------------------------------
    # Globals:
    #  %mappedReversed
    #  %oneToOneViaVega
    #  %ncbiSupplementMapReversed
    #  $pubMappedbasedOnRNA
    #  $pubMappedbasedOnVega
    #  $pubMappedbasedOnNCBISupplement
    my $NCBIgeneId = shift;

    my $zdbGeneId = 0;
    my $attributionPub = 0;

    if ($zdbGeneId = $mappedReversed{$NCBIgeneId}) {
        $attributionPub = $pubMappedbasedOnRNA;
    } elsif ($zdbGeneId = $oneToOneViaVega{$NCBIgeneId}) {
        $attributionPub = $pubMappedbasedOnVega;
    } elsif ($zdbGeneId = $ncbiSupplementMapReversed{$NCBIgeneId}) {
        $attributionPub = $pubMappedbasedOnNCBISupplement;
    }

    return ($zdbGeneId, $attributionPub);
}

sub closeUnloadFiles {
    close TOLOAD;
    close TO_PRESERVE;
    doSystemCommand("sort --unique -o toPreserve.unl toPreserve.unl");
}

sub printStatsBeforeDelete {
    system("/bin/date");
    print LOG "Done everything before doing the deleting and inserting\n";
    print LOG "\n$ctToDelete total number of db_link records are dropped.\n$ctToLoad total number of new records are added.\n\n";
    print STATS_PRIORITY2 "\n$ctToDelete total number of db_link records are dropped.\n$ctToLoad total number of new records are added.\n\n";
}

sub executeDeleteAndLoadSQLFile {
    #-----------------------------------------------------------------------------------------------------------------------
    # Step 8: execute the SQL file to do the deletion according to delete list, and do the loading according to te add list
    #-----------------------------------------------------------------------------------------------------------------------

    if (!-e "toLoad.unl" || $ctToLoad == 0) {
        print LOG "\nMissing the add list, toLoad.unl, or it is empty. Something is wrong!\n\n";
        close STATS_PRIORITY2;
        my $subjectLine = "Auto from $instance: " . "NCBI_gene_load.pl :: missing or empty add list, toLoad.unl";
        reportErrAndExit($subjectLine);
    }

    try {
        doSystemCommand("psql --echo-all -v ON_ERROR_STOP=1 -d $ENV{'DB_NAME'} -a -f loadNCBIgeneAccs.sql >loadLog1 2> loadLog2");
    } catch {
        chomp $_;
        reportErrAndExit("Auto from $instance: NCBI_gene_load.pl :: failed at loadNCBIgeneAccs.sql");
    } ;

    print LOG "\nDone with the deltion and loading!\n\n";
}

sub reportAllLoadStatistics {
    #-------------------------------------------------------------------------------------------------
    # Step 9: Report the GenPept accessions associated with multiple ZFIN genes after the load.
    # Report GenPept accessions associated with ZFIN genes still attributed to a non-load pub.
    # And do the record counts after the load, and report statistics.
    #-------------------------------------------------------------------------------------------------

    # ----- AFTER THE LOAD, get all the Genpept accessions associated with gene at ZFIN, and those with multiple ZFIN genes ---------

    my $sqlAllGenPeptWithGeneAfterLoad = "select dblink_acc_num, dblink_linked_recid
                                     from db_link
                                    where dblink_fdbcont_zdb_id = '$fdcontGenPept'
                                      and (dblink_linked_recid like 'ZDB-GENE%' or dblink_linked_recid like '%RNAG%');";

    my $curAllGenPeptWithGeneAfterLoad = $handle->prepare($sqlAllGenPeptWithGeneAfterLoad);

    $curAllGenPeptWithGeneAfterLoad->execute;

    my ($GenPept, $geneZdbId);
    $curAllGenPeptWithGeneAfterLoad->bind_columns(\$GenPept,\$geneZdbId);

    # use the following hash to store all the GenPept accession stored at ZFIN that are assoctied with gene after the load
    # key: GenPept accession
    # value: gene zdb id

    my %allGenPeptWithGeneAfterLoad = ();

    # a hash to store GenPept accessions and the multiple related ZFIN gene Ids after the load
    # key: GenPept accession
    # value: reference to an array of gene zdb id

    my %GenPeptWithMultipleZDBgeneAfterLoad = ();
    my $ref_arrayZDBgeneIds;

    while ($curAllGenPeptWithGeneAfterLoad->fetch) {

        if (exists($GenPeptWithMultipleZDBgeneAfterLoad{$GenPept}) ||
            (exists($allGenPeptWithGeneAfterLoad{$GenPept}) && $allGenPeptWithGeneAfterLoad{$GenPept} ne $geneZdbId)) {

            if (!exists($GenPeptWithMultipleZDBgeneAfterLoad{$GenPept})) {
                my $firstGenPept = $allGenPeptWithGeneAfterLoad{$GenPept};
                $ref_arrayZDBgeneIds = [$firstGenPept,$geneZdbId];
                $GenPeptWithMultipleZDBgeneAfterLoad{$GenPept} = $ref_arrayZDBgeneIds;
            } else {
                $ref_arrayZDBgeneIds = $GenPeptWithMultipleZDBgeneAfterLoad{$GenPept};
                push(@$ref_arrayZDBgeneIds, $geneZdbId);
            }
        }

        $allGenPeptWithGeneAfterLoad{$GenPept} = $geneZdbId;
    }

    $curAllGenPeptWithGeneAfterLoad->finish();

    my $ctAllGenPeptWithGeneZFINafterLoad = scalar(keys %allGenPeptWithGeneAfterLoad);

    my $ctGenPeptWithMultipleZDBgeneAfterLoad = scalar(keys %GenPeptWithMultipleZDBgeneAfterLoad);

    print LOG "\nctAllGenPeptWithGeneZFINafterLoad = $ctAllGenPeptWithGeneZFINafterLoad\n\n";

    print LOG "\nctGenPeptWithMultipleZDBgeneAfterLoad = $ctGenPeptWithMultipleZDBgeneAfterLoad\n\n";

    print STATS_PRIORITY2 "----- After the load, the GenBank accessions associated with multiple ZFIN genes----\n\n";
    print STATS_PRIORITY2 "GenPept \t mapped gene \tall associated genes\n";
    print STATS_PRIORITY2 "--------\t-------------\t-------------\n";

    $ctGenPeptWithMultipleZDBgeneAfterLoad = 0;
    foreach $GenPept (sort keys %GenPeptWithMultipleZDBgeneAfterLoad) {
        $ref_arrayZDBgeneIds = $GenPeptWithMultipleZDBgeneAfterLoad{$GenPept};

        my $genPeptToLoad = "";
        if(exists($GenPeptsToLoad{$GenPept})) {
            $genPeptToLoad = $GenPeptsToLoad{$GenPept};
        } else {
            $genPeptToLoad = "[not in GenPeptsToLoad hash]";
            print "ERROR - GenPept $GenPept not in GenPeptsToLoad hash\n";
        }
        print STATS_PRIORITY2 "$GenPept\t$genPeptToLoad\t@$ref_arrayZDBgeneIds\n";
        $ctGenPeptWithMultipleZDBgeneAfterLoad++;
    }
    print STATS_PRIORITY2 "-----------------------------------------\nTotal: $ctGenPeptWithMultipleZDBgeneAfterLoad\n\n\n";

    print LOG "\nctGenPeptWithMultipleZDBgeneAfterLoad = $ctGenPeptWithMultipleZDBgeneAfterLoad\n\n";

    #-------------------------------------------------------------------------------------------------
    # Report GenPept accessions associated with ZFIN genes still attributed to a non-load pub.
    #-------------------------------------------------------------------------------------------------


    #-------------------------------------------------------------------------------------------------
    # Do the record counts after the load, and report statistics.
    #-------------------------------------------------------------------------------------------------

    my $sql = "select mrkr_zdb_id, mrkr_abbrev from marker
         where (mrkr_zdb_id like 'ZDB-GENE%' or mrkr_zdb_id like '%RNAG%')
           and exists (select 1 from db_link
         where dblink_linked_recid = mrkr_zdb_id
           and dblink_fdbcont_zdb_id in ('$fdcontRefSeqRNA','$fdcontRefPept','$fdcontRefSeqDNA'));";

    my $curGenesWithRefSeqAfter = $handle->prepare($sql);

    $curGenesWithRefSeqAfter->execute;

    my ($geneId, $geneSymbol);
    $curGenesWithRefSeqAfter->bind_columns(\$geneId,\$geneSymbol);

    my %genesWithRefSeqAfterLoad = ();

    while ($curGenesWithRefSeqAfter->fetch) {
        $genesWithRefSeqAfterLoad{$geneId} = $geneSymbol;
    }

    $curGenesWithRefSeqAfter->finish();

    my $ctGenesWithRefSeqAfter = scalar(keys %genesWithRefSeqAfterLoad);

    $handle->disconnect();

    # NCBI Gene Id
    $sql = getSqlForGeneAndRnagDbLinksFromFdbContId($fdcontNCBIgeneId);
    my $numNCBIgeneIdAfter = ZFINPerlModules->countData($sql);

    #RefSeq RNA
    $sql = getSqlForGeneAndRnagDbLinksFromFdbContId($fdcontRefSeqRNA);
    my $numRefSeqRNAAfter = ZFINPerlModules->countData($sql);

    # RefPept
    $sql = getSqlForGeneAndRnagDbLinksFromFdbContId($fdcontRefPept);
    my $numRefPeptAfter = ZFINPerlModules->countData($sql);

    #RefSeq DNA
    $sql = getSqlForGeneAndRnagDbLinksFromFdbContId($fdcontRefSeqDNA);
    my $numRefSeqDNAAfter = ZFINPerlModules->countData($sql);

    # GenBank RNA (only those loaded - excluding curated ones)
    $sql = getSqlForGeneAndRnagDbLinksSupportedByLoadPubsFromFdbContId($fdcontGenBankRNA);
    my $numGenBankRNAAfter = ZFINPerlModules->countData($sql);

    # GenPept (only those loaded - excluding curated ones)
    $sql = getSqlForGeneAndRnagDbLinksSupportedByLoadPubsFromFdbContId($fdcontGenPept);
    my $numGenPeptAfter = ZFINPerlModules->countData($sql);

    # GenBank DNA (only those loaded - excluding curated ones)
    $sql = getSqlForGeneAndRnagDbLinksSupportedByLoadPubsFromFdbContId($fdcontGenBankDNA);
    my $numGenBankDNAAfter = ZFINPerlModules->countData($sql);

    # number of genes with RefSeq RNA
    $sql = "select distinct dblink_linked_recid
          from db_link
         where dblink_fdbcont_zdb_id = '$fdcontRefSeqRNA'
           and dblink_acc_num like 'NM_%'
           and (dblink_linked_recid like 'ZDB-GENE%' or dblink_linked_recid like '%RNAG%');";

    my $numGenesRefSeqRNAAfter = ZFINPerlModules->countData($sql);

    # number of genes with RefPept
    $sql = "select distinct dblink_linked_recid
          from db_link
         where dblink_fdbcont_zdb_id = '$fdcontRefPept'
           and dblink_acc_num like 'NP_%'
           and (dblink_linked_recid like 'ZDB-GENE%' or dblink_linked_recid like '%RNAG%');";

    my $numGenesRefSeqPeptAfter = ZFINPerlModules->countData($sql);

    # number of genes with GenBank
    $sql = "select distinct dblink_linked_recid
          from db_link, foreign_db_contains, foreign_db
         where dblink_fdbcont_zdb_id = fdbcont_zdb_id
           and fdbcont_fdb_db_id = fdb_db_pk_id
           and fdb_db_name = 'GenBank'
           and (dblink_linked_recid like 'ZDB-GENE%' or dblink_linked_recid like '%RNAG%');";

    my $numGenesGenBankAfter = ZFINPerlModules->countData($sql);

    print STATS_PRIORITY1 "\n********* Percentage change of various categories of records *************\n\n";

    print STATS_PRIORITY1 "number of db_link records with gene     \t";
    print STATS_PRIORITY1 "before load\t";
    print STATS_PRIORITY1 "after load\t";
    print STATS_PRIORITY1 "percentage change\n";
    print STATS_PRIORITY1 "----------------------------------------\t-----------\t-----------\t-------------------------\n";

    print STATS_PRIORITY1 "NCBI gene Id                                  \t";
    print STATS_PRIORITY1 "$numNCBIgeneIdBefore   \t";
    print STATS_PRIORITY1 "$numNCBIgeneIdAfter   \t";
    printf STATS_PRIORITY1 "%.2f\n", ($numNCBIgeneIdAfter - $numNCBIgeneIdBefore) / $numNCBIgeneIdBefore * 100 if ($numNCBIgeneIdBefore > 0);

    print STATS_PRIORITY1 "RefSeq RNA                                 \t";
    print STATS_PRIORITY1 "$numRefSeqRNABefore        \t";
    print STATS_PRIORITY1 "$numRefSeqRNAAfter       \t";
    printf STATS_PRIORITY1 "%.2f\n", ($numRefSeqRNAAfter - $numRefSeqRNABefore) / $numRefSeqRNABefore * 100 if ($numRefSeqRNABefore > 0);

    print STATS_PRIORITY1 "RefPept                                 \t";
    print STATS_PRIORITY1 "$numRefPeptBefore   \t";
    print STATS_PRIORITY1 "$numRefPeptAfter   \t";
    printf STATS_PRIORITY1 "%.2f\n", ($numRefPeptAfter - $numRefPeptBefore) / $numRefPeptBefore * 100 if ($numRefPeptBefore > 0);

    print STATS_PRIORITY1 "RefSeq DNA                                 \t";
    print STATS_PRIORITY1 "$numRefSeqDNABefore      \t";
    print STATS_PRIORITY1 "$numRefSeqDNAAfter        \t";
    if ($numRefSeqDNABefore > 0) {
        printf STATS_PRIORITY1 "%.2f\n", ($numRefSeqDNAAfter - $numRefSeqDNABefore) / $numRefSeqDNABefore * 100;
    } else {
        printf STATS_PRIORITY1 "\n";
    }

    print STATS_PRIORITY1 "GenBank RNA                                 \t";
    print STATS_PRIORITY1 "$numGenBankRNABefore        \t";
    print STATS_PRIORITY1 "$numGenBankRNAAfter       \t";
    printf STATS_PRIORITY1 "%.2f\n", ($numGenBankRNAAfter - $numGenBankRNABefore) / $numGenBankRNABefore * 100 if ($numGenBankRNABefore > 0);

    print STATS_PRIORITY1 "GenPept                                 \t";
    print STATS_PRIORITY1 "$numGenPeptBefore   \t";
    print STATS_PRIORITY1 "$numGenPeptAfter   \t";
    printf STATS_PRIORITY1 "%.2f\n", ($numGenPeptAfter - $numGenPeptBefore) / $numGenPeptBefore * 100 if ($numGenPeptBefore > 0);

    print STATS_PRIORITY1 "GenBank DNA                                 \t";
    print STATS_PRIORITY1 "$numGenBankDNABefore       \t";
    print STATS_PRIORITY1 "$numGenBankDNAAfter        \t";
    printf STATS_PRIORITY1 "%.2f\n", ($numGenBankDNAAfter - $numGenBankDNABefore) / $numGenBankDNABefore * 100 if ($numGenBankDNABefore > 0);

    print STATS_PRIORITY1 "\n\n";

    print STATS_PRIORITY1 "number of genes                              \t";
    print STATS_PRIORITY1 "before load\t";
    print STATS_PRIORITY1 "after load\t";
    print STATS_PRIORITY1 "percentage change\n";
    print STATS_PRIORITY1 "----------------------------------------\t-----------\t-----------\t-------------------------\n";

    print STATS_PRIORITY1 "with RefSeq                             \t";
    print STATS_PRIORITY1 "$ctGenesWithRefSeqBefore   \t";
    print STATS_PRIORITY1 "$ctGenesWithRefSeqAfter   \t";
    printf STATS_PRIORITY1 "%.2f\n", ($ctGenesWithRefSeqAfter - $ctGenesWithRefSeqBefore) / $ctGenesWithRefSeqBefore * 100 if ($ctGenesWithRefSeqBefore > 0);

    print STATS_PRIORITY1 "with RefSeq NM                          \t";
    print STATS_PRIORITY1 "$numGenesRefSeqRNABefore   \t";
    print STATS_PRIORITY1 "$numGenesRefSeqRNAAfter   \t";
    printf STATS_PRIORITY1 "%.2f\n", ($numGenesRefSeqRNAAfter - $numGenesRefSeqRNABefore) / $numGenesRefSeqRNABefore * 100 if ($numGenesRefSeqRNABefore > 0);

    print STATS_PRIORITY1 "with RefSeq NP                          \t";
    print STATS_PRIORITY1 "$numGenesRefSeqPeptBefore   \t";
    print STATS_PRIORITY1 "$numGenesRefSeqPeptAfter   \t";
    printf STATS_PRIORITY1 "%.2f\n", ($numGenesRefSeqPeptAfter - $numGenesRefSeqPeptBefore) / $numGenesRefSeqPeptBefore * 100 if ($numGenesRefSeqPeptBefore > 0);

    print STATS_PRIORITY1 "with GenBank                            \t";
    print STATS_PRIORITY1 "$numGenesGenBankBefore        \t";
    print STATS_PRIORITY1 "$numGenesGenBankAfter       \t";
    printf STATS_PRIORITY1 "%.2f\n", ($numGenesGenBankAfter - $numGenesGenBankBefore) / $numGenesGenBankBefore * 100 if ($numGenesGenBankBefore > 0);

    my $jsonReportData = {
        meta    => {
            title        => "NCBI Load Report",
            releaseID    => "",
            creationDate => time() * 1000
        },
        summary => {
            description => "NCBI Load: Percentage change of various categories of records",
            tables      => [
                {
                    description => "First Table",
                    headers     => [
                        { "key" => "desc", "title" => "number of db_link records with gene" },
                        { "key" => "before", "title" => "before load" },
                        { "key" => "after", "title" => "after load" },
                        { "key" => "perc", "title" => "percentage change" }
                    ],
                    rows        => [
                        {
                            "desc"   => "NCBI gene Id",
                            "before" => $numNCBIgeneIdBefore,
                            "after"  => $numNCBIgeneIdAfter,
                            "perc"   => percentageDisplay($numNCBIgeneIdBefore, $numNCBIgeneIdAfter)
                        },
                        {
                            "desc"   => "RefSeq RNA",
                            "before" => $numRefSeqRNABefore,
                            "after"  => $numRefSeqRNAAfter,
                            "perc"   => percentageDisplay($numRefSeqRNABefore, $numRefSeqRNAAfter)
                        },
                        {
                            "desc"   => "RefPept",
                            "before" => $numRefPeptBefore,
                            "after"  => $numRefPeptAfter,
                            "perc"   => percentageDisplay($numRefPeptBefore, $numRefPeptAfter)
                        },
                        {
                            "desc"   => "RefSeq DNA",
                            "before" => $numRefSeqDNABefore,
                            "after"  => $numRefSeqDNAAfter,
                            "perc"   => percentageDisplay($numRefSeqDNABefore, $numRefSeqDNAAfter)
                        },
                        {
                            "desc"   => "GenBank RNA",
                            "before" => $numGenBankRNABefore,
                            "after"  => $numGenBankRNAAfter,
                            "perc"   => percentageDisplay($numGenBankRNABefore, $numGenBankRNAAfter)
                        }
                    ]
                }
            ]
        },
        actions => []
    };

    #Write jsonReportData to file
    my $jsonString = encode_json($jsonReportData);
    my $jsonFilename = "ncbi_report.json";
    open(FH, '>', $jsonFilename) or die $!;
    print FH $jsonString;
    close(FH);

    #Use report template to create report
    my $templateFile = $ENV{'SOURCEROOT'} . "/home/uniprot/zfin-report-template.html";
    my $reportFilename = "ncbi_report.html";

    open my $templateFileInput, '<', $templateFile or die "can't open $templateFile: $!";
    open my $templateFileOutput, '>', $reportFilename or die "can't open $reportFilename: $!";
    while (<$templateFileInput>) {
        s/JSON_GOES_HERE/$jsonString/;
        print $templateFileOutput $_;
    }
    close $templateFileInput or die "can't close $templateFile: $!";
    close $templateFileOutput or die "can't close $reportFilename: $!";


    my @keysSortedByValues = sort { lc($geneZDBidsSymbols{$a}) cmp lc($geneZDBidsSymbols{$b}) } keys %geneZDBidsSymbols;

    print STATS_PRIORITY1 "\n\nList of genes used to have RefSeq acc but no longer having any:\n";
    print STATS_PRIORITY1 "-------------------------------------------------------------------\n";

    my $symbol;
    my $ctGenesLostRefSeq = 0;
    foreach my $zdbGeneId (@keysSortedByValues) {
        $symbol = $geneZDBidsSymbols{$zdbGeneId};
        if (exists($genesWithRefSeqBeforeLoad{$zdbGeneId})
            && !exists($genesWithRefSeqAfterLoad{$zdbGeneId})) {
            $ctGenesLostRefSeq++;
            print STATS_PRIORITY1 "$zdbGeneId\n";
        }
    }

    print STATS_PRIORITY1 "\ntotal: $ctGenesLostRefSeq\n\n";

    print STATS_PRIORITY1 "\n\nList of genes now having RefSeq acc but used to have none ReSeq:\n";
    print STATS_PRIORITY1 "-------------------------------------------------------------------\n";

    my $ctGenesGainRefSeq = 0;
    foreach my $zdbGeneId (@keysSortedByValues) {
        $symbol = $geneZDBidsSymbols{$zdbGeneId};
        if (exists($genesWithRefSeqAfterLoad{$zdbGeneId})
            && !exists($genesWithRefSeqBeforeLoad{$zdbGeneId})) {
            $ctGenesGainRefSeq++;
            print STATS_PRIORITY1 "$zdbGeneId\n";

        }
    }

    print STATS_PRIORITY1 "\ntotal: $ctGenesGainRefSeq\n\n\n";
    close STATS_PRIORITY1;
    close STATS_PRIORITY2;

    #combine the contents of the two files into one
    open(STATS_PRIORITY1, "<reportStatistics_p1") or die "Cannot open reportStatistics_p1 : $!\n";
    open(STATS_PRIORITY2, "<reportStatistics_p2") or die "Cannot open reportStatistics_p2 : $!\n";
    my $outputBuffer = "";
    while(<STATS_PRIORITY1>) {
        $outputBuffer .= $_;
    }
    while(<STATS_PRIORITY2>) {
        $outputBuffer .= $_;
    }
    close STATS_PRIORITY1;
    close STATS_PRIORITY2;

    #replace the zdb ids with ID and symbol
    foreach my $zdbGeneId (@keysSortedByValues) {
        $symbol = $geneZDBidsSymbols{$zdbGeneId};
        $outputBuffer =~ s/$zdbGeneId([^\d])/$zdbGeneId($symbol)$1/g;
    }
    print STATS getArtifactComparisonURLs();
    print STATS $outputBuffer;

    close STATS;
    #delete the two files
    unlink "reportStatistics_p1";
    unlink "reportStatistics_p2";
}

sub emailLoadReports {
    my $subject = "Auto from $instance: NCBI_gene_load.pl :: Statistics";
    ZFINPerlModules->sendMailWithAttachedReport($SWISSPROT_EMAIL_REPORT,"$subject","reportStatistics");

    $subject = "Auto from $instance: NCBI_gene_load.pl :: log file";
    ZFINPerlModules->sendMailWithAttachedReport($SWISSPROT_EMAIL_ERR,"$subject","logNCBIgeneLoad");
}

sub writeHashOfArraysToFileForDebug {
    if (!$debug) {
        return;
    }

    my ($file, $hash_ref) = @_;
    open(my $fh, ">", $file) || die "Cannot open $file : $!\n";
    foreach my $key (sort keys %$hash_ref) {
        print $fh "$key\t" . joinElementsFromMaybeArray($hash_ref->{$key}, " ") . "\n";
    }
    close $fh;
}

sub isEmpty {
    my $array_ref = shift;
    if (ref($array_ref) eq "ARRAY") {
        return scalar(@$array_ref) == 0;
    }
    if (ref($array_ref) eq "HASH") {
        return scalar(keys %$array_ref) == 0;
    }
    return 0;
}

# This method creates a header to be added to the top of a report file that is sent via email.
# It includes URLs to compare the reports between the current build and the previous build.
# It assumes the jenkins environment variables JOB_URL and BUILD_ID are set.
# It requires artifact-diff plugin to be installed
sub getArtifactComparisonURLs {
    #eg. JOB_URL=http://SITE.zfin.org/jobs/job/test-job2/
    my $jobURL = $ENV{'JOB_URL'};
    my $buildDisplay = $ENV{'BUILD_DISPLAY_NAME'};

    if (!$jobURL || !$buildDisplay) {
        return "";
    }

    #buildDisplay is in the format "#1234"
    $buildDisplay =~ s/#//;

    #is buildDisplay numeric (after removing '#')?
    if ($buildDisplay !~ /^\d+$/) {
        return "";
    }

    #cast buildDisplay to int
    my $buildID = int($buildDisplay);

    #replace http with https if not already https
    $jobURL = "https" . substr($jobURL,4) if (substr($jobURL,0,5) eq "http:");

    my $previousBuildID = $buildID - 1;
    my $artifactSubDirectory = "server_apps/data_transfer/NCBIGENE";

    #array of artifacts
    my @artifacts = ("reportNtoN", "reportNtoOne", "reportOneToN", "reportOneToZero", "reportStatistics", "reportNonLoadPubGenPept", "reportZDBgeneIdWithMultipleVegaIds");

    my $buffer = "See below URLs to compare reports between this build and the previous build:\n";
    $buffer .=   "===========================================================================\n\n";
    foreach my $artifact (@artifacts) {
        #eg. https://SITE.zfin.org/jobs/job/NCBI-Gene-Load_w/2/artifact-diff/3/server_apps/data_transfer/reportNtoN
        $buffer .= $jobURL . $previousBuildID . "/artifact-diff/$buildID/$artifactSubDirectory/$artifact\n";
    }
    $buffer .= "===========================================================================\n\n";

    return $buffer;
}

sub percentageDisplay {
    my $before = shift;
    my $after = shift;
    return ($before > 0) ? sprintf("%.2f%%", ($after - $before) / $before * 100) : "N/A";
}

main();
