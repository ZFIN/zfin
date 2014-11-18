#!/private/bin/perl

# FILE: zfishbook.pl
# load data provided by zfishbook 
# First, it does some sanity check such as to check if there is redundncy, invalid ZDBIds etc.
# If the numb er of errors is greater than 0, the script exits with only report and input file sent to Xiang
# Then it calls loadZfishbookData.sql to do the load and sends in email the records inserted into feature and genotype tables.

use MIME::Lite;


#------------------ Send load output ----------------
# 
#
sub sendLoadOutput() {

  my $SUBJECT="Auto: burgess lin data new features";
  my $MAILTO="pm\@zfin.org,yvonne\@uoneuro.uoregon.edu";
  my $FROM="pm\@zfin.org";
  my $TXTFILE="./newfeatures.unl";
 
  # Create a new multipart message:
  my $msg3 = new MIME::Lite 
    From    => "$FROM",
    To      => "$MAILTO",
    Subject => "$SUBJECT",
    Type    => 'multipart/mixed';
 
  attach $msg3 
   Type     => 'text/plain',   
   Path     => "$TXTFILE";

  # Output the message to sendmail

  open (SENDMAIL, "| /usr/lib/sendmail -t -oi");
  $msg3->print(\*SENDMAIL);

  my $SUBJECT="Auto: features not submitted ";
  my $MAILTO="pm\@zfin.org,yvonne\@uoneuro.uoregon.edu";
  my $FROM="pm\@zfin.org";
  my $TXTFILE="./featuresnotsubmitted.unl";
 
  # Create a new multipart message:
  my $msg4 = new MIME::Lite 
    From    => "$FROM",
    To      => "$MAILTO",
    Subject => "$SUBJECT",
    Type    => 'multipart/mixed';
 
  attach $msg4 
   Type     => 'text/plain',   
   Path     => "$TXTFILE";

  # Output the message to sendmail

  open (SENDMAIL, "| /usr/lib/sendmail -t -oi");
  $msg4->print(\*SENDMAIL);

  close(SENDMAIL);
}


#------------------ Send load output ----------------
# 
#
sub sendLoadLogs() {

  my $SUBJECT="Auto: bl data load weird accessions ";
  my $MAILTO="pm\@zfin.org";
  my $FROM="pm\@zfin.org";
  my $TXTFILE="./weirdaccession1.unl";

  # Create a new multipart message:
  my $msg1 = new MIME::Lite
    From    => "$FROM",
    To      => "$MAILTO",
    Subject => "$SUBJECT",
    Type    => 'multipart/mixed';

  attach $msg1
   Type     => 'text/plain',
   Path     => "$TXTFILE";

  # Output the message to sendmail

  open (SENDMAIL, "| /usr/lib/sendmail -t -oi");
  $msg1->print(\*SENDMAIL);

  my $SUBJECT="Auto: bl data load log2 genes not in zfin ";
  my $MAILTO="pm\@zfin.org";
  my $FROM="pm\@zfin.org";
  my $TXTFILE="./genesnotinzfin.unl";

  # Create a new multipart message:
  my $msg2 = new MIME::Lite
    From    => "$FROM",
    To      => "$MAILTO",
    Subject => "$SUBJECT",
    Type    => 'multipart/mixed';

  attach $msg1
   Type     => 'text/plain',
   Path     => "$TXTFILE";

  # Output the message to sendmail

  open (SENDMAIL, "| /usr/lib/sendmail -t -oi");
  $msg2->print(\*SENDMAIL);

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

chdir "<!--|ROOT_PATH|-->/server_apps/data_transfer/BurgessLin/";

#remove old files

system("rm -f *.unl");
system("rm -f log*");
$dir = "<!--|ROOT_PATH|-->";

@dirPieces = split(/www_homes/,$dir);

$dbname = $dirPieces[1];
$dbname =~ s/\///;

print $dbname;

print "\n\n";

print "\n\nStarting to load ...\n\n\n" ;

system("$ENV{'INFORMIXDIR'}/bin/dbaccess <!--|DB_NAME|--> delete_features_not_submitted.sql") > log1; 

system("$ENV{'INFORMIXDIR'}/bin/dbaccess <!--|DB_NAME|--> load_Burgess_Lin.sql") > log2; 


sendLoadLogs(); 

sendLoadOutput(); 

system("bed2gff3.awk BL.bed > Burgess_Lin.unl");
exit;
