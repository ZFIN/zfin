#!/private/bin/perl

# FILE: Sanger.pl
# load data provided by Sanger 
# First, it does some sanity check such as to check if there is redundncy, invalid ZDBIds etc.
# If the number of errors is greater than 0, the script exits with only report and input file sent to Xiang
# Then it calls loadZfishbookData.sql to do the load and sends in email the records inserted into feature and genotype tables.

use MIME::Lite;
use DBI;


#------------------ Send Checking Result ----------------
# No parameter
#

sub sendReport {
		
  my $SUBJECT="Auto: Sanger sanity checking result";
  my $MAILTO="pm\@cs.uoregon.edu";
  my $TXTFILE="./report.txt";
 
  # Create a new multipart message:
  my $msg1 = new MIME::Lite 
    From    => "$ENV{LOGNAME}",
    To      => "$MAILTO",
    Subject => "$SUBJECT",
    Type    => 'multipart/mixed';
 
  attach $msg1 
   Type     => 'text/plain',   
   Path     => "$TXTFILE";

  # Output the message to sendmail

  open (SENDMAIL, "| /usr/lib/sendmail -t -oi");
  $msg1->print(\*SENDMAIL);

  my $SUBJECT="Auto: Sanger pre_load_input";
  my $MAILTO="pm\@cs.uoregon.edu";
  my $TXTFILE="./pre_load_input_known.txt";
 
  # Create a new multipart message:
  my $msg2 = new MIME::Lite 
    From    => "$ENV{LOGNAME}",
    To      => "$MAILTO",
    Subject => "$SUBJECT",
    Type    => 'multipart/mixed';
 
  attach $msg2 
   Type     => 'text/plain',   
   Path     => "$TXTFILE";

  # Output the message to sendmail

  open (SENDMAIL, "| /usr/lib/sendmail -t -oi");
  $msg2->print(\*SENDMAIL);

  close(SENDMAIL);
}

#------------------ Send output ----------------
# No parameter
#
sub sendPreLoadInput {

  my $SUBJECT="Auto: Sanger data new features";
  my $MAILTO="pm\@cs.uoregon.edu";
  my $TXTFILE="./pre_feature.unl";
 
  # Create a new multipart message:
  my $msg4 = new MIME::Lite 
    From    => "$ENV{LOGNAME}",
    To      => "$MAILTO",
    Subject => "$SUBJECT",
    Type    => 'multipart/mixed';
 
  attach $msg4 
   Type     => 'text/plain',   
   Path     => "$TXTFILE";

  # Output the message to sendmail

  open (SENDMAIL, "| /usr/lib/sendmail -t -oi");
  $msg4->print(\*SENDMAIL);
  
  my $SUBJECT="Auto: Sanger data new genes";
  my $MAILTO="pm\@cs.uoregon.edu";
  my $TXTFILE="./pre_gene.unl";
 
  # Create a new multipart message:
  my $msg3 = new MIME::Lite 
    From    => "$ENV{LOGNAME}",
    To      => "$MAILTO",
    Subject => "$SUBJECT",
    Type    => 'multipart/mixed';
 
  attach $msg3 
   Type     => 'text/plain',   
   Path     => "$TXTFILE";

  # Output the message to sendmail

  open (SENDMAIL, "| /usr/lib/sendmail -t -oi");
  $msg3->print(\*SENDMAIL);
 
 
  my $SUBJECT="Auto: Sanger data new genotypes";
  my $MAILTO="pm\@cs.uoregon.edu";
  my $TXTFILE="./pre_geno.unl";
 
  # Create a new multipart message:
  my $msg5 = new MIME::Lite 
    From    => "$ENV{LOGNAME}",
    To      => "$MAILTO",
    Subject => "$SUBJECT",
    Type    => 'multipart/mixed';
 
  attach $msg5 
   Type     => 'text/plain',   
   Path     => "$TXTFILE";

  # Output the message to sendmail

  open (SENDMAIL, "| /usr/lib/sendmail -t -oi");
  $msg5->print(\*SENDMAIL);


  close(SENDMAIL);
}


