#!/opt/zfin/bin/perl
use strict ;

#
# pre_loadsp.pl
#
# Use environment variable "SKIP_DOWNLOADS" to tell script not to download anything and instead assume files exist locally (will exit if no file exists)
# Use environment variable "SKIP_MANUAL_CHECK" to tell script not to scan uniprot for IDs gone bad
# Use environment variable "SKIP_CLEANUP" to tell script not to remove old files
# Use environment variable "SKIP_SLEEP" to tell script not to sleep for 500 seconds at various parts
# Use environment variable "SKIP_PRE_ZFIN_GEN" to tell script not to generate pre_zfin.dat
# Use environment variable "ARCHIVE_ARTIFACTS" to tell script to save a copy of all generated artifacts
# Flags are useful for being a good citizen and not putting too much strain on servers.
#
# Can run with those env vars in tcsh like so: (https://stackoverflow.com/questions/5946736/)
#    env SKIP_DOWNLOADS=1 env SKIP_MANUAL_CHECK=1 ./runUniprotPreload.sh
# or
#     ( setenv SKIP_DOWNLOADS 1 ; setenv SKIP_MANUAL_CHECK 1; setenv SKIP_CLEANUP 1 ; setenv SKIP_SLEEP 1 ;  setenv SKIP_PRE_ZFIN_GEN 1 ; ./runUniprotPreload.sh )
#

use MIME::Lite;
use LWP::Simple;
use DBI;
use Try::Tiny;
use POSIX;


use lib $ENV{'ROOT_PATH'} . "/server_apps/";
use ZFINPerlModules qw(assert_environment trim);
assert_environment('ROOT_PATH', 'PGHOST', 'DB_NAME', 'SWISSPROT_EMAIL_ERR', 'SWISSPROT_EMAIL_REPORT');

#------------------- Flush Output Buffer --------------
$|=1;

#------------------- Download -----------

sub downloadOrUseLocalFile {
        my ($url, $outfile) = @_;
        if ($ENV{"SKIP_DOWNLOADS"}) {
            print("Skipping download '$url' to '$outfile'\n");
            my $outfileWithoutGz = $outfile  =~ s/\.gz$//r;
            if (!-e $outfile && !-e $outfileWithoutGz) {
                print "*************************************************\n";
                print "* ERROR: The $outfile file does not exist, but we are running with SKIP_DOWNLOADS flag\n";
                print "*************************************************\n";
                exit -1;
            } elsif (!-e $outfile && -e $outfileWithoutGz) {
                print "$outfile file missing, continuing with $outfileWithoutGz\n";
            }
        } else {
            print("Downloading '$url' to '$outfile'\n");

            #set the number of bytes that a dot represents in wget progress bar to 10M
            system("/local/bin/wget --progress=dot -e dotbytes=10M '$url' -O '$outfile'");
        }
}

sub downloadGOtermFiles () {
   downloadOrUseLocalFile("http://www.geneontology.org/external2go/uniprotkb_kw2go", "spkw2go");
   downloadOrUseLocalFile("http://www.geneontology.org/external2go/interpro2go", "interpro2go");
   downloadOrUseLocalFile("http://www.geneontology.org/external2go/ec2go", "ec2go");

   if (!-e "spkw2go" || !-e "interpro2go" || !-e "ec2go") {
      print "One or more of the go translation files not exisiting. Exit.\n";
      exit -1;
   } else {
      print "\nDone with downloading the go translation files.\n\n\n";
   }

   &select_zebrafish ;

   if (!-e "pre_zfin.dat") {
      print "\nSomething is wrong with pre_zfin.dat. Exit.\n\n";
      exit -1;
   } else {
      print "\nDone with generating pre_zfin.dat at " . strftime("%Y-%m-%d %H:%M:%S", localtime(time())) . " \n\n";
   }

    if ($ENV{'SKIP_SLEEP'}) {
        print "Skipping sleep clause 1\n";
    } else {
        print "\nSleeping for 8 minutes at " . strftime("%Y-%m-%d %H:%M:%S", localtime(time())) . " \n\n";
       my $sleepNumDownload1 = 500;
       while($sleepNumDownload1--){
          sleep(1);
       }
   }

   system("touch pre_zfin.dat");

    if ($ENV{'SKIP_SLEEP'}) {
        print "Skipping sleep clause 2 (what is the purpose of these sleeps?)\n";
    } else {
        print "\nSleeping for another 8 minutes at " . strftime("%Y-%m-%d %H:%M:%S", localtime(time())) . " \n\n";
       my $sleepNumDownload2 = 500;
       while($sleepNumDownload2--){
          sleep(1);
       }
   }

   system("touch *2go");

 }

