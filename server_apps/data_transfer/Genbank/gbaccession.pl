#!/opt/zfin/bin/perl -w
#
# The script gets GenBank daily update flat file, parses it,
# and updates the acc information in accession_bank and db_link.
# In the case the script is run from zfin.org, the resulted FASTA
# files as well as the original flat file are moved to
# /research/zblastfiles/files/daily for weekly BLAST db updates.
#
use strict;
use Try::Tiny;
use POSIX;
use FindBin;
use lib "$FindBin::Bin/../../perl_lib/";
use ZFINPerlModules qw(assertEnvironment);
assertEnvironment('ROOT_PATH', 'DB_NAME', 'PGBINDIR', 'PGHOST');

my ($mailprog, $md_date, $prefix, $unzipfile, $newfile, $dir_on_development_machine, $accfile, $report);

my $GENBANK_DAILY_EMAIL = '<!--|GENBANK_DAILY_EMAIL|-->';
if ($ENV{'$GENBANK_DAILY_EMAIL'}) {
    $GENBANK_DAILY_EMAIL = $ENV{'GENBANK_DAILY_EMAIL'};
    print("Using GENBANK_DAILY_EMAIL from environment variable: $GENBANK_DAILY_EMAIL\n");
}

my $MOVE_BLAST_FILES_TO_DEVELOPMENT = "<!--|MOVE_BLAST_FILES_TO_DEVELOPMENT|-->";
if ($ENV{'MOVE_BLAST_FILES_TO_DEVELOPMENT'}) {
    $MOVE_BLAST_FILES_TO_DEVELOPMENT = $ENV{'MOVE_BLAST_FILES_TO_DEVELOPMENT'};
    print("Using MOVE_BLAST_FILES_TO_DEVELOPMENT from environment variable: $MOVE_BLAST_FILES_TO_DEVELOPMENT\n");
}

$mailprog = '/usr/lib/sendmail -t -oi -oem';

chdir "$ENV{'ROOT_PATH'}/server_apps/data_transfer/Genbank/";

$report = "acc_update.report";

#remove old files
system("/bin/rm -f $report");
system("/bin/rm -f *.unl");
system("/bin/rm -f *.fa");
if (!$ENV{'KEEP_FLAT'}) {
    system("/bin/rm -f *.flat")
}

if (@ARGV > 0) {
    $md_date = $ARGV[0];
}
else {
    $md_date = `date +%m%d`;
    chop($md_date);
}
$prefix = "nc$md_date";
$unzipfile = "$prefix.flat";
$newfile = $unzipfile.".gz";
$accfile = $prefix."_zf_acc.unl";

#get daily update file
if (-e "$newfile") {
    print "File $newfile already exists.  Skipping download.\n";
}

my $count = 0;
my $retry = 1;
#verify the file is downloaded
while( !(-e "$newfile") && $retry < 5){

  $count++;
  if ($count > 10) {

	  $count = 0;
	  $retry = $retry +1;
	  &downloadDailyUpdateFile();
	  
  }
}

if (!(-e "$newfile")) {
    die "failed to download genbank file" ;
}

if (-e "$unzipfile") {
    print "File $unzipfile already exists.  Skipping decompression.\n";
}

#decompress files
$count = 0;
#wait until the files are decompressed
while( !(-e "$unzipfile") ) {
    $count++;
    if ($count < 10){
        print "Extracting $newfile to $unzipfile \n";
        system("gunzip -c $newfile > $unzipfile") && die("gunzip failed");
    }
}

if (!(-e "$unzipfile")) {
    die "failed to extract genbank file" ;
}
print "File extracted to: $unzipfile\n";


# parse out accession number, length, datatype for zebrafish records,
# also parse out flat file into several fasta files for blast db update

print "Running parseDaily.pl on $unzipfile \n";
system ("parseDaily.pl $unzipfile")  &&  &writeReport("parseDaily.pl failed.");


# only move the FASTA files and flat files to development_machine if that script
# is run from production.

$dir_on_development_machine = "/research/zblastfiles/files/daily" ;


if ($MOVE_BLAST_FILES_TO_DEVELOPMENT eq "true") {
    print "Moving blast files to development \n";
    if (! system ("/bin/mv *.fa *.flat $dir_on_development_machine") ) {

		&writeReport("Fasta files moved to development_machine.");
		system ("/bin/touch $dir_on_development_machine/fileMoved.$md_date");
	}
	else {
		&writeReport("Failed to move the fasta files to development_machine.");
	}
}
# rename daily zebrafish accession file and use that to update the database
if (! system ("/bin/mv $accfile nc_zf_acc.unl")) {
    print "Running psql to load the unload files.\n";

    # load the updates into accesson_bank and db_link
    try {
        ZFINPerlModules->doSystemCommand("$ENV{'PGBINDIR'}/psql --echo-all -v ON_ERROR_STOP=1 -h $ENV{'PGHOST'} $ENV{'DB_NAME'} < GenBank-Accession-Update_d.sql >> $report 2>&1");
    } catch {
        warn "Failed at GenBank-Accession-Update_d.sql - $_";
        exit -1;
    };

} else {
    &writeReport("Failed to rename the daily accession file.");
}


&sendReport();

###########################################################
#

sub downloadDailyUpdateFile() {
    print "Running: wget -q ftp://ftp.ncbi.nlm.nih.gov/genbank/daily-nc/$newfile;\n";
    system("wget -q ftp://ftp.ncbi.nlm.nih.gov/genbank/daily-nc/$newfile;");
}

sub emailError() {
    &writeReport($_[0]);
    &sendReport();
    exit;
}

sub writeReport() {
    open (REPORT, ">>$report") or die "cannot open report";
    print REPORT "$_[0] \n\n";
    close (REPORT);
}

sub sendReport() {
    open(MAIL, "| $mailprog") || die "cannot open mailprog $mailprog, stopped";
    open(REPORT, "$report") || die "cannot open report";

    print MAIL "To: $GENBANK_DAILY_EMAIL";
    print MAIL "\nSubject: GenBank accession update report\n";
    while(my $line = <REPORT>)
    {
      print MAIL $line;
    }
    close (REPORT);
    close (MAIL);
  }


