#!/private/bin/perl

# FILE: zfishbook.pl
# load data provided by zfishbook 
# First, it does some sanity check such as to check if there is redundncy, invalid ZDBIds etc.
# If the numb er of errors is greater than 0, the script exits with only report and input file sent to Xiang
# Then it calls loadZfishbookData.sql to do the load and sends in email the records inserted into feature and genotype tables.

use MIME::Lite;
use DBI;


#------------------ Send Checking Result ----------------
# No parameter
#

sub sendReport {
		
  my $SUBJECT="Auto: zfishbook sanity checking result";
  my $MAILTO="xshao\@cs.uoregon.edu";
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

  my $SUBJECT="Auto: zfishbook pre_load_input";
  my $MAILTO="xshao\@cs.uoregon.edu";
  my $TXTFILE="./pre_load_input.txt";
 
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

  my $SUBJECT="Auto: zfishbook data new genotypes";
  my $MAILTO="xshao\@cs.uoregon.edu";
  my $TXTFILE="./pre_geno.unl";
 
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

  my $SUBJECT="Auto: zfishbook data new features";
  my $MAILTO="xshao\@cs.uoregon.edu";
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

  my $SUBJECT="Auto: zfishbook data updated feature notes";
  my $MAILTO="xshao\@cs.uoregon.edu";
  my $TXTFILE="./updatedFeaturesToCleanUpZfishbookComments";
 
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

chdir "<!--|ROOT_PATH|-->/server_apps/data_transfer/zfishbook/";

$dbname = "<!--|DB_NAME|-->";
$username = "";
$password = "";

### open a handle on the db
$dbh = DBI->connect ("DBI:Informix:$dbname", $username, $password) 
    or die "Cannot connect to Informix database: $DBI::errstr\n";


#remove old files
 
system("rm -f *.unl");
system("rm -f *.dat");

print "\nRunning zfishbook loading script ...\n\n";

open (INPUT, "zfishbookData.txt") || die "Cannot open zfishbookData.txt : $!\n";
@lines=<INPUT>;
close(ZFISHBOOKDATA);
%prviousNames = ();
%alleles = ();
%allelePrevs = (); 
%ZDBgeneIDgeneAbbrevs = ();
%ZDBfeatureIDfeatureAbbrevs = ();
%ZDBconstructIDconstructAbbrevs = ();
$ct = 0 - 1;
$numErr = 0;
open (REPORT, ">report.txt") || die "Cannot open report : $!\n";
open (FEATGENO, ">featureGenotypes") || die "Cannot open featureGenotypes : $!\n";
open (ZFISHBOOKPRELOAD, ">pre_load_input.txt") || die "Cannot open pre_load_input.txt : $!\n";

foreach $line (@lines) {
  $ct++;
  next if $ct == 0;
  
  if ($line) {
    chop($line);
    undef (@fields);
    @fields = split(/\|/, $line); 
    $prev = $fields[0];
    $prev =~ s/^\s+//; 
    $prev =~ s/\s+$//;
    if (exists $prviousNames{$prev}) {
       $numErr++;
       print REPORT "\n$ct :: previous nam: $prev\n";
    }  else {
       $prviousNames{$prev} = 1;
    }
    
    $lineNum = substr($prev, 3);
    
    $allele = $fields[1];
    $allele =~ s/^\s+//; 
    $allele =~ s/\s+$//;
    
    $allele =~ s/GT$/Gt/;
    
    if (exists $alleles{$allele}) {
       $numErr++;
       print REPORT "\n$ct :: allele: $allele\n";
    }  else {
       $alleles{$allele} = 1;
    }    

    if (exists $allelePrevs{$allele}) {
       $numErr++;
       print REPORT "\n$ct :: allele-prev : $allele $prev\n";
    }  else {
       $allelePrevs{$allele} = $prev;
    }    
    
    $geneId = $fields[2];
    $geneId =~ s/^\s+//; 
    $geneId =~ s/\s+$//; 

    ### check if the zdbIds of the genes are valid or not    
    if ($geneId =~ m/ZDB-GENE-/)  {
      $cur = $dbh->prepare('select mrkr_abbrev from marker where mrkr_zdb_id = ?;');
      $cur->execute($geneId);
      my ($ZFINgeneAbbrev);
      $cur->bind_columns(\$ZFINgeneAbbrev);
      while ($cur->fetch()) {
         $ZDBgeneIDgeneAbbrevs{$geneId} = $ZFINgeneAbbrev;
      }
   
      if ($cur->rows == 0) {
         $numErr++;
         print REPORT "\n$ct :: $geneId\n"; 
      }
      
      $cur->finish(); 
    }

    $featNamePart =  $allele;   
    if (!$geneId) {
      $featNamePart =~ s/Gt$//;
    }
    
    $featureId = $fields[4];
    $featureId =~ s/^\s+//; 
    $featureId =~ s/\s+$//;   
    
    ### check if the zdbIds of the features are valid or not   
    if ($featureId =~ m/ZDB-ALT-/)  {
      $cur = $dbh->prepare('select feature_abbrev from feature where feature_zdb_id = ?;');
      $cur->execute($featureId);
      my ($ZFINfeatureAbbrev);
      $cur->bind_columns(\$ZFINfeatureAbbrev);
      while ($cur->fetch()) {
         $ZDBfeatureIDfeatureAbbrevs{$featureId} = $ZFINfeatureAbbrev;
      }
   
      if ($cur->rows == 0) {
         $numErr++;
         print REPORT "\n$ct :: $featureId\n"; 
      }
      
      $cur->finish(); 
    }    

    $cnstrtId = $fields[6];
    $cnstrtId =~ s/^\s+//; 
    $cnstrtId =~ s/\s+$//;   

    ### check if the zdbIds of the constructs are valid or not   
    if ($cnstrtId =~ m/ZDB-GTCONSTRCT-/)  {
      $cur = $dbh->prepare('select mrkr_abbrev from marker where mrkr_zdb_id = ?;');
      $cur->execute($cnstrtId);
      my ($ZFINconstructAbbrev);
      $cur->bind_columns(\$ZFINconstructAbbrev);
      while ($cur->fetch()) {
         $ZDBconstructIDconstructAbbrevs{$cnstrtId} = $ZFINconstructAbbrev;
      }
   
      if ($cur->rows == 0) {
         $numErr++;
         print REPORT "\n$ct :: $cnstrtId\n"; 
      }
      
      $cur->finish(); 
    }

    $acc = $fields[7];
    $acc =~ s/^\s+//; 
    $acc =~ s/\s+$//;   

    $geno = $fields[8];
    
    @genoIds = split(/,/, $geno); 
    
    foreach $genoId (@genoIds) {
       $genoId =~ s/^\s+//; 
       $genoId =~ s/\s+$//;   
      if (!($genoId eq "" || $featureId eq "")) {
          print FEATGENO "$ct|$featureId|$genoId\n";
       }
    }
    
    print ZFISHBOOKPRELOAD "$ct|$prev|$lineNum|$allele|$geneId|$featNamePart|$featureId|$cnstrtId|$acc|\n";
    
  }
}
undef @lines;

$dbh->disconnect(); 

print  "\nnumber of error of pre_load checking:  $numErr\n";
print REPORT "\nnumErr:  $numErr\n";
close (REPORT);
close (FEATGENO);

sendReport();

exit if ($numErr > 0);

system("$ENV{'INFORMIXDIR'}/bin/dbaccess <!--|DB_NAME|--> loadZfishbookData.sql");

system("$ENV{'INFORMIXDIR'}/bin/dbaccess <!--|DB_NAME|--> cleanupGBTfeatureNotes.sql");

system("<!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/fishMart/runFishMart.sh  <!--|DB_NAME|-->");

sendPreLoadInput();

exit;