# ----------------- Send Error Report -------------
# Parameter
#   $    Error message

sub sendErrorReport ($) {
    my $subject = "Auto from SWISS-PROT:".$_[0];
    ZFINPerlModules->sendMailWithAttachedReport($ENV{'SWISSPROT_EMAIL_ERR'},"$subject","report.txt");
#    system("cp report.txt log");
}

#------------------ Send Running Result ----------------
# No parameter
#
sub sendRunningResult {
  my $dbname = $_[0];
  #----- One mail send out the checking report----
  my $subject = "Auto from $dbname: SWISS-PROT check report";
  ZFINPerlModules->sendMailWithAttachedReport($ENV{'SWISSPROT_EMAIL_REPORT'},"$subject","checkreport.txt");
#  system("cp checkreport.txt log");

  #----- Another mail send out problem files ----
  $subject = "Auto from $dbname: SWISS-PROT problem file";
  ZFINPerlModules->sendMailWithAttachedReport($ENV{'SWISSPROT_EMAIL_REPORT'},"$subject","allproblems.txt");
#  system("cp allproblems.txt log");

  #----- Another mail send out problem files ----
  $subject = "Auto from $dbname: PubMed not in ZFIN";
  ZFINPerlModules->sendMailWithAttachedReport($ENV{'SWISSPROT_EMAIL_REPORT'},"$subject","pubmed_not_in_zfin");
#  system("cp pubmed_not_in_zfin log");

  #----- Another mail send out problem files ----
  $subject = "Auto from $dbname: report of processing pre_zfin.org";
  ZFINPerlModules->sendMailWithAttachedReport($ENV{'SWISSPROT_EMAIL_REPORT'},"$subject","redGeneReport.txt");
#  system("cp redGeneReport.txt log");
}


