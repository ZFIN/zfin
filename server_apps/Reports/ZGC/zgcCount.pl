#! /local/bin/perl -w 
#
# This monthly-run script queries the statistics on 
# ZFIN nomenclature and expression pattern related 
# to the ZGC project. The result is mailed to curators.
#
use strict;
use MIME::Lite;

#------------------ Send Result ----------------
# 
#
sub sendResult ($$$){
		
  my $SUBJECT=$_[0];
  my $MAILTO=$_[1];
  my $TXTFILE=$_[2];
 
  # Create a new multipart message:
  my $msg = new MIME::Lite 
    From    => "$ENV{LOGNAME}",
    To      => "$MAILTO",
    Subject => "$SUBJECT",
    Type    => 'multipart/mixed';
 
  attach $msg 
   Type     => 'text/plain',   
   Path     => "$TXTFILE";

  # Output the message to sendmail

  open (SENDMAIL, "| /usr/lib/sendmail -t -oi");
  $msg->print(\*SENDMAIL);

  close(SENDMAIL);
}

#------------------ Main -------------------------
# set environment variables

$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";

chdir "<!--|ROOT_PATH|-->/server_apps/Reports/ZGC";

system("$ENV{'INFORMIXDIR'}/bin/dbaccess <!--|DB_NAME|--> zgcCount.sql > zgcStatistics 2> err");

&sendResult("Monthly ZGC statistics", "sramacha\@uoneuro.uoregon.edu, van_slyke\@uoneuro.uoregon.edu", "./zgcStatistics");

&sendResult("Monthly ZGC statistics Err", "peirans\@cs.uoregon.edu", "./err");

exit;
