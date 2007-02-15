#! /private/bin/perl -w
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

&sendResult("Monthly ZGC statistics","<!--|COUNT_ZGC_OUT|-->", "./zgcStatistics");
&sendResult("Monthly ZGC statistics Err", "<!--|COUNT_ZGC_ERR|-->", "./err");

#-----------------------------------------------------------------------

chdir "<!--|ROOT_PATH|-->/server_apps/Reports/Vega";

# Run vega_thisse_report.sql before VegaCount.sql.
# A file is created by vega_thisse_report.sql that is read by VegaCount.sql.

system("$ENV{'INFORMIXDIR'}/bin/dbaccess <!--|DB_NAME|--> vega_thisse_report.sql 2> err");

&sendResult("Monthly Vega-Thisse statistics", "<!--|COUNT_THISSE_VEGA_OUT|-->", "./vega_thisse_report.unl");
&sendResult("Vega-Thisse statistics Err", "<!--|COUNT_THISSE_VEGA_ERR|-->", "./err");


system("$ENV{'INFORMIXDIR'}/bin/dbaccess <!--|DB_NAME|--> VegaCount.sql > VegaStatistics 2> err");

&sendResult("Monthly Vega statistics", "<!--|COUNT_VEGA_OUT|-->","./VegaStatistics");
&sendResult("Monthly Vega statistics Err", "<!--|COUNT_VEGA_ERR|-->", "./err");


#--------------------------------------------------------------------------
chdir "<!--|ROOT_PATH|-->/server_apps/Reports/PATO";

system("$ENV{'INFORMIXDIR'}/bin/dbaccess <!--|DB_NAME|--> PhenoytpeCount.sql > PhenotypeStatistics 2> err");

&sendResult("Monthly Phenotype statistics", "<!--|COUNT_PATO_OUT|-->","./PhenotypeStatistics");
&sendResult("Monthly Phenotype statistics Err", "<!--|COUNT_VEGA_ERR|-->", "./err");

exit;
