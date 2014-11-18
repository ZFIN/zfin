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
		
  my $SUBJECT="Auto: Allele-known zfin gene associations input sent by Sanger";
  my $MAILTO="pm\@zfin.org";
  my $TXTFILE="./alleleZfinKnown.csv";
 
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


    my $SUBJECT="Auto: # of alleles-known ZFIN genes sent by Sanger";
    my $MAILTO="pm\@zfin.org,leyla\@zfin.org";
    my $TXTFILE="./countSangerKnownInput.txt";

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



  my $SUBJECT="Auto: 1 allele-multiple ZFIN genes association sent by Sanger";
  my $MAILTO="pm\@zfin.org,leyla\@zfin.org";
  my $TXTFILE="./duplicateGenes.unl";
 
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

  my $SUBJECT="Auto: Ensdarg-ZFIN gene matches made on Sanger allele-ENSDARG file";
    my $MAILTO="pm\@zfin.org,leyla\@zfin.org";
    my $TXTFILE="./zfinGeneEnsdargMatches.unl";

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






my $SUBJECT="Auto: Final Sanger allele-Known ZFIN genes to be loaded";
     my $MAILTO="pm\@zfin.org";
     my $TXTFILE="./sangerInputWithoutDuplicates.unl";

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

#------------------ Send output ----------------
# No parameter
#
sub sendPreLoadInput {

  my $SUBJECT="Auto: Sanger data new features";
  my $MAILTO="pm\@zfin.org,leyla\@zfin.org";
  my $TXTFILE="./pre_feature.unl";
 
  # Create a new multipart message:
  my $msg7 = new MIME::Lite
    From    => "$ENV{LOGNAME}",
    To      => "$MAILTO",
    Subject => "$SUBJECT",
    Type    => 'multipart/mixed';
 
  attach $msg7
   Type     => 'text/plain',   
   Path     => "$TXTFILE";

  # Output the message to sendmail

  open (SENDMAIL, "| /usr/lib/sendmail -t -oi");
  $msg7->print(\*SENDMAIL);
  
  my $SUBJECT="Auto: Sanger data new feature-marker relationships";
  my $MAILTO="pm\@zfin.org,leyla\@zfin.org";
  my $TXTFILE="./pre_fmrel.unl";
 
  # Create a new multipart message:
  my $msg8 = new MIME::Lite
    From    => "$ENV{LOGNAME}",
    To      => "$MAILTO",
    Subject => "$SUBJECT",
    Type    => 'multipart/mixed';
 
  attach $msg8
   Type     => 'text/plain',   
   Path     => "$TXTFILE";

  # Output the message to sendmail

  open (SENDMAIL, "| /usr/lib/sendmail -t -oi");
  $msg8->print(\*SENDMAIL);
 
 
  my $SUBJECT="Auto: Sanger data new dblinks";
  my $MAILTO="pm\@zfin.org,leyla\@zfin.org";
  my $TXTFILE="./pre_dblink.unl";
 
  # Create a new multipart message:
  my $msg9 = new MIME::Lite
    From    => "$ENV{LOGNAME}",
    To      => "$MAILTO",
    Subject => "$SUBJECT",
    Type    => 'multipart/mixed';
 
  attach $msg9
   Type     => 'text/plain',   
   Path     => "$TXTFILE";

  # Output the message to sendmail

  open (SENDMAIL, "| /usr/lib/sendmail -t -oi");
  $msg9->print(\*SENDMAIL);


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

### open a handle on the db


#remove old files
 
system("rm -f *.unl");
system("rm -f *.txt");

print "\nRunning Sanger loading script ...\n\n";

open (INPUT1, "alleleZfinKnown.csv") || die "Cannot open alleleZfinKnown.csv : $!\n";
@lines=<INPUT1>;
close(SANGERPRECOUNT);
close(SANGERINPUT);
%alleles = ();
%ZDBgeneIDgeneAbbrevs = ();
$ct = 0 - 1;
$numErr = 0;

open (SANGERPRECOUNT, ">countSangerKnownInput.txt") || die "Cannot open pre_load_input.txt : $!\n";
open (SANGERINPUT, ">sangerKnown.unl") || die "Cannot open sangerKnown.unl : $!\n";

foreach $line (@lines) {
  $ct++;
  next if $ct == 0;

  if ($line) {
    chop($line);
    undef (@fields);
    @fields = split(/\,/, $line); 
    $allele = $fields[0];
    $allele=~ s/"//g;
    $lineNum = substr($allele, 2);
    $geneID = $fields[1];
    $geneID=~ s/"//g;
    $bkground = $fields[2];
    $bkground=~ s/"//g;
    $bkground=~ s/^M//g;
    
   } 

    print SANGERINPUT "$allele|$geneID|$bkground|$lineNum|\n";
    
  }
   print SANGERPRECOUNT "$.\n";
undef @lines;


print "\nRunning Sanger multiple gene ...\n\n";
system("$ENV{'INFORMIXDIR'}/bin/dbaccess <!--|DB_NAME|--> checkMultipleGenes.sql");
print "\nRunning Sanger checkEnsdarg ...\n\n";
system("$ENV{'INFORMIXDIR'}/bin/dbaccess <!--|DB_NAME|--> lookforEnsdargMatches.sql");
system("chmod 777 *.*") ;
sendReport();
print "\nRunning Sanger load ...\n\n";
system("$ENV{'INFORMIXDIR'}/bin/dbaccess <!--|DB_NAME|--> loadSangerData131203.sql");
sendPreLoadInput();


exit;