# ====================================
#
# Extracts only zfin data from vertebrates.
#
sub select_zebrafish {
    #where to find the uniprot files (can override with env var, for example with a file:/// URL)
    my $TREMBL_FILE_URL = "https://ftp.expasy.org/databases/uniprot/current_release/knowledgebase/taxonomic_divisions/uniprot_trembl_vertebrates.dat.gz";
    if ($ENV{'TREMBL_FILE_URL'}) {
        $TREMBL_FILE_URL = $ENV{'TREMBL_FILE_URL'};
    } elsif ($ENV{'SKIP_DOWNLOADS'}) {
        assert_file_exists('./uniprot_trembl_vertebrates.dat.gz', 'Missing uniprot tremble file and SKIP_DOWNLOADS set to 1');
        $TREMBL_FILE_URL = 'file://' . `pwd` . '/uniprot_trembl_vertebrates.dat.gz';
    }

    my $SPROT_FILE_URL = "https://ftp.expasy.org/databases/uniprot/current_release/knowledgebase/taxonomic_divisions/uniprot_sprot_vertebrates.dat.gz";
    if ($ENV{'SPROT_FILE_URL'}) {
        $SPROT_FILE_URL = $ENV{'SPROT_FILE_URL'};
    } elsif ($ENV{'SKIP_DOWNLOADS'}) {
        assert_file_exists('./uniprot_sprot_vertebrates.dat.gz', 'Missing uniprot tremble file and SKIP_DOWNLOADS set to 1');
        $SPROT_FILE_URL = 'file://' . `pwd` . '/uniprot_sprot_vertebrates.dat.gz';
    }

    if ($ENV{'SKIP_PRE_ZFIN_GEN'}) {
        print "Skipping generation of pre_zfin.dat file for troubleshooting purposes.  Assuming an accurate pre_zfin.dat file already exists.\n";
    } else {
        $/ = "\/\/\n"; #custom record separator
        open OUTPUT, ">pre_zfin.dat" or die "Cannot open pre_zfin.dat";
        my $record;

        ######## BEGIN PROCESSING: uniprot_trembl_vertebrates ##########
        print("Processing uniprot_trembl_vertebrates.dat.gz at " . strftime("%Y-%m-%d %H:%M:%S", localtime(time())) . " \n");
        print("Using URL of $TREMBL_FILE_URL");
        $cache_tee = "";
        if ($ENV{'CACHE_DOWNLOADS'}) {
            $cache_tee = " | tee uniprot_trembl_vertebrates.dat.gz ";
        }
        open(DAT1, "curl -s '$TREMBL_FILE_URL' $cache_tee | gunzip -c |") || die("Could not open uniprot_trembl_vertebrates.dat.gz $!");
        while ($record = <DAT1>){
           ZFINPerlModules->printWhirleyToStderr();
           print OUTPUT "$record" if $record =~ m/OS   Danio rerio/;
        }
        close(DAT1) ;
        print("Done processing uniprot_trembl_vertebrates.dat.gz at " . strftime("%Y-%m-%d %H:%M:%S", localtime(time())) . " \n");
        ######## END PROCESSING: uniprot_trembl_vertebrates ###########

        ######## BEGIN PROCESSING: uniprot_sprot_vertebrates ##########
        print("Processing uniprot_sprot_vertebrates.dat.gz at " . strftime("%Y-%m-%d %H:%M:%S", localtime(time())) . "\n");
        print("Using URL of $SPROT_FILE_URL");
        $cache_tee = "";
        if ($ENV{'CACHE_DOWNLOADS'}) {
            $cache_tee = " | tee uniprot_sprot_vertebrates.dat.gz ";
        }
        open(DAT2, "curl -s '$SPROT_FILE_URL' $cache_tee | gunzip -c |") || die("Could not open uniprot_sprot_vertebrates.dat.gz $!");
        while ($record = <DAT2>){
           ZFINPerlModules->printWhirleyToStderr();
           print OUTPUT "$record" if $record =~ m/OS   Danio rerio/;
        }
        close(DAT2) ;
        print("Done processing uniprot_sprot_vertebrates.dat.gz at " . strftime("%Y-%m-%d %H:%M:%S", localtime(time())) . " \n");
        ######## END PROCESSING: uniprot_sprot_vertebrates ###########

        $/ = "\n";
        close(OUTPUT) ;
    }

}


sub getMergedGeneIdIfExists {
    my $dbh = $_[0];
    my $geneId = $_[1];
    my $cur = $dbh->prepare('SELECT zrepld_new_zdb_id, mrkr_abbrev FROM zdb_replaced_data LEFT JOIN marker on zrepld_new_zdb_id = mrkr_zdb_id WHERE zrepld_old_zdb_id = ?;');
    $cur->execute($geneId);
    my $mergedGeneId;
    my $mergedGeneAbbrev;
    $cur->bind_columns(\$mergedGeneId, \$mergedGeneAbbrev);
    $cur->fetch();
    $cur->finish();
    return ($mergedGeneId, $mergedGeneAbbrev);
}


#=======================================================
#
#   Main
#


chdir $ENV{'ROOT_PATH'} . "/server_apps/data_transfer/SWISS-PROT/";


#remove old files
if ($ENV{'SKIP_CLEANUP'}) {
    print "Skipping file cleanup\n";
} else {
    print "Cleaning up old files\n";
    system("rm -f ./ccnote/*");
    system("rmdir ./ccnote");
    system("rm -f *.ontology");
    system("rm -f *2go");
    system("rm -f prob*");
    system("rm -f okfile");
    system("rm -f pubmed_not_in_zfin");
    system("rm -f *.unl");
    system("rm -f *.txt");
    system("rm -f *.dat.gz");
    system("mkdir ./ccnote");
}

