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
sub sendLoadOutput($) {

  my $SUBJECT="Auto from ".$_[0]." : zfishbook data - new genotypes";
  my $MAILTO="xshao\@zfin.org,yvonne\@uoneuro.uoregon.edu";
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

  my $SUBJECT="Auto from ".$_[0]." : zfishbook data - new features";
  my $MAILTO="xshao\@zfin.org,yvonne\@uoneuro.uoregon.edu";
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

  close(SENDMAIL);
}


#------------------ Send load output ----------------
# 
#
sub sendLoadLogs($) {

  my $SUBJECT="Auto from ".$_[0]." : zfishbook data load - log1";
  my $MAILTO="xshao\@zfin.org";
  my $TXTFILE="./log1";

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

  my $SUBJECT="Auto from ".$_[0]." : zfishbook data load - log2";
  my $MAILTO="xshao\@zfin.org";
  my $TXTFILE="./log2";

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

#remove old files
 
system("rm -f *.unl");
system("rm -f *.dat");
system("rm -f report");
system("rm -f log1");
system("rm -f log2");

$dbname = "<!--|DB_NAME|-->";

print "\nRunning zfishbook pre-process script ...\n\n";

system("preprocess_zfishbook.pl");

open (ERRREPORT, "report") || die "Cannot open report : $!\n";

@lines = <ERRREPORT>;

$doTheLoad = 1;
foreach $line (@lines) {
   next if $line !~ m/numOfCrucialErrors/;
   chop($line);
   $line =~ s/numOfCrucialErrors:\s+//;
   $line =~ s/\s+$//;
   print "\nnumber of crucial errors:  $line\n\n"; 
   if ($line > 0) {
      print "\nThe loading is not done due to crucial error(s).\nExit!\n\n\n";
      $doTheLoad = 0;
      exit;
   }
} 

close(ERRREPORT);

print "\nPre-processing done. doTheLoad =  $doTheLoad   \n\n";

print "\n\nStarting to load ...\n\n\n" if $doTheLoad > 0;

system("$ENV{'INFORMIXDIR'}/bin/dbaccess <!--|DB_NAME|--> loadZfishbookData.sql >log1 2> log2") if $doTheLoad > 0;

##system("$ENV{'INFORMIXDIR'}/bin/dbaccess <!--|DB_NAME|--> cleanupGBTfeatureNotes.sql");

sendLoadLogs("$dbname") if $doTheLoad > 0;

sendLoadOutput("$dbname") if $doTheLoad > 0;

print "\n\nLoading data done.\n\n\n" if $doTheLoad > 0;

exit;
