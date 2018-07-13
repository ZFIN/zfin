#!/private/bin/perl 
#
# this script reads the SwissProt accession list from
# a curator and match out the detailed records from problemfile
# and writes it into ok2file which would be appended to okfile
# in loadsp.pl process. 
#

use lib "<!--|ROOT_PATH|-->/server_apps/";
use ZFINPerlModules;

if (@ARGV < 1) {
    die "Please enter the accession file name. \n";
}

open ACC, "<$ARGV[0]" or die "Cannot open $ARGV[0] file";
undef $/;
$accfile = <ACC>;
close ACC;

open FILE, "<problemfile" or die "Cannot open problemfile";
open OK, ">ok2file" or die "Cannot open ok2file";
$/ = "//\n";
while (<FILE>) {
    foreach $acc (split /\n/,$accfile) {
        print OK if /$acc/;
    }
}
close FILE;
close OK;

  $dbname = "<!--|DB_NAME|-->";

  $subject = "Auto from $dbname: SWISS-PROT check report";
  ZFINPerlModules->sendMailWithAttachedReport("<!--|SWISSPROT_EMAIL_REPORT|-->","$subject","checkreport.txt");
  		
  #----- Another mail send out problem files ----
  $subject = "Auto from $dbname: SWISS-PROT problem file";
  ZFINPerlModules->sendMailWithAttachedReport("<!--|SWISSPROT_EMAIL_REPORT|-->","$subject","allproblems.txt");

  #----- Another mail send out problem files ----
  $subject = "Auto from $dbname: PubMed not in ZFIN";
  ZFINPerlModules->sendMailWithAttachedReport("<!--|SWISSPROT_EMAIL_REPORT|-->","$subject","pubmed_not_in_zfin");  

  #----- Another mail send out problem files ----
  $subject = "Auto from $dbname: report of processing pre_zfin.org";
  ZFINPerlModules->sendMailWithAttachedReport("<!--|SWISSPROT_EMAIL_REPORT|-->","$subject","redGeneReport.txt");
