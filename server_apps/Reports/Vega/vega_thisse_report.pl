#!/private/bin/perl 
#
# Report the genes in ZFIN that have a VEGA link and
# do not have a Thisse expression experiment.
#
# Three levels of significance
# 1- zgc: genes
# 2- genes with a GenBank cDNA accession number
# 3- genes without a GenBank cDNA accession number

use strict;
use MIME::Lite;


#set environment variables
$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";


chdir "<!--|ROOT_PATH|-->/server_apps/Reports/VEGA/";

my $sys_status = 0;

$sys_status = system("$ENV{'INFORMIXDIR'}/bin/dbaccess <!--|DB_NAME|--> vega_thisse_report.sql");
if ($sys_status > 0)
{
    &sendResult("SQL error genarating Vega file", "bsprunge\@cs.uoregon.edu", "./vega_thisse_report.unl");
}
else
{
    &sendResult("Vega file for the Thisse", "bsprunge\@cs.uoregon.edu", "./vega_thisse_report.unl");
}

exit;

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