my $dbname = $ENV{'DB_NAME'};
my $dbhost = $ENV{'PGHOST'};
my $username = "";
my $password = "";

########################################################################################################
#
#  for FB case 15015, Validate manually-added UniProt IDs attributed to the new publication, ZDB-PUB-170131-9
#
########################################################################################################

### open a handle on the db
my $dbh = DBI->connect ("DBI:Pg:dbname=$dbname;host=$dbhost", $username, $password)
    or die "Cannot connect to PostgreSQL database: $DBI::errstr\n";

my $sqlGetManuallyEnteredUniProtIDsWithMultGenes = "select distinct db1.dblink_acc_num from db_link db1
                                                     where exists(select 'x' from record_attribution
                                                                   where db1.dblink_zdb_id = recattrib_data_zdb_id
                                                                     and recattrib_source_zdb_id = 'ZDB-PUB-170131-9')
                                                       and exists(select 'x' from db_link db2
                                                                   where db2.dblink_acc_num = db1.dblink_acc_num
                                                                     and db2.dblink_linked_recid != db1.dblink_linked_recid);";

my $curGetManuallyEnteredUniProtIDsWithMultGenes = $dbh->prepare_cached($sqlGetManuallyEnteredUniProtIDsWithMultGenes);
$curGetManuallyEnteredUniProtIDsWithMultGenes->execute();
my $manuallyEnteredUniProtIDWithMultGenes;
$curGetManuallyEnteredUniProtIDsWithMultGenes->bind_columns(\$manuallyEnteredUniProtIDWithMultGenes);
open MULTIPLE, ">manuallyCuratedUniProtIDsWithMultipleGenes.txt" || die ("Cannot open manuallyCuratedUniProtIDsWithMultipleGenes.txt !");
my $ctManuallyEnteredUniProtIDsWithMultGenes = 0;
while ($curGetManuallyEnteredUniProtIDsWithMultGenes->fetch()) {
    print MULTIPLE "$manuallyEnteredUniProtIDWithMultGenes\n";
    $ctManuallyEnteredUniProtIDsWithMultGenes++;
}
$curGetManuallyEnteredUniProtIDsWithMultGenes->finish();

close(MULTIPLE);

print "\nNumber of manually curated UniProt IDs with multiple genes: $ctManuallyEnteredUniProtIDsWithMultGenes at " . strftime("%Y-%m-%d %H:%M:%S", localtime(time())) . " \n\n";

if ($ctManuallyEnteredUniProtIDsWithMultGenes > 0) {
  my $subject = "Auto from SWISS-PROT: manually curated UniProt IDs with multiple genes";
  ZFINPerlModules->sendMailWithAttachedReport($ENV{'SWISSPROT_EMAIL_REPORT'},"$subject","manuallyCuratedUniProtIDsWithMultipleGenes.txt");
#  system("cp manuallyCuratedUniProtIDsWithMultipleGenes.txt log/");
}

my $sqlGetManuallyEnteredUniProtIDs = "select dblink_acc_num from db_link
                                        where exists(select 'x' from record_attribution
                                                      where dblink_zdb_id = recattrib_data_zdb_id
                                                        and recattrib_source_zdb_id = 'ZDB-PUB-170131-9');";

my $curGetManuallyEnteredUniProtIDs = $dbh->prepare_cached($sqlGetManuallyEnteredUniProtIDs);
$curGetManuallyEnteredUniProtIDs->execute();
my $manuallyEnteredUniProtID;
$curGetManuallyEnteredUniProtIDs->bind_columns(\$manuallyEnteredUniProtID);
my @manuallyEnteredUniProtIDs = ();
my $ctManuallyEnteredUniProtIDs = 0;
open MCIDS, ">manuallyCuratedUniProtIDs.txt" || die ("Cannot open manuallyCuratedUniProtIDs.txt !");
while ($curGetManuallyEnteredUniProtIDs->fetch()) {
    $manuallyEnteredUniProtIDs[$ctManuallyEnteredUniProtIDs] = $manuallyEnteredUniProtID;
    print MCIDS "$manuallyEnteredUniProtID\n";
    $ctManuallyEnteredUniProtIDs++;
}
$curGetManuallyEnteredUniProtIDs->finish();
close(MCIDS);

