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

}

#------------------ Main -------------------------
# set environment variables

$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";

#--------------------------------------------------------------------------
chdir "<!--|ROOT_PATH|-->/server_apps/Reports/PATO";

system("$ENV{'INFORMIXDIR'}/bin/dbaccess -a <!--|DB_NAME|--> PhenoytpeCount.sql > PhenotypeStatistics.txt 2> err.txt");

&sendResult("Monthly Phenotype statistics", "<!--|COUNT_PATO_OUT|-->","./PhenotypeStatistics.txt");
&sendResult("Monthly Phenotype statistics Err", "<!--|COUNT_VEGA_ERR|-->", "./err.txt");

print "\n call FinCount.pl to get monthly fin phenotype count\n";
system ("<!--|ROOT_PATH|-->/server_apps/Reports/PATO/FinCount.pl");


#--------------------------------------------------------------------------
# send Ken counts of various gene name types with & without orthology
system("<!--|ROOT_PATH|-->/server_apps/Reports/Nomenclature/get_uninformative.sh");





exit;
