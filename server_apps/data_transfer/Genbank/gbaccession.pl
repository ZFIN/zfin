#!/private/bin/perl 
#
# Script to get genbank daily update and update the accession information 
# in accessoin_bank and db_link. 
#

my ($mailprog, $md_date, $prefix, $upzipfile, $newfile, $dir_on_bionix, $accfile, $report);

$mailprog = '/usr/lib/sendmail -t -oi -oem';

#set environment variables
$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";

chdir "<!--|ROOT_PATH|-->/server_apps/data_transfer/Genbank/";

# a place on bionix is used to store the fasta files for blast db update.
$dir_on_bionix = "/research/zblastfiles/files/daily/" ;

$report = "acc_update.report";

#remove old files
system("/bin/rm -f $report");
system("/bin/rm -f *.unl");
system("/bin/rm -f *.fa");

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
	  &emailError("Failed to download Genbank daily update file.") 
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

system ("parseDaily.pl $unzipfile");


if (! system ("/bin/mv *.fa *.flat $dir_on_bionix") ) {

    &writeReport("Fasta files moved to bionix."); 
    system ("/bin/touch $dir_on_bionix/fileMoved.$md_date");
}
else {
     &writeReport("Failed to move the fasta files to bionix.");
 }

if (! system ("/bin/mv $accfile nc_zf_acc.unl")) {

  # load the updates into accesson_bank and db_link
  system("$ENV{'INFORMIXDIR'}/bin/dbaccess <!--|DB_NAME|--> dailyUpdate.sql > $report 2>&1");
} else {
     &writeReport("Failed to rename the daily accession file.");
 }

&sendReport();


###########################################################
#

sub downloadDailyUpdateFile() {
    my $file = $_[0];
    print "download: $file\n";
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

    print MAIL "To: peirans\@cs.uoregon.edu\n";
    print MAIL "Subject: Genbank accession update report\n";
    while($line = <REPORT>)
    {
      print MAIL $line;
    }
    close (REPORT);
    close (MAIL);
  }