print "\nNumber of manually curated UniProt IDs: $ctManuallyEnteredUniProtIDs at " . strftime("%Y-%m-%d %H:%M:%S", localtime(time())) . "\n\n";

my $uniprotId;
my $url;
my $uniProtURL = "https://www.uniprot.org/uniprot/";

if ($ENV{"SKIP_MANUAL_CHECK"}) {
    print("Skipping check for invalid manually entered uniprot IDs\n");
} else {
    open INVALID, ">invalidManuallyCuratedUniProtIDs.txt" || die ("Cannot open invalidManuallyCuratedUniProtIDs.txt !");
    my $numInvalidUniProtIDs = 0;


    if ($ctManuallyEnteredUniProtIDs > 0) {
      print "Checking for invalid manually entered uniprot IDs at " . strftime("%Y-%m-%d %H:%M:%S", localtime(time())) . " \n";
      foreach $uniprotId (@manuallyEnteredUniProtIDs) {
          ZFINPerlModules->printWhirleyToStderr();
         $url = $uniProtURL . $uniprotId;
         my $status_code = getstore($url, "/dev/null");
         if ($status_code != 200) {
              print INVALID "$uniprotId\n";
              $numInvalidUniProtIDs++;
          }
          undef $status_code;
      }
      print("\n");
    }

    close(INVALID);

    print "\nNumber of Invalid Manually curated UniProt IDs: $numInvalidUniProtIDs\n\n";

    if ($numInvalidUniProtIDs > 0) {
      my $subject = "Auto from SWISS-PROT: invalid manually curated UniProt IDs";
      ZFINPerlModules->sendMailWithAttachedReport($ENV{'SWISSPROT_EMAIL_REPORT'},"$subject","invalidManuallyCuratedUniProtIDs.txt");
#      system("cp invalidManuallyCuratedUniProtIDs.txt log");
    }
}

&downloadGOtermFiles();

print "Finished downloading GO term files at " . strftime("%Y-%m-%d %H:%M:%S", localtime(time())) . " \n";

########################################################################################################
#
#  for FB case 8042 UniProt load: DR lines redundancy in the input file
#
########################################################################################################

print "Reading pre_zfin.dat and generating zfin.dat, zfinGeneDeleted.dat, zfinGenes.dat, debugfile.dat  " . strftime("%Y-%m-%d %H:%M:%S", localtime(time())) . " \n";

my $cur;

$/ = "//\n";
open(PREDAT, "pre_zfin.dat") || die("Could not open pre_zfin.dat !");
my @blocks = <PREDAT>;
close(PREDAT);
print STDERR "Processing pre_zfin.dat\n";

open ZFINDAT, ">zfin.dat" || die ("Cannot open zfin.dat !");
open ZFINDATDELETED, ">zfinGeneDeleted.dat" || die ("Cannot open zfinGeneDeleted.dat !");
open ZFINGENES, ">zfinGenes.dat" || die ("Cannot open zfinGenes.dat !");
open DBG, ">debugfile.dat" || die ("Cannot open debugfile.dat !");

