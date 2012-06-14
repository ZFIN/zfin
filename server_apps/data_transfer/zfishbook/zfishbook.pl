#!/private/bin/perl

# FILE: zfishbook.pl
# load data provided by zfishbook 
# First, it does some sanity check such as to check if there is redundncy, invalid ZDBIds etc.
# If the numb er of errors is greater than 0, the script exits with only report and input file sent to Xiang
# Then it calls loadZfishbookData.sql to do the load and sends in email the records inserted into feature and genotype tables.

use MIME::Lite;


#------------------ Send output ----------------
# No parameter
#
sub sendPreLoadInput($) {

  my $SUBJECT="Auto: zfishbook data new genotypes on ".$_[0];
  my $MAILTO="xshao\@cs.uoregon.edu,yvonne\@uoneuro.uoregon.edu";
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

  my $SUBJECT="Auto: zfishbook data new features on ".$_[0];
  my $MAILTO="xshao\@cs.uoregon.edu,yvonne\@uoneuro.uoregon.edu";
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

$dir = "<!--|ROOT_PATH|-->";

@dirPieces = split(/www_homes/,$dir);

$dbname = $dirPieces[1];
$dbname =~ s/\///;

print $dbname;

print "\n\n";


print "\nRunning zfishbook loading script ...\n\n";

system("preprocess_zfishbook.pl");

open (ERRREPORT, "report") || die "Cannot open report : $!\n";

@lines = <ERRREPORT>;

foreach $line (@lines) {
   next if $line !~ m/numOfCrucialErrors/;
   chop($line);
   $line =~ s/numOfCrucialErrors:\s+//;
   $line =~ s/\s+$//;
   print "\nnumber of crucial errors:  $line\n\n"; 
   if ($line > 0) {
      print "\nThe loading is not done due to crucial error(s).\nExit!\n\n\n";
      exit;
   }
} 

close(ERRREPORT);

system("$ENV{'INFORMIXDIR'}/bin/dbaccess <!--|DB_NAME|--> loadZfishbookData.sql");

##system("$ENV{'INFORMIXDIR'}/bin/dbaccess <!--|DB_NAME|--> cleanupGBTfeatureNotes.sql");


sendPreLoadInput("$dbname");

exit;
