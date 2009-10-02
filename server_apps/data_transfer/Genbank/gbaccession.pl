#!/private/bin/perl -w
#
# The script gets GenBank daily update flat file, parses it,
# and updates the acc information in accession_bank and db_link.
# In the case the script is run from zfin.org, the resulted FASTA
# files as well as the original flat file are moved to
# /research/zblastfiles/files/daily for weekly BLAST db updates.
#
use strict;

my ($mailprog, $md_date, $prefix, $unzipfile, $newfile, $dir_on_embryonix, $accfile, $report);

$mailprog = '/usr/lib/sendmail -t -oi -oem';

#set environment variables
$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";

chdir "<!--|ROOT_PATH|-->/server_apps/data_transfer/Genbank/";

# a place on embryonix is used to store the fasta files for blast db update.

my $host = "<!--|DOMAIN_NAME|-->";
if ($host ne "zfin.org") {
    $dir_on_embryonix = "/research/zblastfiles/dev_files/daily" ;
}
else {
    $dir_on_embryonix = "/research/zblastfiles/files/daily" ;    
}

$report = "acc_update.report";

#remove old files
system("/bin/rm -f $report");
system("/bin/rm -f *.unl");
system("/bin/rm -f *.fa");
system("/bin/rm -f *.flat");

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
&downloadDailyUpdateFile($newfile);

my $count = 0;
my $retry = 1;
#verify the file is downloaded
while( !(-e "$newfile") ){

  $count++;
  if ($count > 10) {

      if ($retry) {
	  $count = 0;
	  $retry = 0;
	  &downloadDailyUpdateFile();
      }
      else {
	  &emailError("Failed to download GenBank daily update file.")
	  }
  }
}


#decompress files
system("/local/bin/gunzip $newfile");

$count = 0;
$retry = 1;
#wait until the files are decompressed
while( !(-e "$unzipfile") ) {
    $count++;
    if ($count > 10){
	if ($retry) {

	    $count = 0;
	    $retry = 0;
	    system("/local/bin/gunzip -f $newfile");
	}
	else{
	    &emailError("Gunzip failed to extract the file.")
	    }
    }
}



# parse out accession number, length, datatype for zebrafish records,
# also parse out flat file into several fasta files for blast db update

system ("parseDaily.pl $unzipfile")  &&  &writeReport("parseDaily.pl failed.");


# only move the FASTA files and flat files to embryonix if that script
# is run from production.


if ("<!--|DOMAIN_NAME|-->" eq "zfin.org") {
	if (! system ("/bin/mv *.fa *.flat $dir_on_embryonix") ) {

		&writeReport("Fasta files moved to embryonix.");
		system ("/bin/touch $dir_on_embryonix/fileMoved.$md_date");
	}
	else {
		&writeReport("Failed to move the fasta files to embryonix.");
	}
}
# rename daily zebrafish accession file and use that to update the database
if (! system ("/bin/mv $accfile nc_zf_acc.unl")) {
    
    # load the updates into accesson_bank and db_link
    system("$ENV{'INFORMIXDIR'}/bin/dbaccess <!--|DB_NAME|--> dailyUpdate.sql> > $report 2>&1");
} else {
    &writeReport("Failed to rename the daily accession file.");
}
	

&sendReport();


###########################################################
#

sub downloadDailyUpdateFile() {
    my $file = $_[0];
    #print "download: $file\n";
    system("/local/bin/wget -q ftp://ftp.ncbi.nih.gov/genbank/daily-nc/$newfile;");
}

sub emailError()
  {
    &writeReport($_[0]);
    &sendReport();
    exit;
  }

sub writeReport()
  {
    open (REPORT, ">>$report") or die "cannot open report";
    print REPORT "$_[0] \n\n";
    close (REPORT);
  }

sub sendReport()
  {
    open(MAIL, "| $mailprog") || die "cannot open mailprog $mailprog, stopped";
    open(REPORT, "$report") || die "cannot open report";

    print MAIL "To: informix\@cs.uoregon.edu\n";
    print MAIL "Subject: GenBank accession update report\n";
    while(my $line = <REPORT>)
    {
      print MAIL $line;
    }
    close (REPORT);
    close (MAIL);
  }