my $totalOnZfinDat = 0;
my $totalOnDeleted = 0;
my $ttt = 0;
my @lines = ();
my %toNewInput = ();
my %deletes = ();
my $ct = 0;
my %ZDBgeneIDgeneAbbrevs = ();
my $line;
my $lineKey = 0;
my @fields = ();
my $ZFINgeneId;
my $geneAbbrev;
my $block;
my $newLineNumber;
my $key;
foreach $block (@blocks) {
   $ttt++;
   if($block !~ m/OS   Danio rerio/) {
       print "ERROR: Found non-zebrafish record! Ignoring.\n";
       next;
   }
    @lines = split(/\n/, $block);
    %toNewInput = ();
    %deletes = ();
    $ct = 0;
    %ZDBgeneIDgeneAbbrevs = ();
    foreach $line (@lines) {
       if($line =~ m/CC   (-------)|(Copyrighted)|(Distributed)/ ) {
           #Skip copyright lines
           next;
       }

       ## add 10000 to pad so that the sorting would be right
       $lineKey = 10000 + $ct;
       $toNewInput{$lineKey} = $line;
       $deletes{$lineKey} = 0;

       if ($line !~ m/DR   ZFIN; ZDB-GENE-/) {
           #Skip lines that are not DR ZFIN lines
           next;
       }

       @fields = split(/;/, $line);
       $ZFINgeneId = trim($fields[1]);
       $geneAbbrev = trim($fields[2]);
       $geneAbbrev =~ s/\.$//;

       ### Some Debug Statements For First 1000 Records ###
       if ($ttt < 1000) {
           print DBG "ZFINgeneId : $ZFINgeneId \t geneAbbrev : $geneAbbrev  \n";
           if (exists($ZDBgeneIDgeneAbbrevs{$ZFINgeneId})) {
               print DBG "exists  $ZDBgeneIDgeneAbbrevs{$ZFINgeneId} \n";
           } else {
               print DBG "Not exists  \$ZDBgeneIDgeneAbbrevs{$ZFINgeneId} \n";
           }
       }

       if (!exists($ZDBgeneIDgeneAbbrevs{$ZFINgeneId})) {
           $cur = $dbh->prepare('select mrkr_abbrev from marker where mrkr_zdb_id = ?;');

           $cur->execute($ZFINgeneId);
           my ($ZFINgeneAbbrev);
           $cur->bind_columns(\$ZFINgeneAbbrev);
           while ($cur->fetch()) {
               $ZDBgeneIDgeneAbbrevs{$ZFINgeneId} = $ZFINgeneAbbrev;
           }
           $cur->finish();

           if (!defined($ZFINgeneAbbrev)) {
               my ($mergedGeneId, $mergedGeneAbbrev) = getMergedGeneIdIfExists($dbh, $ZFINgeneId);
               if ($mergedGeneId) {
                   print DBG "Merged '$ZFINgeneId' ($geneAbbrev) to '$mergedGeneId' ($mergedGeneAbbrev) \n";
                   print DBG "   Old Line: $line\n";
                   $line =~ s/$geneAbbrev/$mergedGeneAbbrev/g;
                   $line =~ s/$ZFINgeneId/$mergedGeneId/g;
                   print DBG "   New Line: $line\n";
                   $ZFINgeneAbbrev = $geneAbbrev;

                   if (exists($ZDBgeneIDgeneAbbrevs{$mergedGeneId})) {
                       $deletes{$lineKey} = 1;
                       next;
                   }
               } else {
                   print DBG "ERROR: Search for gene abbreviation failed for zdb id '$ZFINgeneId'";
                   die("ERROR: Search for gene abbreviation failed for zdb id '$ZFINgeneId'");
               }
           }

           if ($geneAbbrev ne $ZFINgeneAbbrev) {
               my $ZFINgeneAbbrevDebug = defined($ZFINgeneAbbrev) ? $ZFINgeneAbbrev : "UNDEFINED";
               print DBG "Gene Abbreviation '$geneAbbrev' is not the same as ZFIN Gene Abbreviation ($ZFINgeneAbbrevDebug)! Updating. \n";
               $line =~ s/$geneAbbrev/$ZFINgeneAbbrev/g;
           }
           $toNewInput{$lineKey} = $line;
       } else {
           # Is this ever true? It seems we would only ever get here if one record had multiple lines
           # with the same "DR   ZFIN; ZDB-GENE-..." information
           print DBG "Possible ERROR? Multiple lines in the same record with the same gene info: ";
           print DBG "geneAbbrev $geneAbbrev; ZDB $ZFINgeneId; recordNum $ttt \n";
           $deletes{$lineKey} = 1;
       }
       ZFINPerlModules->printWhirleyToStderr();
    } continue {
        $ct++;
    }

    foreach $newLineNumber (sort keys %toNewInput) {
       if ($deletes{$newLineNumber} == 0) {
           print ZFINDAT "$toNewInput{$newLineNumber}\n";
           $totalOnZfinDat = $totalOnZfinDat + 1;
       } else {
           print ZFINDATDELETED "$toNewInput{$newLineNumber}\n";
           $totalOnDeleted++;
       }
    }

    foreach $key (sort keys %ZDBgeneIDgeneAbbrevs) {
           print ZFINGENES "$ZDBgeneIDgeneAbbrevs{$key}\n";
    }

}