#=======================================================
#
#   Main
#


#set environment variables
$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";

chdir "<!--|ROOT_PATH|-->/server_apps/data_transfer/SangerMutants/";

$dbname = "<!--|DB_NAME|-->";
$username = "";
$password = "";

### open a handle on the db
$dbh = DBI->connect ("DBI:Informix:$dbname", $username, $password) 
    or die "Cannot connect to Informix database: $DBI::errstr\n";


#remove old files
 
system("rm -f *.unl");
system("rm -f *.txt");

print "\nRunning Sanger loading script ...\n\n";

open (INPUT1, "/research/zarchive/zarchive3/data_in/SangerMutants/InputData/allelezfin.csv") || die "Cannot open SangerData.csv : $!\n";
@lines=<INPUT1>;
close(SANGERDATA);
%alleles = ();
%ZDBgeneIDgeneAbbrevs = ();
$ct = 0 - 1;
$numErr = 0;
open (REPORT, ">report.txt") || die "Cannot open report : $!\n";
open (SANGERPRELOAD, ">pre_load_input_known.txt") || die "Cannot open pre_load_input.txt : $!\n";

foreach $line (@lines) {
  $ct++;
  next if $ct == 0;
  
  if ($line) {
    chop($line);
    undef (@fields);
    @fields = split(/\,/, $line); 
    $allele = $fields[0];
    $lineNum = substr($allele, 2);
    $geneID = $fields[1];
    $bkground = $fields[2];
    
   } 
    print SANGERPRELOAD "$allele|$geneID|$bkground|$lineNum|\n";
    
  }

undef @lines;

open (INPUT2, "/research/zarchive/zarchive3/data_in/SangerMutants/InputData/allelenozfin.csv") || die "Cannot open SangerDataNoZFIN.csv : $!\n";
@lines=<INPUT2>;
close(SANGERDATA);
%alleles = ();
%ZDBgeneIDgeneAbbrevs = ();
$ct = 0 - 1;
$numErr = 0;
open (REPORT, ">report.txt") || die "Cannot open report : $!\n";
open (SANGERPRELOADENSDARG, ">pre_load_input_ensdarg.txt") || die "Cannot open pre_load_input.txt : $!\n";

foreach $line (@lines) {
  $ct++;
  next if $ct == 0;
  
  if ($line) {
    chop($line);
    undef (@fields);
    @fields = split(/\,/, $line); 
    $allele = $fields[0];
    $lineNum = substr($allele, 2);
    $geneID = $fields[1];
    $bkground = $fields[2];
    
   } 
    print SANGERPRELOADENSDARG "$allele|$geneID|$bkground|$lineNum|\n";
    
  }

undef @lines;

open (INPUT3, "/research/zarchive/zarchive3/data_in/SangerMutants/InputData/EnsdargMatches.unl") || die "Cannot open EnsdargMatches.unl : $!\n";
@lines=<INPUT3>;
open (ENSDARGMATCHES, ">EnsdargMatches.txt") || die "Cannot open pre_load_input.txt : $!\n";
foreach $line (@lines) {
  if ($line) {
    chop($line);
    undef (@fields);
    @fields = split(/\|/, $line); 
    $ensdarg = $fields[0];
    $geneID = $fields[1];
   } 
    print ENSDARGMATCHES "$ensdarg|$geneID|\n";
    
  }

undef @lines;

$dbh->disconnect(); 

print  "\nnumber of error of pre_load checking:  $numErr\n";
print REPORT "\nnumErr:  $numErr\n";
close (REPORT);

sendReport();

exit if ($numErr > 0);

system("$ENV{'INFORMIXDIR'}/bin/dbaccess <!--|DB_NAME|--> loadSangerData.sql");

sendPreLoadInput();

exit;
