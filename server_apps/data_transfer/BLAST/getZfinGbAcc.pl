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

my $outputdir = "/research/zblastfiles/files/genomix/";
my $accFile = $outputdir."zfin_genbank_acc.unl";
my $accFile_cdna = $outputdir."zfin_genbank_cdna_acc.unl";
my $accFile_xpat = $outputdir."zfin_gene_xpat_cdna_acc.unl";

################
# Form the sql 
################

my $sql ="unload to \"$accFile\" delimiter \" \" select dblink_acc_num from db_link where dblink_fdbcont_zdb_id in (select fdbcont_zdb_id from foreign_db_contains where fdbcont_fdb_db_name = \"GenBank\" and fdbcont_fdbdt_super_type = \"sequence\")";  
     
my $sql_cdna ="unload to \"$accFile_cdna\" delimiter \" \" select dblink_acc_num from db_link where dblink_fdbcont_zdb_id in (select fdbcont_zdb_id from foreign_db_contains where fdbcont_fdb_db_name = \"GenBank\" and fdbcont_fdbdt_data_type = \"cDNA\") ";  

# query genbank cDNA and vega transcripts accessions on genes 
# that has expression data, and not named microRNA%
my $sql_xpat ="

create temp table tmp_xpatmrkr_zdb_id_list (t_xgl_mrkr_zdb_id	varchar(50) )with no log;

insert into tmp_xpatmrkr_zdb_id_list 
     select distinct xpatex_gene_zdb_id 
       from expression_experiment, marker 
      where xpatex_gene_zdb_id =  mrkr_zdb_id
        and mrkr_name[1,8] <> \"microRNA\"
        and exists (select xpatres_zdb_id from expression_result where xpatres_xpatex_zdb_id = xpatex_zdb_id);

insert into tmp_xpatmrkr_zdb_id_list 
     select distinct mrel_mrkr_2_zdb_id 
       from tmp_xpatmrkr_zdb_id_list, marker_relationship 
      where t_xgl_mrkr_zdb_id = mrel_mrkr_1_zdb_id 
        and mrel_type = \"gene encodes small segment\";

unload to \"$accFile_xpat\" delimiter \" \" 
   select distinct dblink_acc_num 
     from db_link, foreign_db_contains, tmp_xpatmrkr_zdb_id_list
    where dblink_linked_recid = t_xgl_mrkr_zdb_id
      and dblink_fdbcont_zdb_id = fdbcont_zdb_id 
      and fdbcont_fdb_db_name in (\"GenBank\",\"Vega_Trans\") 
      and fdbcont_fdbdt_data_type = \"cDNA\"
";  
  

#################
# Execute
#################
system ("echo '$sql' | $ENV{INFORMIXDIR}/bin/dbaccess $dbname -") && die "Failure on sql: $sql \n"; 
system ("echo '$sql_cdna' | $ENV{INFORMIXDIR}/bin/dbaccess $dbname -") && die "Failure on sql: $sql_cdna \n"; 
system ("echo '$sql_xpat' | $ENV{INFORMIXDIR}/bin/dbaccess $dbname -") && die "Failure on sql: $sql_xpat \n"; 

exit;