close(ZFINDAT);
close(ZFINDATDELETED);
close(ZFINGENES);
close(DBG);

$dbh->disconnect();
print "Finished generating output .dat files at " . strftime("%Y-%m-%d %H:%M:%S", localtime(time())) . " \n";


open(PRE, "pre_zfin.dat") || die("Could not open pre_zfin.dat !");
my @blocksPRE = <PRE>;
close(PRE);
my $prezfin = 0;
my $totalprezfin = 0;

foreach $block (@blocksPRE){
   $totalprezfin++;
   if($block =~ m/OS   Danio rerio/) {
      $prezfin++;
   }
}

open(ZF, "zfin.dat") || die("Could not open zfin.dat !");
my @blocksZF = <ZF>;
close(ZF);
my $zfin = 0;
my $totalzfin = 0;
foreach $block (@blocksZF){
   $totalzfin++;
   if($block =~ m/OS   Danio rerio/) {
      $zfin++;
   }
}

$/ = "\n";

open(PRE, "pre_zfin.dat") || die("Could not open pre_zfin.dat !");
my @blocksPRE = <PRE>;
close(PRE);
my $prezfinGene = 0;
my $prezfinLines = 0;
foreach $block (@blocksPRE){
   $prezfinLines++;
   if($block =~ m/DR   ZFIN; ZDB-GENE-/) {
      $prezfinGene++;
   }
}

open(ZF, "zfin.dat") || die("Could not open zfin.dat !");
my @blocksZF = <ZF>;
close(ZF);
my $zfinGene = 0;
my $zfinLines = 0;
foreach $block (@blocksZF){
   $zfinLines++;
   if($block =~ m/DR   ZFIN; ZDB-GENE-/) {
      $zfinGene++;
   }
}

print "totalOnZfinDat = $totalOnZfinDat\t totalOnDeleted = $totalOnDeleted\n\n\n";
print "prezfin = $prezfin\t";
print "totalprezfin = $totalprezfin\n";
print "zfin = $zfin\t";
print "totalzfin = $totalzfin\n";

print "\n\n\n";
print "prezfinGene = $prezfinGene\t";
print "prezfinLines = $prezfinLines\n";
print "zfinGene = $zfinGene\t";
print "zfinLines = $zfinLines\n";

open(REDGENERPT, ">redGeneReport.txt") || die("Could not open redGeneReport.txt !");
print REDGENERPT "totalOnZfinDat = $totalOnZfinDat\t totalOnDeleted = $totalOnDeleted\n\n\n";
print REDGENERPT "prezfin = $prezfin\t";
print REDGENERPT "totalprezfin = $totalprezfin\n";
print REDGENERPT "zfin = $zfin\t";
print REDGENERPT "totalzfin = $totalzfin\n";

print REDGENERPT "\n\n\n";
print REDGENERPT "prezfinGene = $prezfinGene\t";
print REDGENERPT "prezfinLines = $prezfinLines\n";
print REDGENERPT "zfinGene = $zfinGene\t";
print REDGENERPT "zfinLines = $zfinLines\n";

close REDGENERPT;

exit;

# Comments on 11/22/2021 by Ryan T.
#
# Some small TODO items that aren't worth creating a ticket, but might be nice to clean up this file a bit:
#
# Emails going out from this seem to expect the files "report.txt", "checkreport.txt", "allproblems.txt",
# "pubmed_not_in_zfin" to exist, but are not generated directly by this script. They are generated by later scripts,
# but not sure why this script would email those artifacts.  Might be a bug?
#
# Structurally, it's a bit confusing. Might be nice to wrap some of the steps up into subroutines. And maybe adopt a
# pattern like the one described here: https://stackoverflow.com/questions/6763987
#
# Why do we sleep for 500 seconds in various places?
# Why do we touch various files?


