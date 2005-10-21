#!/private/bin/perl
#
# This script generates ZFIN GenBank accession and 
# GenBank cDNA accession lists for Genomix. Since it is
# internal use, it doesn't has to be generated on production.
# After Sunday night's almost reload, we schedule this script. 
#

use strict;

# define environment variable
$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";

my $dbname = "<!--|DB_NAME|-->";
my $username = "";
my $password = "";

my $outputdir = "<!--|FTP_ROOT|-->/pub/transfer/Genomix/";
my $accFile = $outputdir."zfin_genbank_acc.unl";
my $accFile_cdna = $outputdir."zfin_genbank_cdna_acc.unl";

################
# Form the sql 
################

my $sql ="unload to \"$accFile\" delimiter \" \" select distinct dblink_acc_num from db_link where dblink_fdbcont_zdb_id in (select fdbcont_zdb_id from foreign_db_contains where fdbcont_fdb_db_name = \"GenBank\" and fdbcont_fdbdt_super_type = \"sequence\")";  
     
my $sql_cdna ="unload to \"$accFile_cdna\" delimiter \" \" select distinct dblink_acc_num from db_link where dblink_fdbcont_zdb_id in (select fdbcont_zdb_id from foreign_db_contains where fdbcont_fdb_db_name = \"GenBank\" and fdbcont_fdbdt_data_type = \"cDNA\") ";  
  

#################
# Execute
#################
system ("echo '$sql' | $ENV{INFORMIXDIR}/bin/dbaccess $dbname -") && die "Failure on sql: $sql \n"; 
system ("echo '$sql_cdna' | $ENV{INFORMIXDIR}/bin/dbaccess $dbname -") && die "Failure on sql: $sql_cdna \n"; 

exit;
